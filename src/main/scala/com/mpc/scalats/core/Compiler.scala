package com.mpc.scalats.core

import com.mpc.scalats.configuration.Config
import com.mpc.scalats.core.ScalaModel.{ClassEntity, EntityMember, ObjectEntity}
import com.mpc.scalats.core.TypeScriptModel.{NullRef, ObjectRef, UndefinedRef}

/**
 * Created by Milosz on 09.06.2016.
 */
object Compiler {

  def compile(scalaEntities: List[ScalaModel.Entity])(implicit config: Config): List[TypeScriptModel.Declaration] = {
    scalaEntities map { scalaEntity =>
      compileEntity(scalaEntity)
    }
  }

  private def compileEntity(entity: ScalaModel.Entity)(implicit config: Config): TypeScriptModel.Declaration = entity match {
    case ce: ClassEntity =>
      TypeScriptModel.InterfaceDeclaration(
        ce.name,
        ce.members.map { scalaMember =>
          TypeScriptModel.Member(
            scalaMember.name,
            compileTypeRef(scalaMember.typeRef)
          )
        },
        typeParams = ce.params
      )
    case oe: ObjectEntity =>
      TypeScriptModel.ConstantDeclaration(
        TypeScriptModel.Member(oe.name, ObjectRef),
        TypeScriptModel.ObjectValue(oe.members.map(compileMember))
      )
  }

  private def compileMember(scalaMember: ScalaModel.EntityMember)(implicit config: Config): (TypeScriptModel.Member, TypeScriptModel.Value) = {
    val member = TypeScriptModel.Member(
      scalaMember.name,
      compileTypeRef(scalaMember.typeRef)
    )
    val value = scalaMember.valueOpt.get match {
      case sv:ScalaModel.SimpleValue =>
        TypeScriptModel.PrimitiveValue(sv.value, compileTypeRef(sv.typeRef))
      case sv:ScalaModel.StructValue =>
        TypeScriptModel.ObjectValue(sv.members.map(compileMember).toList)
    }
    (member, value)
  }

  private def compileTypeRef(
    scalaTypeRef: ScalaModel.TypeRef
  )(implicit config: Config): TypeScriptModel.TypeRef = scalaTypeRef match {
    case ScalaModel.IntRef =>
      TypeScriptModel.NumberRef
    case ScalaModel.LongRef =>
      TypeScriptModel.NumberRef
    case ScalaModel.DoubleRef =>
      TypeScriptModel.NumberRef
    case ScalaModel.BooleanRef =>
      TypeScriptModel.BooleanRef
    case ScalaModel.StringRef =>
      TypeScriptModel.StringRef
    case ScalaModel.SeqRef(innerType) =>
      TypeScriptModel.ArrayRef(compileTypeRef(innerType))
    case ScalaModel.CaseClassRef(name, typeArgs) =>
      TypeScriptModel.CustomTypeRef(
        name,
        typeArgs.map(compileTypeRef(_)))
    case ScalaModel.DateRef =>
      TypeScriptModel.DateRef
    case ScalaModel.DateTimeRef =>
      TypeScriptModel.DateTimeRef
    case ScalaModel.TypeParamRef(name) =>
      TypeScriptModel.TypeParamRef(name)
    case ScalaModel.OptionRef(innerType)
      if config.optionToNullable && config.optionToUndefined =>
      TypeScriptModel.UnionType(
        TypeScriptModel.UnionType(compileTypeRef(innerType),
          NullRef),
        UndefinedRef)
    case ScalaModel.OptionRef(innerType) if config.optionToNullable =>
      TypeScriptModel.UnionType(compileTypeRef(innerType),
        NullRef)
    case ScalaModel.MapRef(kT, vT) =>
      TypeScriptModel.MapType(compileTypeRef(kT),
        compileTypeRef(vT))
    case ScalaModel.UnionRef(i, i2) =>
      TypeScriptModel.UnionType(compileTypeRef(i),
        compileTypeRef(i2))
    case ScalaModel.OptionRef(innerType) if config.optionToUndefined =>
      TypeScriptModel.UnionType(compileTypeRef(innerType),
        UndefinedRef)
    case ScalaModel.StructRef =>
      TypeScriptModel.ObjectRef
    case ScalaModel.UnknownTypeRef(_) =>
      TypeScriptModel.StringRef
  }

}
