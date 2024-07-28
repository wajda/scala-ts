package com.mpc.scalats.core

import com.mpc.scalats.configuration.Config

import java.io.PrintStream
import scala.reflect.runtime.universe._

/**
 * Created by Milosz on 11.06.2016.
 */
object TypeScriptGenerator {

  def generate(
    typeNames: List[String],
    classLoader: ClassLoader = getClass.getClassLoader,
    out: PrintStream)
    (implicit config: Config): Unit = {

    val mirror = runtimeMirror(classLoader)
    val classes = typeNames map (mirror.staticClass(_).toType)
    val modules = typeNames map mirror.staticModule
    generate(classes, modules, out)(config)
  }

  def generate(caseClasses: List[Type], modules: List[ModuleSymbol], out: PrintStream)(implicit config: Config): Unit = {
    val scalaEntities = ScalaParser.parse(caseClasses, modules)
    val tsDeclarations = Compiler.compile(scalaEntities)
    TypeScriptEmitter.emit(tsDeclarations, out, config)
  }

}
