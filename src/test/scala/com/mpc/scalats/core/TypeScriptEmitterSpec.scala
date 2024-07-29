package com.mpc.scalats.core

import com.mpc.scalats.StreamFixture
import com.mpc.scalats.configuration.Config
import com.mpc.scalats.core.TypeScriptModel.AccessModifier.{Private, Public}
import com.mpc.scalats.core.TypeScriptModel._
import org.scalactic.StringNormalizations._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import java.util.Date

class TypeScriptEmitterSpec
  extends AnyFlatSpec
    with Matchers
    with StreamFixture {

  it should "emit nothing for empty declaration list" in {
    val result = withPrintStreamAsUTF8String(new TypeScriptEmitter(Config()).emit(Nil, _))

    result should be(empty)
  }

  it should "emit an interface" in {
    val decls = Seq(
      InterfaceDeclaration(
        "Foo",
        Seq(
          Member("aNull", NullRef),
          Member("anUndefined", UndefinedRef),
          Member("aString", StringRef),
          Member("aBoolean", BooleanRef),
          Member("aNumber", NumberRef),
          Member("aDate", DateRef),
          Member("aDateTime", DateTimeRef)
        ),
        Nil
      )
    )

    val result = withPrintStreamAsUTF8String(new TypeScriptEmitter(Config()).emit(decls, _))

    result should equal(
      """
        |export interface Foo {
        |  aNull: null
        |  anUndefined: undefined
        |  aString: string
        |  aBoolean: boolean
        |  aNumber: number
        |  aDate: Date
        |  aDateTime: Date
        |}
        |""".stripMargin
    )(after being trimmed)
  }

  it should "emit a type" in {
    val decls = Seq(
      InterfaceDeclaration(
        "Foo",
        Seq(
          Member("aNull", NullRef),
          Member("anUndefined", UndefinedRef),
          Member("aString", StringRef),
          Member("aBoolean", BooleanRef),
          Member("aNumber", NumberRef),
          Member("aDate", DateRef),
          Member("aDateTime", DateTimeRef)
        ),
        Nil
      )
    )

    val result = withPrintStreamAsUTF8String(new TypeScriptEmitter(Config(emitInterfacesAsTypes = true)).emit(decls, _))

    result should equal(
      """
        |export type Foo = {
        |  aNull: null,
        |  anUndefined: undefined,
        |  aString: string,
        |  aBoolean: boolean,
        |  aNumber: number,
        |  aDate: Date,
        |  aDateTime: Date,
        |}
        |""".stripMargin
    )(after being trimmed)
  }

  it should "emit an class" in {
    val decls = Seq(
      ClassDeclaration(
        "Bar",
        ClassConstructor(
          Seq(
            ClassConstructorParameter("aNull", NullRef, None),
            ClassConstructorParameter("anUndefined", UndefinedRef, None),
            ClassConstructorParameter("aString", StringRef, None),
            ClassConstructorParameter("aBoolean", BooleanRef, Some(Private)),
            ClassConstructorParameter("aNumber", NumberRef, Some(Private)),
            ClassConstructorParameter("aDate", DateRef, Some(Public)),
            ClassConstructorParameter("aDateTime", DateTimeRef, Some(Public))
          )
        ),
        Nil
      )
    )

    val result = withPrintStreamAsUTF8String(new TypeScriptEmitter(Config()).emit(decls, _))

    result should equal(
      """
        |export class Bar {
        |	constructor(
        |		aNull: null,
        |		anUndefined: undefined,
        |		aString: string,
        |		private aBoolean: boolean,
        |		private aNumber: number,
        |		public aDate: Date,
        |		public aDateTime: Date
        |	) {}
        |}
        |""".stripMargin
    )(after being trimmed)
  }

  it should "emit primitive constants" in {
    val decls = Seq(
      ConstantDeclaration(Member("ANull", NullRef), PrimitiveValue(null, NullRef)),
      ConstantDeclaration(Member("AUndefined", UndefinedRef), PrimitiveValue(null, UndefinedRef)),
      ConstantDeclaration(Member("ANumber", NumberRef), PrimitiveValue(42, NumberRef)),
      ConstantDeclaration(Member("AString", StringRef), PrimitiveValue("blah", StringRef)),
      ConstantDeclaration(Member("ABoolean", BooleanRef), PrimitiveValue(true, BooleanRef)),
      ConstantDeclaration(Member("ADate1", DateRef), PrimitiveValue(new Date(1), DateRef)),
      ConstantDeclaration(Member("ADate2", DateRef), PrimitiveValue(1, DateRef))
    )

    val result = withPrintStreamAsUTF8String(new TypeScriptEmitter(Config(emitSemicolons = true)).emit(decls, _))

    result should equal(
      """
        |export const ANull: null = null as const;
        |
        |export const AUndefined: undefined = undefined as const;
        |
        |export const ANumber: number = 42 as const;
        |
        |export const AString: string = "blah" as const;
        |
        |export const ABoolean: boolean = true as const;
        |
        |export const ADate1: Date = new Date(1) as const;
        |
        |export const ADate2: Date = new Date(1) as const;
        |
        |""".stripMargin)(after being trimmed)
  }

  it should "emit objects" in {
    val decls = Seq(
      ConstantDeclaration(Member("ANumber", NumberRef), PrimitiveValue(42, NumberRef)),
      ConstantDeclaration(Member("AnArray", ArrayRef(NumberRef)), ArrayValue(NumberRef, PrimitiveValue(71, NumberRef), PrimitiveValue(72, NumberRef))),
      ConstantDeclaration(Member("ANestedArray", ArrayRef(ArrayRef(NumberRef))),
        ArrayValue(
          ArrayRef(NumberRef),
          ArrayValue(NumberRef, PrimitiveValue(81, NumberRef), PrimitiveValue(82, NumberRef)),
          ArrayValue(NumberRef, PrimitiveValue(91, NumberRef), PrimitiveValue(92, NumberRef))
        )),
      ConstantDeclaration(Member("AnEmptyObject", ObjectRef), ObjectValue(Nil)),
      ConstantDeclaration(Member("NonEmptyObject", ObjectRef), ObjectValue(Seq(
        (Member("APrimitive", NumberRef), PrimitiveValue(42, NumberRef)),
        (Member("ASubObject1", ObjectRef), ObjectValue(Seq(
          (Member("ASubPrimitive1", NumberRef), PrimitiveValue(111, NumberRef))
        ))),
        (Member("ASubObject2", ObjectRef), ObjectValue(Seq(
          (Member("ASubPrimitive2", NumberRef), PrimitiveValue(222, NumberRef)),
          (Member("ASubSubObject", ObjectRef), ObjectValue(Seq(
            (Member("ASubSubPrimitive", StringRef), PrimitiveValue("a string", StringRef))
          )))
        )))
      )))
    )

    val result = withPrintStreamAsUTF8String(new TypeScriptEmitter(Config()).emit(decls, _))

    result should equal(
      """
        |export const ANumber: number = 42 as const
        |
        |export const AnArray: number[] = [
        |  71,
        |  72
        |] as const
        |
        |export const ANestedArray: number[][] = [
        |  [
        |    81,
        |    82
        |  ],
        |  [
        |    91,
        |    92
        |  ]
        |] as const
        |
        |export const AnEmptyObject = {
        |} as const
        |
        |export const NonEmptyObject = {
        |  APrimitive: 42,
        |  ASubObject1: {
        |    ASubPrimitive1: 111
        |  },
        |  ASubObject2: {
        |    ASubPrimitive2: 222,
        |    ASubSubObject: {
        |      ASubSubPrimitive: "a string"
        |    }
        |  }
        |} as const
        |""".stripMargin)(after being trimmed)
  }

}
