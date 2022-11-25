package com.mpc.scalats

import java.io.{File, PrintStream}

import com.mpc.scalats.configuration.Config
import com.mpc.scalats.core.TypeScriptGenerator
import CLIOpts._

object CLI {

  def main(args: Array[String]): Unit = {
    val options = parseArgs(args.toList)

    val classNames: List[String] = options get SrcClassNames match {
      case Some(cns) => cns
      case _ => printUsage()
    }

    val out: PrintStream = options get OutFile match {
      case None => System.out
      case Some(outFile) =>
        outFile.getAbsoluteFile.getParentFile.mkdirs()
        outFile.delete()
        new PrintStream(outFile)
    }

    val config = Config(
      optionToNullable = options contains OptionToNullable,
      optionToUndefined = options contains OptionToUndefined,
      prependIPrefix = options contains PrependIPrefix,
      emitInterfacesAsTypes = options contains TraitToType
    )

    try TypeScriptGenerator.generateFromClassNames(classNames, out = out)(config)
    finally out.close()
  }

  private def parseArgs(args: List[String]): CLIOpts = {
    args match {
      case Nil => CLIOpts.empty
      case OutFile.key :: outFileName :: restArgs => parseArgs(restArgs) + (OutFile -> new File(outFileName))
      case OptionToNullable.key :: restArgs => parseArgs(restArgs) + (OptionToNullable -> true)
      case OptionToUndefined.key :: restArgs => parseArgs(restArgs) + (OptionToUndefined -> true)
      case TraitToType.key :: restArgs => parseArgs(restArgs) + (TraitToType -> true)
      case PrependIPrefix.key :: restArgs => parseArgs(restArgs) + (PrependIPrefix -> true)
      case key :: _ if key startsWith "-" => printUsage(s"Unknown option $key")
      case classNames => CLIOpts(SrcClassNames -> classNames)
    }
  }

  private def printUsage(errMsg: String = "") = {
    if (errMsg.nonEmpty) Console.println(s"ERROR: $errMsg")
    Console.println(
      s"""
         |Usage: java com.mpc.scalats.Main
         |        [${OutFile.key} out.ts]    # output file
         |        [${OptionToNullable.key}]  # emit `Option` as `nullable`
         |        [${TraitToType.key}]       # emit `trait` as `type`
         |        [${PrependIPrefix.key}]    # prefix interface names with `I`
         |        class_name [class_name ...]"""
        .stripMargin)
    sys.exit(1)
  }
}
