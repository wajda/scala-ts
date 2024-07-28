package com.mpc.scalats.examples

import com.mpc.scalats.configuration.Config
import com.mpc.scalats.core.TypeScriptGenerator

trait Pet {
  val name: String
}

case class Cat(name: String) extends Pet

case class Dog(name: String, weight: BigDecimal) extends Pet

case class Zoo(pets: Seq[Pet])

object TraitExample {

  def main(args: Array[String]): Unit = {
    TypeScriptGenerator.generate(
      Seq(
        classOf[Zoo].getName,
        classOf[Cat].getName,
        classOf[Dog].getName
      ),
      out = System.out)(Config())
  }

}
