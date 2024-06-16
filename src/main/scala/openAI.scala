import openAIClient.{checkRunStatus, createAssistant, createRun, createThread, listMessage, retrieveMessage, submitToolsOutputs}
import play.api.libs.json.{JsObject, Json}

import scala.collection.mutable.ArrayBuffer
import scala.concurrent.ExecutionContext

object openAI {
  implicit val ec: ExecutionContext = ExecutionContext.Implicits.global
  
  def run(messages: Seq[JsObject]) = for {
    assistantResponse <- createAssistant
    threadResponse <- createThread(messages)
    runResponse <- createRun(assistantResponse.id, threadResponse.id)
  } yield {
    println(s"-------------------------RUN ID: " + runResponse.id + "   THREAD ID: " + threadResponse.id + "\n")

    var done = false

    while (!done) {
      Thread.sleep(1000)

      checkRunStatus(threadResponse.id, runResponse.id).map(run => {
        println(s"---------RUN STATUS: " + run.status + "\n")

        if ((Seq("cancelled", "failed", "completed", "expired", "incomplete").contains(run.status))) {
          listMessage(threadResponse.id).map(messages => {
            retrieveMessage(threadResponse.id, messages.first_id)
            done = true
          })
        }

        if (run.status == "requires_action" && run.required_action.`type` == "submit_tool_outputs") {

          val toolOutputs = ArrayBuffer[JsObject]()

          run.required_action.submit_tool_outputs.tool_calls.foreach(toolCall => {
            println(s"---------OPENAI TOOL CALL: " + toolCall.function.name + "\n")
            //runSteps(threadResponse.id, runResponse.id)

            toolOutputs += Json.obj(
              "tool_call_id" -> toolCall.id,
              "output" -> toolCalls.call(toolCall)

            )
          })

          println(s"---------OPENAI TOOL OUTPUTS: " + toolOutputs.toSeq + "\n")

          submitToolsOutputs(threadResponse.id, runResponse.id, toolOutputs.toSeq)
        }
      })
    }
  }
}
