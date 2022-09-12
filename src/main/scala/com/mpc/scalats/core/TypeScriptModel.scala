package com.mpc.scalats.core

object TypeScriptModel {

  sealed trait Declaration

  sealed trait TypeRef

  sealed trait Value {
    def typeRef: TypeRef
  }

  sealed trait AccessModifier

  case class CustomTypeRef(name: String, typeArgs: List[TypeRef]) extends TypeRef

  case class ArrayRef(innerType: TypeRef) extends TypeRef

  case class InterfaceDeclaration(name: String, members: List[Member], typeParams: List[String]) extends Declaration

  case class ConstantDeclaration(member: Member, value: Value) extends Declaration

  case class Member(name: String, typeRef: TypeRef)

  case class PrimitiveValue(value: Any, typeRef: TypeRef) extends Value

  case class ObjectValue(members: List[(Member, Value)]) extends Value {
    override def typeRef: TypeRef = ObjectRef
  }

  case class ClassDeclaration(name: String, constructor: ClassConstructor, typeParams: List[String]) extends Declaration

  case class ClassConstructor(parameters: List[ClassConstructorParameter])

  case class ClassConstructorParameter(name: String,
    typeRef: TypeRef,
    accessModifier: Option[AccessModifier])

  case class UnknownTypeRef(name: String) extends TypeRef

  case object ObjectRef extends TypeRef

  case object NumberRef extends TypeRef

  case object StringRef extends TypeRef

  case object BooleanRef extends TypeRef

  case object DateRef extends TypeRef

  case object DateTimeRef extends TypeRef

  case object NullRef extends TypeRef

  case object UndefinedRef extends TypeRef

  case class TypeParamRef(name: String) extends TypeRef

  case class UnionType(inner1: TypeRef, inner2: TypeRef) extends TypeRef

  case class MapType(keyType: TypeRef, valueType: TypeRef) extends TypeRef

  case object AccessModifier {

    case object Public extends AccessModifier

    case object Private extends AccessModifier

  }

}
