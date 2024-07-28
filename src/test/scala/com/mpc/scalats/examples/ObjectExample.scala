package com.mpc.scalats.examples

import com.mpc.scalats.configuration.Config
import com.mpc.scalats.core.TypeScriptGenerator

sealed abstract class DataSourceActionType(val name: String) {
  override def toString: String = name
}

object DataSourceActionTypeValues {

  case object Read extends DataSourceActionType("read")

  case object Write extends DataSourceActionType("write")


  val values: Seq[DataSourceActionType] = Seq(Read, Write)

  def findValueOf(name: String): Option[DataSourceActionType] =
    DataSourceActionTypeValues.values.find(_.name.equalsIgnoreCase(name))
}

object ObjectExample {

  def main(args: Array[String]): Unit = {
    TypeScriptGenerator.generate(Seq("com.mpc.scalats.examples.DataSourceActionTypeValues"), out = System.out)(Config())
  }

}
