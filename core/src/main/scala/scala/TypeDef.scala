package io.github.scalats.scala

import io.github.scalats.core.Internals.ListSet

case class QualifiedIdentifier(
    name: String,
    enclosingClassNames: List[String])

sealed trait TypeDef {
  def identifier: QualifiedIdentifier
}

case class CaseClass private (
    identifier: QualifiedIdentifier,
    fields: ListSet[TypeMember],
    values: ListSet[TypeInvariant],
    typeArgs: List[String])
    extends TypeDef

case class ValueClass(
    identifier: QualifiedIdentifier,
    field: TypeMember)
    extends TypeDef

case class CaseObject private (
    identifier: QualifiedIdentifier,
    values: ListSet[TypeInvariant])
    extends TypeDef

case class SealedUnion(
    identifier: QualifiedIdentifier,
    fields: ListSet[TypeMember],
    possibilities: ListSet[TypeDef])
    extends TypeDef

case class EnumerationDef(
    identifier: QualifiedIdentifier,
    values: ListSet[String])
    extends TypeDef