import akka.actor.ActorSystem
import akka.stream.Materializer
import akka.stream.scaladsl.{FileIO, Source}
import io.cequence.openaiscala.service.ws.{FilePart, MultipartFormData, MultipartWritable}
import play.api.libs.json.*
import play.api.libs.ws.StandaloneWSClient
import play.api.libs.ws.StandaloneWSRequest
import play.api.libs.ws.ahc.StandaloneAhcWSClient
import play.api.libs.ws.ahc.StandaloneAhcWSRequest

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}
import play.api.libs.ws.JsonBodyReadables.*
import play.api.libs.ws.JsonBodyWritables.*

import java.io.File
import java.nio.file.Paths

case class OpenAIToolCallsFunction(name: String, arguments: String)

implicit val openAIToolCallsFunctionReads: Reads[OpenAIToolCallsFunction] = Json.reads[OpenAIToolCallsFunction]

case class OpenAIToolCall(id: String, `type`: String, function: OpenAIToolCallsFunction)

implicit val openAIToolCallReads: Reads[OpenAIToolCall] = Json.reads[OpenAIToolCall]

case class OpenAIToolCalls(tool_calls: Array[OpenAIToolCall])

implicit val openAIToolCallsReads: Reads[OpenAIToolCalls] = Json.reads[OpenAIToolCalls]

case class OpenAIRunRequiredAction(`type`: String, submit_tool_outputs: OpenAIToolCalls)

implicit val openAIRunRequiredActionReads: Reads[OpenAIRunRequiredAction] = Json.reads[OpenAIRunRequiredAction]

case class OpenAIRun(id: String, status: String, required_action: OpenAIRunRequiredAction)

implicit val openAIRunReads: Reads[OpenAIRun] = Json.reads[OpenAIRun]

case class OpenAISubmitToolsOutputs(id: String, status: String)

implicit val openAISubmitToolsOutputsReads: Reads[OpenAISubmitToolsOutputs] = Json.reads[OpenAISubmitToolsOutputs]

case class OpenAIObject(id: String)

implicit val openAIObjectReads: Reads[OpenAIObject] = Json.reads[OpenAIObject]

object openAIClient {
  implicit val system: ActorSystem = ActorSystem()
  implicit val materializer: Materializer = Materializer(system)
  implicit val ec: ExecutionContext = ExecutionContext.Implicits.global

  val apiKey = ""
  
  val ws: StandaloneWSClient = StandaloneAhcWSClient()
  
  val assistantInstructions = "You are assistant in analyzing company data. " +
    "I will work with you to choose right approaches for data analysis. " +
    "Return response in JSON format. Never add other text to a reply. " +
    "Answer immediately if you don't have an answer. " +
    "If you answer correctly, I will buy you ice cream."

  def createAssistant: Future[OpenAIObject] = for {
    assistant <- ws.url("https://api.openai.com/v1/assistants")
      .withHttpHeaders(
        "OpenAI-Beta" -> "assistants=v2",
        "Authorization" -> s"Bearer $apiKey",
      )
      .post(Json.obj(
        "model" -> "gpt-3.5-turbo-0125",
        "name" -> "Workshop Data Analyst.",
        "instructions" -> assistantInstructions,
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
  
  def createThread: Future[OpenAIObject] = for {
    thread <- ws.url("https://api.openai.com/v1/threads")
      .withHttpHeaders(
        "OpenAI-Beta" -> "assistants=v2",
        "Authorization" -> s"Bearer $apiKey"
      )
      .post(Json.obj())
      .map(res => Json.parse(res.body).as[OpenAIObject])
  } yield thread

  def sendMessage(threadId: String, message: String): Future[OpenAIObject] = for {
    message <- ws.url(s"https://api.openai.com/v1/threads/$threadId/messages")
      .withHttpHeaders(
        "OpenAI-Beta" -> "assistants=v2",
        "Authorization" -> s"Bearer $apiKey"
      )
      .post(Json.obj("role" -> "user", "content" -> message))
      .map(res => Json.parse(res.body).as[OpenAIObject])
  } yield message

  def listMessage(threadId: String): Future[OpenAIObject] = for {
    message <- ws.url(s"https://api.openai.com/v1/threads/$threadId/messages")
      .withHttpHeaders(
        "OpenAI-Beta" -> "assistants=v2",
        "Authorization" -> s"Bearer $apiKey"
      )
      .get()
      .map(res => {
        print("---------------------------------")
        print(res.body)
        OpenAIObject("1")
      })
  } yield message

  def run(assistantId: String, threadId: String): Future[OpenAIObject] = for {
    execRun <- ws.url(s"https://api.openai.com/v1/threads/$threadId/runs")
      .withHttpHeaders(
        "OpenAI-Beta" -> "assistants=v2",
        "Authorization" -> s"Bearer $apiKey"
      )
      .post(Json.obj(
        "assistant_id" -> assistantId,
        "parallel_tool_calls" -> false
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
      .map(res => Json.parse(res.body).as[OpenAIRun])
  } yield runStatus

  def submitToolsOutputs(threadId: String, runId: String, toolCallId: String, output: String): Future[OpenAISubmitToolsOutputs] = for {
    toolsOutputs <- ws.url(s"https://api.openai.com/v1/threads/$threadId/runs/$runId/submit_tool_outputs")
      .withHttpHeaders(
        "OpenAI-Beta" -> "assistants=v2",
        "Authorization" -> s"Bearer $apiKey"
      )
      .post(Json.obj("tool_outputs" -> Seq(
        Json.obj(
          "tool_call_id" -> toolCallId,
          "output" -> output
        )
      )))
      .map(res => Json.parse(res.body).as[OpenAISubmitToolsOutputs])
  } yield toolsOutputs

  def runSteps(threadId: String, runId: String): Future[OpenAIRun] = for {
    runStatus <- ws.url(s"https://api.openai.com/v1/threads/$threadId/runs/$runId/steps")
      .withHttpHeaders(
        "OpenAI-Beta" -> "assistants=v2",
        "Authorization" -> s"Bearer $apiKey"
      )
      .get()
      .map(res => Json.parse(res.body).as[OpenAIRun])
  } yield runStatus
}
