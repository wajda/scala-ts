package com.mpc.scalats

import java.io.{File, PrintStream}

import com.mpc.scalats.configuration.Config
import com.mpc.scalats.core.TypeScriptGenerator

object Main {

  sealed abstract class OptKey(val key: String)

  case object SrcClassNames extends OptKey(null)

  case object OutFile extends OptKey("--out")

  case object EmitInterfaces extends OptKey("--emit-interfaces")

  case object EmitClasses extends OptKey("--emit-classes")

  case object OptionToNullable extends OptKey("--option-to-nullable")

  case object OptionToUndefined extends OptKey("--option-to-undefined")

  def main(args: Array[String]) {
    val options = parseArgs(args.toList)

    val classNames: List[String] = options get SrcClassNames match {
      case Some(cns: List[String]) => cns
      case _ => printUsage()
    }

    val out: PrintStream = options get OutFile match {
      case None => System.out
      case Some(outFile: File) =>
        outFile.getAbsoluteFile.getParentFile.mkdirs()
        outFile.delete()
        new PrintStream(outFile)
    }

    val config = Config(
      emitInterfaces = options contains EmitInterfaces,
      emitClasses = options contains EmitClasses,
      optionToNullable = options contains OptionToNullable,
      optionToUndefined = options contains OptionToUndefined
    )

    try TypeScriptGenerator.generateFromClassNames(classNames, out = out)(config)
    finally out.close()
  }

  private def parseArgs(args: List[String]): Map[OptKey, Any] = {
    args match {
      case Nil => Map.empty
      case OutFile.key :: outFileName :: restArgs => parseArgs(restArgs) + (OutFile -> new File(outFileName))
      case EmitInterfaces.key :: restArgs => parseArgs(restArgs) + (EmitInterfaces -> true)
      case EmitClasses.key :: restArgs => parseArgs(restArgs) + (EmitClasses -> true)
      case OptionToNullable.key :: restArgs => parseArgs(restArgs) + (OptionToNullable -> true)
      case OptionToUndefined.key :: restArgs => parseArgs(restArgs) + (OptionToUndefined -> true)
      case key :: _ if key startsWith "-" => printUsage(s"Unknown option $key")
      case classNames => Map(SrcClassNames -> classNames)
    }
  }

  private def printUsage(errMsg: String = "") = {
    if (errMsg.nonEmpty) Console.println(s"ERROR: $errMsg")
    Console.println(
      s"""
         |Usage: java com.mpc.scalats.Main
         |        [${OutFile.key} out.ts]
         |        [${EmitInterfaces.key}]
         |        [${EmitClasses.key}]
         |        [${OptionToNullable.key}]
         |        [${OptionToUndefined.key}]
         |        class_name [class_name ...]"""
        .stripMargin)
    sys.exit(1)
  }
}
