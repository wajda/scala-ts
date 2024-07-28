package com.mpc.scalats.e2e

import com.mpc.scalats.StreamFixture
import com.mpc.scalats.configuration.Config
import com.mpc.scalats.core.TypeScriptGenerator
import com.mpc.scalats.e2e.ComplexTest.NodeDef
import org.scalactic.StringNormalizations._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

object ComplexExample {

  def main(args: Array[String]): Unit = {
    TypeScriptGenerator.generate(Seq(classOf[ComplexTest].getName), out = System.out)(Config())
  }

}

class ComplexTest
  extends AnyFlatSpec
    with Matchers
    with StreamFixture {

  it should "work correctly" in {
    val config = Config()

    val result = withPrintStreamAsUTF8String(out => TypeScriptGenerator.generate(Seq(classOf[NodeDef].getName), out, getClass.getClassLoader)(config))

    result should equal(
      s"""
         |""".stripMargin
    )(after being trimmed)
  }

}

object ComplexTest {

  object ArangoDocument {
    type Id = String
    type Key = String
    type Rev = String
    type Timestamp = Long
  }

  case class IndexDef(fields: Seq[String])

  sealed trait GraphElementDef

  sealed trait CollectionDef {
    def name: String

    def indexDefs: Seq[IndexDef] = Nil

    def numShards: Int = 1

    def shardKeys: Seq[String] = Seq("_key")

    def replFactor: Int = 1

    def initData: Seq[AnyRef] = Nil
  }


  sealed abstract class NodeDef(override val name: String)
    extends GraphElementDef {
    this: CollectionDef =>
  }


  object NodeDef {

    object DataSource extends NodeDef("dataSource") with CollectionDef {

      override def shardKeys: Seq[String] = Seq("uri")

      override def indexDefs: Seq[IndexDef] = Seq(
        IndexDef(Seq("_created")),
        IndexDef(Seq("uri")),
        IndexDef(Seq("name"))
      )
    }

    object ExecutionPlan extends NodeDef("executionPlan") with CollectionDef {
      def id(key: ArangoDocument.Key): ArangoDocument.Id = s"$name/$key"

    }

    object Operation extends NodeDef("operation") with CollectionDef {
      override def indexDefs: Seq[IndexDef] = Seq(
        IndexDef(Seq("_belongsTo")),
        IndexDef(Seq("type")),
        IndexDef(Seq("outputSource")),
        IndexDef(Seq("append"))
      )
    }

    object Progress extends NodeDef("progress") with CollectionDef {
      override def indexDefs: Seq[IndexDef] = Seq(
        IndexDef(Seq("timestamp")),
        IndexDef(Seq("durationNs")),
        IndexDef(Seq("_created")),
        IndexDef(Seq("extra.appId")),
        IndexDef(Seq("execPlanDetails.executionPlanKey")),
        IndexDef(Seq("execPlanDetails.frameworkName")),
        IndexDef(Seq("execPlanDetails.applicationName")),
        IndexDef(Seq("execPlanDetails.dataSourceUri")),
        IndexDef(Seq("execPlanDetails.dataSourceName")),
        IndexDef(Seq("execPlanDetails.dataSourceType")),
        IndexDef(Seq("execPlanDetails.append")))
    }

    object Schema extends NodeDef("schema") with CollectionDef

    object Attribute extends NodeDef("attribute") with CollectionDef

    object Expression extends NodeDef("expression") with CollectionDef

  }
}
