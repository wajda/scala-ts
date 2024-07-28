package com.mpc.scalats.core

import com.mpc.scalats.configuration.Config
import com.mpc.scalats.core.TypeScriptModel.AccessModifier.{Private, Public}

import java.io.PrintStream
import java.util.Date
import scala.collection.mutable

class TypeScriptEmitter(config: Config) {

  import TypeScriptModel._

  private val `"` = if (config.stringWithSingleQuotes) "'" else "\""
  private val ` ` = " " * config.indentSize
  private val `;` = if (config.emitSemicolons) ";" else ""

  def emit(declarations: List[Declaration], out: PrintStream): Unit = {
    declarations foreach {
      case decl: InterfaceDeclaration =>
        emitInterfaceDeclaration(decl, out, config.emitInterfacesAsTypes)
      case decl: ClassDeclaration =>
        emitClassDeclaration(decl, out)
      case decl: ConstantDeclaration =>
        emitConstant(decl, out)
    }
  }

  private def emitConstant(decl: ConstantDeclaration, out: PrintStream): Unit = {
    val ConstantDeclaration(Member(name, typeRef), value) = decl
    assume(typeRef == value.typeRef)
    out.print(s"export const $name")
    val typeName = getTypeRefString(typeRef)
    if (typeName != "object") {
      out.print(s": $typeName")
    }
    out.println(s" = ${emitValue(value, 0)}${`;`}")
    out.println()
  }

  private def emitValue(value: TypeScriptModel.Value, indent: Int): String = {
    value match {
      // Primitive value
      case PrimitiveValue(null, NullRef) => "null"
      case PrimitiveValue(null, UndefinedRef) => "undefined"
      case PrimitiveValue(v, StringRef) => s"""${`"`}$v${`"`}"""
      case PrimitiveValue(date: Date, DateRef) => s"new Date(${date.getTime})"
      case PrimitiveValue(millis: Number, DateRef) => s"new Date($millis)"
      case PrimitiveValue(v, _) => v.toString

      // Array value
      case ArrayValue(_, items@_*) =>
        val margin = ` ` * indent
        val sb = new mutable.StringBuilder
        sb.append("[\n")
        items.zipWithIndex.foreach {
          case (item, i) =>
            sb.append(s"$margin${` `}${emitValue(item, indent + 1)}")
            if (i < items.length - 1) sb.append(",")
            sb.append("\n")
        }
        sb.append(s"$margin]")
        sb.toString()

      // Object value
      case ObjectValue(members) =>
        val margin = ` ` * indent
        val sb = new mutable.StringBuilder
        sb.append("{\n")
        members.zipWithIndex.foreach {
          case ((member, memberValue), i) =>
            sb.append(s"$margin${` `}${member.name}: ${emitValue(memberValue, indent + 1)}")
            if (i < members.length - 1) sb.append(",")
            sb.append("\n")
        }
        sb.append(s"$margin}")
        sb.toString()
    }
  }

  private def emitInterfaceDeclaration(decl: InterfaceDeclaration, out: PrintStream, emitAsType: Boolean): Unit = {
    val InterfaceDeclaration(name, members, typeParams) = decl
    out.print(if (emitAsType) s"export type $name" else s"export interface $name")
    emitTypeParams(decl.typeParams, out)
    out.println(if (emitAsType) " = {" else " {")
    members foreach { member =>
      out.print(s"${` `}${member.name}: ${getTypeRefString(member.typeRef)}")
      out.println(if (emitAsType) "," else ";")
    }
    out.println("}")
    out.println()
  }

  private def emitClassDeclaration(decl: ClassDeclaration, out: PrintStream): Unit = {
    val ClassDeclaration(name, ClassConstructor(parameters), typeParams) = decl
    out.print(s"export class $name")
    emitTypeParams(decl.typeParams, out)
    out.println(" {")
    out.println(s"\tconstructor(")
    parameters.zipWithIndex foreach { case (parameter, index) =>
      val accessModifier = parameter.accessModifier match {
        case Some(Public) => "public "
        case Some(Private) => "private "
        case None => ""
      }
      out.print(s"\t\t$accessModifier${parameter.name}: ${getTypeRefString(parameter.typeRef)}")
      val endLine = if (index + 1 < parameters.length) "," else ""
      out.println(endLine)
    }
    out.println("\t) {}")
    out.println("}")
  }

  private def emitTypeParams(params: List[String], out: PrintStream): Unit =
    if (params.nonEmpty) {
      out.print("<")
      out.print(params.mkString(", "))
      out.print(">")
    }

  private def getTypeRefString(typeRef: TypeRef): String = typeRef match {
    case NumberRef => "number"
    case BooleanRef => "boolean"
    case StringRef => "string"
    case DateRef | DateTimeRef => "Date"
    case ObjectRef | DateTimeRef => "object"
    case ArrayRef(innerType) => s"${getTypeRefString(innerType)}[]"
    case CustomTypeRef(name, params) if params.isEmpty => name
    case CustomTypeRef(name, params) if params.nonEmpty =>
      s"$name<${params.map(getTypeRefString).mkString(", ")}>"
    case UnknownTypeRef(typeName) => typeName
    case TypeParamRef(param) => param
    case UnionType(inner1, inner2) => s"(${getTypeRefString(inner1)} | ${getTypeRefString(inner2)})"
    case MapType(keyType, valueType) => s"{ [key: ${getTypeRefString(keyType)}]: ${getTypeRefString(valueType)} }"
    case NullRef => "null"
    case UndefinedRef => "undefined"
  }

}
