package com.mpc.scalats.examples

import com.mpc.scalats.configuration.Config
import com.mpc.scalats.core.TypeScriptGenerator

import java.time.{Instant, LocalDate}
import java.util.UUID

case class BookDto(title: String, pageCount: Int)

case class AddressDto(
  street: String,
  city: String
)

case class AuthorDto(
  id: UUID,
  name: String,
  age: Option[Int],
  address: AddressDto,
  nicknames: List[String],
  workAddress: Option[AddressDto],
  principal: AuthorDto,
  books: List[Option[BookDto]],
  creationDate: Instant,
  birthday: LocalDate,
  isRetired: Boolean
)

object BasicExample {

  def main(args: Array[String]): Unit = {
    TypeScriptGenerator.generate(List(classOf[AuthorDto].getName), out = System.out)(Config())
  }

}
