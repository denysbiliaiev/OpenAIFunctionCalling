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

  val apiKey = "sk-proj-tNBqkHwNartwbfIzSxcWT3BlbkFJQABvHUKtC5rIVpoQwZwa"
  
  val ws: StandaloneWSClient = StandaloneAhcWSClient()
  
  val assistantInstructions = "Use the uploaded knowledge to answer the question. " +
    "If you don't know the answer, just say that you don't know; don't try to make up an answer."

  def createVectorStore(fileIds: Seq[String]): Future[OpenAIObject] = for {
    vectorStore <- ws.url("https://api.openai.com/v1/vector_stores")
      .withHttpHeaders(
        "OpenAI-Beta" -> "assistants=v2",
        "Authorization" -> s"Bearer $apiKey"
      )
      .post(Json.obj(
        "file_ids" -> fileIds
      ))
      .map(res => Json.parse(res.body).as[OpenAIObject])
  } yield vectorStore

  def createAssistant(fileIds: Seq[String]): Future[OpenAIObject] = for {
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
            "type" -> "file_search",
          )
        ),
        "tool_resources" -> Json.obj(
          "file_search" -> Json.obj(
            "vector_stores" -> Seq(Json.obj("file_ids" -> fileIds))  
          )
        )
      ))
      .map(res => {
        print(res.body)
        Json.parse(res.body).as[OpenAIObject]
      })
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
      .map(res => {
        print(res.body)
        Json.parse(res.body).as[OpenAIObject]
      })
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
