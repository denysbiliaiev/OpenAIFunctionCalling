import akka.actor.ActorSystem
import akka.stream.Materializer
import com.typesafe.config.ConfigFactory
import play.api.libs.json.{JsObject, Json}
import play.api.libs.ws.JsonBodyReadables.*
import play.api.libs.ws.JsonBodyWritables.*
import play.api.libs.ws.StandaloneWSClient
import play.api.libs.ws.ahc.StandaloneAhcWSClient

import scala.concurrent.{ExecutionContext, Future}

object openAIClient {
  implicit val system: ActorSystem = ActorSystem()
  implicit val materializer: Materializer = Materializer(system)
  implicit val ec: ExecutionContext = ExecutionContext.Implicits.global
  var conf = ConfigFactory.load
  val apiKey = "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"

  val ws: StandaloneWSClient = StandaloneAhcWSClient()
  
  val assistantInstructions = "Use function to answer the question. " +
    "Response must be in JSON format."

  def createAssistant: Future[OpenAIObject] = for {
    assistant <- ws.url("https://api.openai.com/v1/assistants")
      .withHttpHeaders(
        "OpenAI-Beta" -> "assistants=v2",
        "Authorization" -> s"Bearer $apiKey",
      )
      .post(Json.obj(
        "model" -> "gpt-4o",
        "name" -> "Workshop Data Analyst.",
        "instructions" -> assistantInstructions,
        "response_format" -> Json.obj(
          "type" -> "json_object"
        ),
        "tools" -> Seq(
          Json.obj(
            "type" -> "function",
            "function" -> Json.obj(
              "name" -> "numberOfRegistredCompanies",
              "description" -> "Return number of registered companies.",
              "parameters" -> Json.obj()
            )
          ),
          Json.obj(
            "type" -> "function",
            "function" -> Json.obj(
              "name" -> "numberOfDifferentCertifications",
              "description" -> "Return number of different certifications.",
              "parameters" -> Json.obj()
            )
          ),
          Json.obj(
            "type" -> "function",
            "function" -> Json.obj(
              "name" -> "mostCommonCertification",
              "description" -> "Return number the most common certification.",
              "parameters" -> Json.obj()
            )
          ),
          Json.obj(
            "type" -> "function",
            "function" -> Json.obj(
              "name" -> "numberOfCompaniesMissingWebsites",
              "description" -> "Return number of companies are missing websites",
              "parameters" -> Json.obj()
            )
          ),
          Json.obj(
            "type" -> "function",
            "function" -> Json.obj(
              "name" -> "mostCommonCEOFirstMame",
              "description" -> "Most common first name of a CEO in a certified workshop",
              "parameters" -> Json.obj()
            )
          ),
          Json.obj(
            "type" -> "function",
            "function" -> Json.obj(
              "name" -> "mostPopularYearWorkshopIncorporate",
              "description" -> "Most popular year to incorporate a workshop",
              "parameters" -> Json.obj()
            )
          )
        )
      ))
      .map(res => Json.parse(res.body).as[OpenAIObject])
  } yield assistant
  
  def createThread(messages: Seq[JsObject]): Future[OpenAIObject] = for {
    thread <- ws.url("https://api.openai.com/v1/threads")
      .withHttpHeaders(
        "OpenAI-Beta" -> "assistants=v2",
        "Authorization" -> s"Bearer $apiKey"
      )
      .post(Json.obj(
        "messages" -> messages
      ))
      .map(res => Json.parse(res.body).as[OpenAIObject])
  } yield thread

  def createRun(assistantId: String, threadId: String): Future[OpenAIObject] = for {
    execRun <- ws.url(s"https://api.openai.com/v1/threads/$threadId/runs")
      .withHttpHeaders(
        "OpenAI-Beta" -> "assistants=v2",
        "Authorization" -> s"Bearer $apiKey"
      )
      .post(Json.obj(
        "assistant_id" -> assistantId,
        "tool_choice" -> "required",
        //"parallel_tool_calls" -> false
      ))
      .map(res => Json.parse(res.body).as[OpenAIObject])
  } yield execRun

  def checkRunStatus(threadId: String, runId: String): Future[OpenAIRun] = for {
    runStatus <- ws.url(s"https://api.openai.com/v1/threads/$threadId/runs/$runId")
      .withHttpHeaders(
        "OpenAI-Beta" -> "assistants=v2",
        "Authorization" -> s"Bearer $apiKey"
      )
      .get()
      .map(res => {
        Json.parse(res.body).as[OpenAIRun]
      })
  } yield runStatus

  def submitToolsOutputs(threadId: String, runId: String, toolOutputs: Seq[JsObject]): Future[OpenAISubmitToolsOutputs] = for {
    toolsOutputs <- ws.url(s"https://api.openai.com/v1/threads/$threadId/runs/$runId/submit_tool_outputs")
      .withHttpHeaders(
        "OpenAI-Beta" -> "assistants=v2",
        "Authorization" -> s"Bearer $apiKey"
      )
      .post(Json.obj("tool_outputs" -> toolOutputs))
      .map(res => Json.parse(res.body).as[OpenAISubmitToolsOutputs])
  } yield toolsOutputs

  def listMessage(threadId: String): Future[OpenAIMessages] = for {
    messages <- ws.url(s"https://api.openai.com/v1/threads/$threadId/messages")
      .withHttpHeaders(
        "OpenAI-Beta" -> "assistants=v2",
        "Authorization" -> s"Bearer $apiKey"
      )
      .get()
      .map(res => Json.parse(res.body).as[OpenAIMessages])
  } yield messages

  def retrieveMessage(threadId: String, messageId: String): Future[OpenAIMessage] = for {
    message <- ws.url(s"https://api.openai.com/v1/threads/$threadId/messages/$messageId")
      .withHttpHeaders(
        "OpenAI-Beta" -> "assistants=v2",
        "Authorization" -> s"Bearer $apiKey"
      )
      .get()
      .map(res => {
        println("---------ASSISTANT MESSAGE: " + Json.parse(res.body).as[OpenAIMessage].content(0).text.value)
        Json.parse(res.body).as[OpenAIMessage]
      })
  } yield message
}
