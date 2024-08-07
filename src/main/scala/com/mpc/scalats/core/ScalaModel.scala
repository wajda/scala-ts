package com.mpc.scalats.core

object ScalaModel {

  sealed trait TypeRef {
    def isKnown: Boolean
  }

  trait KnownTypeRef extends TypeRef {
    override def isKnown: Boolean = true
  }

  trait SimpleTypeRef extends KnownTypeRef

  trait ComplexTypeRef extends KnownTypeRef

  case class TypeParamRef(name: String) extends KnownTypeRef

  case object IntRef extends SimpleTypeRef

  case object LongRef extends SimpleTypeRef

  case object DoubleRef extends SimpleTypeRef

  case object BooleanRef extends SimpleTypeRef

  case object StringRef extends SimpleTypeRef

  case object DateRef extends SimpleTypeRef

  case object DateTimeRef extends SimpleTypeRef

  case object StructRef extends ComplexTypeRef

  case class OptionRef(innerType: TypeRef) extends ComplexTypeRef

  case class UnionRef(innerType: TypeRef, innerType2: TypeRef) extends ComplexTypeRef

  case class MapRef(keyType: TypeRef, valueType: TypeRef) extends ComplexTypeRef

  case class CaseClassRef(name: String, typeArgs: Seq[TypeRef]) extends ComplexTypeRef

  case class SeqRef(innerType: TypeRef) extends ComplexTypeRef

  case class UnknownTypeRef(name: String) extends TypeRef {
    override def isKnown: Boolean = false
  }

  trait Entity {
    def name: String

    def members: Seq[EntityMember]
  }

  case class ClassEntity(name: String, members: Seq[EntityMember], params: Seq[String]) extends Entity

  case class ObjectEntity(name: String, members: Seq[EntityMember]) extends Entity

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

  case class SeqValue(itemType: TypeRef, items: Value*) extends Value {
    override def typeRef: TypeRef = SeqRef(itemType)
  }
}
