package com.mpc.scalats.core

import com.mpc.scalats.configuration.Config

import java.io.PrintStream
import scala.reflect.runtime.universe._

/**
 * Created by Milosz on 11.06.2016.
 */
object TypeScriptGenerator {

  def generateFromClassNames(
    classNames: List[String],
    classLoader: ClassLoader = getClass.getClassLoader,
    out: PrintStream)
    (implicit config: Config): Unit = {

    val mirror = runtimeMirror(classLoader)
    val types = classNames map (mirror.staticClass(_).toType)
    generate(types, out)(config)
  }

  def generate(caseClasses: List[Type], out: PrintStream)(implicit config: Config): Unit = {
    val scalaEntities = ScalaParser.parseCaseClasses(caseClasses)
    val tsDeclarations = Compiler.compile(scalaEntities)
    TypeScriptEmitter.emit(tsDeclarations, out, config)
  }

}
