package com.mpc.scalats

import com.mpc.scalats.CLIOpts._
import com.mpc.scalats.configuration.Config
import com.mpc.scalats.core.TypeScriptGenerator

import java.io.{File, PrintStream}
import scala.util.Try

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
      stringWithSingleQuotes = options contains StringSingleQuotes,
      indentSize = options get IndentSize getOrElse IndentSize.Default,
      emitSemicolons = options contains EmitSemicolons,
      optionToNullable = options contains OptionToNullable,
      optionToUndefined = options contains OptionToUndefined,
      prependIPrefix = options contains PrependIPrefix,
      emitInterfacesAsTypes = options contains TraitToType
    )

    try TypeScriptGenerator.generate(classNames, out = out)(config)
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
      case StringSingleQuotes.key :: restArgs => parseArgs(restArgs) + (StringSingleQuotes -> true)
      case IndentSize.key :: PositiveIntExtractor(n) :: restArgs => parseArgs(restArgs) + (IndentSize -> n)
      case EmitSemicolons.key :: restArgs => parseArgs(restArgs) + (EmitSemicolons -> true)
      case key :: _ if key startsWith "-" => printUsage(s"Unknown option $key")
      case classNames => CLIOpts(SrcClassNames -> classNames)
    }
  }

  private object PositiveIntExtractor {
    def unapply(s: String): Option[Int] = Try(s.toInt).toOption.filter(_ > 0)
  }

  private def printUsage(errMsg: String = "") = {
    if (errMsg.nonEmpty) Console.println(s"ERROR: $errMsg")
    Console.println(
      s"""
         |Usage: java com.mpc.scalats.Main
         |        [${OutFile.key} out.ts]\t\t\t# output file
         |        [${OptionToNullable.key}]  \t# emit `Option` as `nullable`
         |        [${TraitToType.key}]       \t# emit `trait` as `type`
         |        [${PrependIPrefix.key}]    \t# prefix interface names with `I`
         |        [${StringSingleQuotes.key}]\t# use single quotes for strings
         |        [${IndentSize.key}=N]    \t\t# code indent size (default ${IndentSize.Default})
         |        [${EmitSemicolons.key}]  \t\t# emit semicolons
         |        class_name [class_name ...]"""
        .stripMargin)
    sys.exit(1)
  }
}
