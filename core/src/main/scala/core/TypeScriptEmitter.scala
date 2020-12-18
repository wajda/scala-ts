package io.github.scalats.core

import java.io.PrintStream

import scala.collection.immutable.ListSet

// TODO: (low priority) Use a template engine (velocity?)
/**
 * @param out the function to select a `PrintStream` from type name
 */
final class TypeScriptEmitter(
  val config: Configuration,
  out: String => PrintStream,
  typeMapper: TypeScriptEmitter.TypeMapper) {

  import TypeScriptModel._
  import Internals.list

  import config.{
    discriminator => discriminatorName,
    typescriptIndent => indent
  }
  import config.typescriptLineSeparator.{ value => lineSeparator }

  def emit(declaration: ListSet[Declaration]): Unit =
    list(declaration).foreach {
      case decl: InterfaceDeclaration =>
        emitInterfaceDeclaration(decl)

      case decl: EnumDeclaration =>
        emitEnumDeclaration(decl)

      case decl: ClassDeclaration =>
        emitClassDeclaration(decl)

      case SingletonDeclaration(name, members, superInterface) =>
        emitSingletonDeclaration(name, members, superInterface)

      case UnionDeclaration(name, fields, possibilities, superInterface) =>
        emitUnionDeclaration(
          name, fields, possibilities, superInterface)
    }

  // ---

  private def emitUnionDeclaration(
    name: String,
    fields: ListSet[Member],
    possibilities: ListSet[CustomTypeRef],
    superInterface: Option[InterfaceDeclaration]): Unit = withOut(name) { o =>
    // Namespace and union type
    o.println(s"export namespace $name {")
    o.println(s"""${indent}type Union = ${possibilities.map(_.name) mkString " | "}${lineSeparator}""")

    if (config.emitCodecs.enabled) {
      // TODO: Config
      val naming: String => String = identity[String](_)
      val children = list(possibilities)

      // Decoder factory: MyClass.fromData({..})
      o.println(s"\n${indent}public static fromData(data: any): ${name} {")
      o.println(s"${indent}${indent}switch (data.${discriminatorName}) {")

      children.foreach { sub =>
        val clazz = if (sub.name startsWith "I") sub.name.drop(1) else sub.name

        o.println(s"""${indent}${indent}${indent}case "${naming(sub.name)}": {""")
        o.println(s"${indent}${indent}${indent}${indent}return ${clazz}.fromData(data)${lineSeparator}")
        o.println(s"${indent}${indent}${indent}}")
      }

      o.println(s"${indent}${indent}}")
      o.println(s"${indent}}")

      // Encoder
      o.println(s"\n${indent}public static toData(instance: ${name}): any {")

      children.zipWithIndex.foreach {
        case (sub, index) =>
          o.print(s"${indent}${indent}")

          if (index > 0) {
            o.print("} else ")
          }

          val clazz =
            if (sub.name startsWith "I") sub.name.drop(1) else sub.name

          o.println(s"if (instance instanceof ${sub.name}) {")
          o.println(s"${indent}${indent}${indent}const data = ${clazz}.toData(instance)${lineSeparator}")
          o.println(s"""${indent}${indent}${indent}data['$discriminatorName'] = "${naming(sub.name)}"${lineSeparator}""")
          o.println(s"${indent}${indent}${indent}return data${lineSeparator}")
      }

      o.println(s"${indent}${indent}}")
      o.println(s"${indent}}")
    }

    o.println("}")

    // Union interface
    o.print(s"\nexport interface I${name}")

    superInterface.foreach { iface =>
      o.print(s" extends I${iface.name}")
    }

    o.println(" {")

    // Abstract fields - common to all the subtypes
    val fieldNaming = config.fieldNaming(name, _: String)

    list(fields).foreach { member =>
      o.println(s"${indent}${fieldNaming(member.name)}: ${resolvedTypeMapper(name, member.name, member.typeRef)}${lineSeparator}")
    }

    o.println("}")
  }

  private def emitSingletonDeclaration(
    name: String,
    members: ListSet[Member],
    superInterface: Option[InterfaceDeclaration]): Unit = withOut(name) { o =>
    if (members.nonEmpty) {
      def mkString = members.map {
        case Member(nme, tpe) => s"$nme ($tpe)"
      }.mkString(", ")

      throw new IllegalStateException(s"Cannot emit static members for properties of singleton '$name': ${mkString}")
    }

    // Class definition
    o.print(s"export class $name")

    superInterface.filter(_ => members.isEmpty).foreach { i =>
      o.print(s" implements ${i.name}")
    }

    o.println(" {")

    o.println(s"${indent}private static instance: $name${lineSeparator}\n")

    o.println(s"${indent}private constructor() {}\n")
    o.println(s"${indent}public static getInstance() {")
    o.println(s"${indent}${indent}if (!${name}.instance) {")
    o.println(s"${indent}${indent}${indent}${name}.instance = new ${name}()${lineSeparator}")
    o.println(s"${indent}${indent}}\n")
    o.println(s"${indent}${indent}return ${name}.instance${lineSeparator}")
    o.println(s"${indent}}")

    if (config.emitCodecs.enabled) {
      // Decoder factory: MyClass.fromData({..})
      o.println(s"\n${indent}public static fromData(data: any): ${name} {")
      o.println(s"${indent}${indent}return ${name}.instance${lineSeparator}")
      o.println(s"${indent}}")

      // Encoder
      o.println(s"\n${indent}public static toData(instance: ${name}): any {")
      o.println(s"${indent}${indent}return instance${lineSeparator}")
      o.println(s"${indent}}")
    }

    o.println("}")
  }

  private def emitInterfaceDeclaration(
    decl: InterfaceDeclaration): Unit = {
    val InterfaceDeclaration(name, fields, typeParams, superInterface) = decl

    withOut(name) { o =>
      o.print(s"export interface $name${typeParameters(typeParams)}")

      superInterface.foreach { iface =>
        o.print(s" extends ${iface.name}")
      }

      o.println(" {")

      list(fields).reverse.foreach { member =>
        o.println(s"${indent}${config.fieldNaming(name, member.name)}: ${resolvedTypeMapper(name, member.name, member.typeRef)}${lineSeparator}")
      }

      o.println("}")
    }
  }

  private def emitEnumDeclaration(decl: EnumDeclaration): Unit = {
    val EnumDeclaration(name, values) = decl

    withOut(name) { o =>
      o.println(s"export enum $name {")

      list(values).foreach { value =>
        o.println(s"${indent}${value} = '${value}',")
      }

      o.println("}")
    }
  }

  private def emitClassDeclaration(decl: ClassDeclaration): Unit = {
    val ClassDeclaration(name, ClassConstructor(parameters),
      values, typeParams, _ /*superInterface*/ ) = decl

    withOut(name) { o =>
      val tparams = typeParameters(typeParams)

      if (values.nonEmpty) {
        def mkString = values.map {
          case Member(nme, tpe) => s"$nme ($tpe)"
        }.mkString(", ")

        throw new IllegalStateException(
          s"Cannot emit static members for class values: ${mkString}")
      }

      // Class definition
      o.print(s"export class ${name}${tparams}")

      if (config.emitInterfaces) {
        o.print(s" implements I${name}${tparams}")
      }

      o.println(" {")

      val fieldNaming = config.fieldNaming(name, _: String)

      list(values).foreach { v =>
        o.print(indent)

        if (config.emitInterfaces) {
          o.print("public ")
        }

        o.println(s"${fieldNaming(v.name)}: ${resolvedTypeMapper(name, v.name, v.typeRef)}${lineSeparator}")
      }

      val params = list(parameters).reverse

      if (!config.emitInterfaces) {
        // Class fields
        params.foreach { parameter =>
          o.print(s"${indent}public ${parameter.name}: ${resolvedTypeMapper(name, parameter.name, parameter.typeRef)}${lineSeparator}")
        }
      }

      // Class constructor
      o.print(s"${indent}constructor(")

      params.zipWithIndex.foreach {
        case (parameter, index) =>
          if (index > 0) {
            o.println(",")
          } else {
            o.println("")
          }

          o.print(s"${indent}${indent}")

          if (config.emitInterfaces) {
            o.print("public ")
          }

          o.print(s"${fieldNaming(parameter.name)}: ${resolvedTypeMapper(name, parameter.name, parameter.typeRef)}")
      }

      o.println(s"\n${indent}) {")

      params.foreach { parameter =>
        val nme = fieldNaming(parameter.name)

        o.println(s"${indent}${indent}this.${nme} = ${nme}${lineSeparator}")
      }
      o.println(s"${indent}}")

      // Codecs functions
      if (config.emitCodecs.enabled) {
        emitClassCodecs(o, decl)
      }

      o.println("}")
    }
  }

  private def emitClassCodecs(
    o: PrintStream,
    decl: ClassDeclaration): Unit = {
    import decl.{ constructor, name, typeParams }, constructor.parameters

    val tparams = typeParameters(typeParams)

    /* TODO: Review as toJSON/fromJSON,
     - support Date as string, support other class-trait as property
     - Return type { [key: string]: any }
     */

    o.println(s"\n${indent}public static fromData${tparams}(data: any): ${name}${tparams} {")

    if (config.fieldNaming == FieldNaming.Identity) {
      // optimized identity

      // Decoder factory: MyClass.fromData({..})
      o.println(s"${indent}${indent}return <${name}${tparams}>(data)${lineSeparator}")
      o.println(s"${indent}}")

      // Encoder
      o.println(s"\n${indent}public static toData${tparams}(instance: ${name}${tparams}): any {")
      o.println(s"${indent}${indent}return instance${lineSeparator}")
      o.println(s"${indent}}")
    } else {
      // Decoder factory: MyClass.fromData({..})
      o.print(s"${indent}${indent}return new ${name}${tparams}(")

      val params = list(parameters).reverse.zipWithIndex
      val fieldNaming = config.fieldNaming(name, _: String)

      params.foreach {
        case (parameter, index) =>
          val encoded = fieldNaming(parameter.name)

          if (index > 0) o.print(", ")

          o.print(s"data.${encoded}")
      }

      o.println(s")${lineSeparator}")
      o.println(s"${indent}}")

      // Encoder
      o.println(s"\n${indent}public static toData${tparams}(instance: ${name}${tparams}): any {")
      o.println(s"${indent}${indent}return {")

      params.foreach {
        case (parameter, index) =>
          val encoded = fieldNaming(parameter.name)

          if (index > 0) o.print(",\n")

          o.print(s"${indent}${indent}${indent}${encoded}: instance.${fieldNaming(parameter.name)}")
      }

      o.println(s"\n${indent}${indent}}${lineSeparator}")
      o.println(s"${indent}}")
    }
  }

  // ---

  @inline private def typeParameters(params: List[String]): String =
    if (params.isEmpty) "" else params.mkString("<", ", ", ">")

  private lazy val resolvedTypeMapper: TypeScriptTypeMapper.Resolved = {
    (ownerType: String, memberName: String, typeRef: TypeRef) =>
      typeMapper(resolvedTypeMapper, ownerType, memberName, typeRef).
        getOrElse(defaultTypeMapping(ownerType, memberName, typeRef))
  }

  private def defaultTypeMapping(
    ownerType: String,
    memberName: String,
    typeRef: TypeRef): String = {
    val tr = resolvedTypeMapper(ownerType, memberName, _: TypeRef)

    typeRef match {
      case NumberRef => "number"

      case BooleanRef => "boolean"

      case StringRef => "string"

      case DateRef | DateTimeRef => "Date"

      case ArrayRef(innerType) =>
        s"${tr(innerType)}[]"

      case TupleRef(params) =>
        params.map(tr).mkString("[", ", ", "]")

      case CustomTypeRef(name, Nil) => name

      case CustomTypeRef(name, params) =>
        s"$name<${params.map(tr).mkString(", ")}>"

      case UnknownTypeRef(typeName) => typeName

      case tpe: SimpleTypeRef => tpe.name

      case NullableType(innerType) if config.optionToNullable =>
        s"(${tr(innerType)} | null)"

      case NullableType(innerType) =>
        s"(${tr(innerType)} | undefined)"

      case UnionType(possibilities) =>
        possibilities.map(tr).mkString("(", " | ", ")")

      case MapType(keyType, valueType) =>
        s"{ [key: ${tr(keyType)}]: ${tr(valueType)} }" // TODO: Unit test

    }
  }

  private def withOut[T](name: String)(f: PrintStream => T): T = {
    lazy val print = out(name)

    try {
      val res = f(print)

      print.flush()

      res
    } finally {
      try {
        print.close()
      } catch {
        case scala.util.control.NonFatal(_) =>
      }
    }
  }
}

private[core] object TypeScriptEmitter {
  type TypeMapper = Function4[TypeScriptTypeMapper.Resolved, String, String, TypeScriptModel.TypeRef, Option[String]]
}
