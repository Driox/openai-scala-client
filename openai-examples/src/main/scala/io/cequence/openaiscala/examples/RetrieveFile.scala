package io.cequence.openaiscala.examples

import scala.concurrent.Future

object RetrieveFile extends Example {

  override protected def run: Future[?] =
    for {
      assistant <- service.retrieveFile("file-xyz")
    } yield {
      println(assistant)
    }

}
