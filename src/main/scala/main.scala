import openAIClient.{checkRunStatus, createAssistant, createThread, listMessage, run, sendMessage, submitToolsOutputs}
import akka.actor.ActorSystem
import akka.actor.ActorSystem
import akka.stream.Materializer
import akka.stream.scaladsl.{FileIO, Source}
import com.github.tototoshi.csv.*
import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json.{Format, JsError, JsObject, JsString, JsSuccess, JsValue, Json, OFormat, Reads, __}
import play.api.libs.ws.ahc.StandaloneAhcWSClient
import play.api.libs.ws.{StandaloneWSClient, StandaloneWSRequest, StandaloneWSResponse}
import play.api.libs.ws.JsonBodyReadables.*
import play.api.libs.ws.JsonBodyWritables.*
import statistic.{mostCommonCertification, numberOfDifferentCertifications, numberOfRegistredCompanies}

import java.io.File
import java.net.http.HttpResponse
import java.nio.file.{Files, Path, Paths}
import scala.collection.immutable.ListMap
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Failure, Success}
import java.util as ju
import scala.concurrent.duration.Duration
import scala.language.postfixOps
import scala.util.control.Breaks.break

@main
def main(): Unit = {
  implicit val ec: ExecutionContext = ExecutionContext.Implicits.global
  
  val result = for {
    assistantResponse <- createAssistant
    threadResponse <- createThread
    runResponse <- run(assistantResponse.id, threadResponse.id)
  } yield {
    
    //thread_9iuvs4LUMfBQeGcUqPqDW3ry
    sendMessage(threadResponse.id, "How many companies are registered?")
      .map(res => print(res.id))

    sendMessage(threadResponse.id, "How many different certifications exist?")
      .map(res => print(res.id))
    
    
    while(true) {
      Await.result(Future {
        Thread.sleep(3000)
      }, Duration(3000, "millis"))
      
      checkRunStatus(threadResponse.id, runResponse.id).map(run => {
        print(s"------------------------Run status: " + run.status + "\n")

        if ((Seq("cancelled", "queued", "failed", "completed", "expired").contains(run.status))) {
          listMessage(threadResponse.id).map(res => {
            print(res)
          })
        }
        
        if (run.status == "requires_action" && run.required_action.`type` == "submit_tool_outputs") {
          
          run.required_action.submit_tool_outputs.tool_calls.foreach(openAIToolCall => {
            print(s"------------openAIToolCall ID: " + openAIToolCall.id+ "\n")
            print(s"------------openAIToolCall FUNCTION: " + openAIToolCall.function.name+ "\n")
            
            var output = ""

            if(openAIToolCall.function.name == "numberOfRegistredCompanies") {
              output = numberOfRegistredCompanies
            }

            if (openAIToolCall.function.name == "numberOfDifferentCertifications") {
              output = numberOfDifferentCertifications
            }

            if (openAIToolCall.function.name == "mostCommonCertification") {
              output = mostCommonCertification
            }

            if (openAIToolCall.function.name == "numberOfCompaniesMissingWebsites") {
              output = mostCommonCertification
            }

            if (openAIToolCall.function.name == "mostCommonCEOFirstMame") {
              output = mostCommonCertification
            }

            if (openAIToolCall.function.name == "mostPopularYearWorkshopIncorporate") {
              output = mostCommonCertification
            }

            submitToolsOutputs(threadResponse.id, runResponse.id, openAIToolCall.id, output)
              .map(res => {
                println(s"------------submitToolsOutputs: $res")
              })
          })
        }
      })
    }
  }
}
