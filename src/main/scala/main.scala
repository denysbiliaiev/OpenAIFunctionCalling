import play.api.libs.json.Json

import scala.concurrent.ExecutionContext

@main
def main(): Unit = {
  implicit val ec: ExecutionContext = ExecutionContext.Implicits.global

  val messagesToVerstedsRegister = Seq(
    Json.obj(
      "role" -> "user",
      "content" -> "How many companies are registered?"
    ),
    Json.obj(
      "role" -> "user",
      "content" -> "How many different certifications exist?"
    ),
    Json.obj(
      "role" -> "user",
      "content" -> "Which certification is the most common?"
    )
  )

  val messagesToBrønnøysundRegistrene = Seq(
      Json.obj(
        "role" -> "user",
        "content" -> "How many companies are missing websites?"
      ),
      Json.obj(
        "role" -> "user",
        "content" -> "What is the most common first name of a CEO?"
      ),
      Json.obj(
        "role" -> "user",
        "content" -> "What was the most popular year to incorporate a workshop?"
      )
    )

  openAI
    .run(messagesToVerstedsRegister)
    .map(res => openAI.run(messagesToBrønnøysundRegistrene))
    
}
