package com.mpc.scalats.core

import com.mpc.scalats.configuration.Config

import java.io.PrintStream
import scala.reflect.runtime.universe._

/**
 * Created by Milosz on 11.06.2016.
 */
object TypeScriptGenerator {

  def generate(
    typeNames: Seq[String],
    out: PrintStream,
    classLoader: ClassLoader = getClass.getClassLoader
  )
    (implicit config: Config): Unit = {

    val mirror = runtimeMirror(classLoader)
    val classes = typeNames map (mirror.staticClass(_).toType)
    val modules = typeNames map mirror.staticModule

    val scalaEntities = ScalaParser.parse(classes, modules)
    val tsDeclarations = Compiler.compile(scalaEntities)
    val tsCodeEmitter = new TypeScriptEmitter(config)
    tsCodeEmitter.emit(tsDeclarations, out)
  }
}
