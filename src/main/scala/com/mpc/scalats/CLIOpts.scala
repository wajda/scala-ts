package com.mpc.scalats

import com.mpc.scalats.CLIOpts.Key

import java.io.File

/**
 * Created with IntelliJ IDEA.
 * User: awajda
 * Date: 3/7/17
 * Time: 1:46 PM
 */

class CLIOpts(table: Map[Key[_], _]) {
  def +[T](kv: (Key[T], T)) = new CLIOpts(table + kv)

  def get[T](key: Key[T]): Option[T] = (table get key).asInstanceOf[Option[T]]

  def contains(key: Key[_]): Boolean = table contains key
}

object CLIOpts {
  def empty = new CLIOpts(Map.empty)

  def apply[T](kvs: (Key[T], T)*) = new CLIOpts(Map(kvs: _*))

  sealed abstract class Key[T](val key: String)

  case object SrcClassNames extends Key[List[String]](null)

  case object OutFile extends Key[File]("--out")

  case object StringSingleQuotes extends Key[Boolean]("--string-single-quotes")

  case object EmitSemicolons extends Key[Boolean]("--emit-semicolons")

  case class IndentSize(indent: Int = IndentSize.Default)

  object IndentSize extends Key[Int]("--indent-size") {
    val Default = 2
  }

  case object OptionToNullable extends Key[Boolean]("--option-to-nullable")

  case object OptionToUndefined extends Key[Boolean]("--option-to-undefined")

  case object TraitToType extends Key[Boolean]("--trait-to-type")

  case object PrependIPrefix extends Key[Boolean]("--prepend-I-prefix")

}

