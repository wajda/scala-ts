package com.mpc.scalats.core

import com.mpc.scalats.core.ScalaModel._
import com.mpc.scalats.core.ScalaParserSpec.TestTypes
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.reflect.runtime.universe
import scala.reflect.runtime.universe._

/**
 * Created by Milosz on 06.12.2016.
 */
class ScalaParserSpec extends AnyFlatSpec with Matchers {

  it should "parse case class with one primitive member" in {
    val parsed = ScalaParser.parseCaseClasses(List(TestTypes.TestClass1Type))
    val expected = ClassEntity("TestClass1", List(EntityMember("name", StringRef)), List.empty)
    parsed should contain(expected)
  }

  it should "parse generic case class with one member" in {
    val parsed = ScalaParser.parseCaseClasses(List(TestTypes.TestClass2Type))
    val expected = ClassEntity("TestClass2", List(EntityMember("name", TypeParamRef("T"))), List("T"))
    parsed should contain(expected)
  }

  it should "parse generic case class with one member list of type parameter" in {
    val parsed = ScalaParser.parseCaseClasses(List(TestTypes.TestClass3Type))
    val expected = ClassEntity(
      "TestClass3",
      List(EntityMember("name", SeqRef(TypeParamRef("T")))),
      List("T")
    )
    parsed should contain(expected)
  }

  it should "parse generic case class with one optional member" in {
    val parsed = ScalaParser.parseCaseClasses(List(TestTypes.TestClass5Type))
    val expected = ClassEntity(
      "TestClass5",
      List(EntityMember("name", OptionRef(TypeParamRef("T")))),
      List("T")
    )
    parsed should contain(expected)
  }

  it should "correctly detect involved types" in {
    val parsed = ScalaParser.parseCaseClasses(List(TestTypes.TestClass6Type))
    parsed should have length 6
  }

  it should "correctly handle either types" in {
    val parsed = ScalaParser.parseCaseClasses(List(TestTypes.TestClass7Type))
    val expected = ClassEntity(
      "TestClass7",
      List(EntityMember("name", UnionRef(CaseClassRef("TestClass1", List()), CaseClassRef("TestClass1B", List())))),
      List("T")
    )
    parsed should contain(expected)
  }

  it should "parse object alone" in {
    val parsed = ScalaParser.parseCaseClasses(
      List(
        TestTypes.TestObjectType,
        TestTypes.TestTraitWithCompanionObjectType
      )
    )

    parsed should contain theSameElementsAs Seq(
      ObjectEntity(
        "Foo",
        List(
          EntityMember("z", SeqRef(IntRef), SimpleValue(List(1, 2, 3), SeqRef(IntRef))),
          EntityMember("y", StringRef, SimpleValue("yyy", StringRef))
        )),
      ObjectEntity(
        "TestTraitWithCompanionObject",
        List(
          EntityMember("bar", StructRef, StructValue(
            EntityMember("qux", IntRef, SimpleValue(777, IntRef)),
            EntityMember("baz", IntRef, SimpleValue(555, IntRef))
          )),
          EntityMember("Bar", StructRef, StructValue()),
          EntityMember("Foo", StructRef, StructValue(
            EntityMember("z", SeqRef(IntRef), SimpleValue(List(1, 2, 3), SeqRef(IntRef))),
            EntityMember("y", StringRef, SimpleValue("yyy", StringRef))
          )),
          EntityMember("x", IntRef, SimpleValue(42, IntRef))
        ))
    )
  }

}

object ScalaParserSpec {
  object TestTypes {

    implicit val mirror: universe.Mirror = runtimeMirror(getClass.getClassLoader)

    private[ScalaParserSpec] lazy val TestClass1Type = classTypeForName("com.mpc.scalats.core.ScalaParserSpec.TestTypes.TestClass1")
    private[ScalaParserSpec] lazy val TestClass2Type = classTypeForName("com.mpc.scalats.core.ScalaParserSpec.TestTypes.TestClass2")
    private[ScalaParserSpec] lazy val TestClass3Type = classTypeForName("com.mpc.scalats.core.ScalaParserSpec.TestTypes.TestClass3")
    private[ScalaParserSpec] lazy val TestClass4Type = classTypeForName("com.mpc.scalats.core.ScalaParserSpec.TestTypes.TestClass4")
    private[ScalaParserSpec] lazy val TestClass5Type = classTypeForName("com.mpc.scalats.core.ScalaParserSpec.TestTypes.TestClass5")
    private[ScalaParserSpec] lazy val TestClass6Type = classTypeForName("com.mpc.scalats.core.ScalaParserSpec.TestTypes.TestClass6")
    private[ScalaParserSpec] lazy val TestClass7Type = classTypeForName("com.mpc.scalats.core.ScalaParserSpec.TestTypes.TestClass7")
    private[ScalaParserSpec] lazy val TestTraitWithCompanionObjectType = objectTypeForName("com.mpc.scalats.core.ScalaParserSpec.TestTypes.TestTraitWithCompanionObject")
    private[ScalaParserSpec] lazy val TestObjectType = objectTypeForName("com.mpc.scalats.core.ScalaParserSpec.TestTypes.TestTraitWithCompanionObject.Foo")

    private def classTypeForName(name: String): Type = mirror.staticClass(name).toType

    private def objectTypeForName(name: String): Type = mirror.staticModule(name).info

    trait TestClass1 {
      val name: String
    }

    case class TestClass1B(foo: String)

    case class TestClass2[T](name: T)

    case class TestClass3[T](name: List[T])

    case class TestClass4[T](name: TestClass3[T])

    case class TestClass5[T](name: Option[T])


    case class TestClass6[T](name: Option[TestClass5[List[Option[TestClass4[String]]]]], age: TestClass3[TestClass2[TestClass1]])

    case class TestClass7[T](name: Either[TestClass1, TestClass1B])

    trait TestTraitWithCompanionObject

    object TestTraitWithCompanionObject {
      val x = 42

      object Foo {
        val y: String = "yyy"
        val z: List[Int] = List(1, 2, 3)
      }

      case class Bar(baz: Int, qux: Int)

      val bar: Bar = Bar(555, 777)
    }

  }
}
