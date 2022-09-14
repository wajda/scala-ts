package com.mpc.scalats.core

object ScalaModel {

  sealed trait TypeRef {
    def isKnown: Boolean
  }

  case class OptionRef(innerType: TypeRef) extends KnownTypeRef

  case class UnionRef(innerType: TypeRef, innerType2: TypeRef) extends KnownTypeRef

  case class MapRef(keyType: TypeRef, valueType: TypeRef) extends KnownTypeRef

  case class CaseClassRef(name: String, typeArgs: List[TypeRef]) extends KnownTypeRef

  case class SeqRef(innerType: TypeRef) extends KnownTypeRef

  case class Entity(name: String, members: List[EntityMember], params: List[String])

  case class EntityMember(name: String, typeRef: TypeRef, valueOpt: Option[Value])

  object EntityMember {
    def apply(name: String, typeRef: TypeRef): EntityMember = EntityMember(name, typeRef, None)
    def apply(name: String, typeRef: TypeRef, value: Value): EntityMember = EntityMember(name, typeRef, Some(value))
  }

  sealed trait Value {
    def typeRef: TypeRef
  }

  case class SimpleValue(value: Any, typeRef: TypeRef) extends Value

  case class StructValue(members: EntityMember*) extends Value {
    override def typeRef: TypeRef = StructRef
  }

  trait KnownTypeRef extends TypeRef {
    override def isKnown: Boolean = true
  }
  case class UnknownTypeRef(name: String) extends TypeRef {
    override def isKnown: Boolean = false
  }

  case class TypeParamRef(name: String) extends KnownTypeRef

  case object IntRef extends KnownTypeRef

  case object LongRef extends KnownTypeRef

  case object DoubleRef extends KnownTypeRef

  case object BooleanRef extends KnownTypeRef

  case object StringRef extends KnownTypeRef

  case object DateRef extends KnownTypeRef

  case object DateTimeRef extends KnownTypeRef

  case object StructRef extends KnownTypeRef

}
