package com.mpc.scalats

import java.io.{ByteArrayOutputStream, PrintStream}

trait StreamFixture {

  def withPrintStreamAsUTF8String(body: PrintStream => Unit): String = {
    val baos = new ByteArrayOutputStream
    body(new PrintStream(baos))
    baos.toString("UTF-8")
  }

}
