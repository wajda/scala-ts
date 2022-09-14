package com.mpc.scalats.core

import com.mpc.scalats.configuration.Config
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class CompilerSpec
  extends AnyFlatSpec
    with Matchers {

  it should "compile scala model" in {
    implicit val config: Config = Config()

    val decls = Compiler.compile(List(
      ScalaModel.ObjectEntity(
        "AAA",
        List(
          ScalaModel.EntityMember("bar", ScalaModel.StructRef, ScalaModel.StructValue(
            ScalaModel.EntityMember("qux", ScalaModel.IntRef, ScalaModel.SimpleValue(777, ScalaModel.IntRef)),
            ScalaModel.EntityMember("baz", ScalaModel.IntRef, ScalaModel.SimpleValue(555, ScalaModel.IntRef))
          )),
          ScalaModel.EntityMember("Bar", ScalaModel.StructRef, ScalaModel.StructValue()),
          ScalaModel.EntityMember("Foo", ScalaModel.StructRef, ScalaModel.StructValue(
            ScalaModel.EntityMember("z", ScalaModel.SeqRef(ScalaModel.IntRef), ScalaModel.SimpleValue(List(1, 2, 3), ScalaModel.SeqRef(ScalaModel.IntRef))),
            ScalaModel.EntityMember("y", ScalaModel.StringRef, ScalaModel.SimpleValue("yyy", ScalaModel.StringRef))
          )),
          ScalaModel.EntityMember("x", ScalaModel.IntRef, ScalaModel.SimpleValue(42, ScalaModel.IntRef))
        ))
    ))

    decls should contain theSameElementsAs Seq(
      TypeScriptModel.ConstantDeclaration(
        TypeScriptModel.Member("AAA", TypeScriptModel.ObjectRef),
        TypeScriptModel.ObjectValue(List(
          (TypeScriptModel.Member("bar", TypeScriptModel.ObjectRef), TypeScriptModel.ObjectValue(List(
            (TypeScriptModel.Member("qux", TypeScriptModel.NumberRef), TypeScriptModel.PrimitiveValue(777, TypeScriptModel.NumberRef)),
            (TypeScriptModel.Member("baz", TypeScriptModel.NumberRef), TypeScriptModel.PrimitiveValue(555, TypeScriptModel.NumberRef))
          ))),
          (TypeScriptModel.Member("Bar", TypeScriptModel.ObjectRef), TypeScriptModel.ObjectValue(Nil)),
          (TypeScriptModel.Member("Foo", TypeScriptModel.ObjectRef), TypeScriptModel.ObjectValue(List(
            (TypeScriptModel.Member("z", TypeScriptModel.ArrayRef(TypeScriptModel.NumberRef)), TypeScriptModel.PrimitiveValue(List(1, 2, 3), TypeScriptModel.ArrayRef(TypeScriptModel.NumberRef))),
            (TypeScriptModel.Member("y", TypeScriptModel.StringRef), TypeScriptModel.PrimitiveValue("yyy", TypeScriptModel.StringRef))
          ))),
          (TypeScriptModel.Member("x", TypeScriptModel.NumberRef), TypeScriptModel.PrimitiveValue(42, TypeScriptModel.NumberRef))
        ))
      )
    )
  }
}
