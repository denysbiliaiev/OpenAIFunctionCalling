import play.api.libs.json.{Json, Reads}

case class OpenAIMessageContentText(value: String)

implicit val openAIMessageContentTextReads: Reads[OpenAIMessageContentText] = Json.reads[OpenAIMessageContentText]

case class OpenAIMessageContent(text: OpenAIMessageContentText)

implicit val openAIMessageContentReads: Reads[OpenAIMessageContent] = Json.reads[OpenAIMessageContent]

case class OpenAIMessage(content: Array[OpenAIMessageContent])

implicit val openAIMessageReads: Reads[OpenAIMessage] = Json.reads[OpenAIMessage]

case class OpenAIMessages(first_id: String, last_id: String)

implicit val openAIMessagesReads: Reads[OpenAIMessages] = Json.reads[OpenAIMessages]

case class OpenAIToolCallsFunction(name: String, arguments: String)

implicit val openAIToolCallsFunctionReads: Reads[OpenAIToolCallsFunction] = Json.reads[OpenAIToolCallsFunction]

case class OpenAIToolCall(id: String, `type`: String, function: OpenAIToolCallsFunction)

implicit val openAIToolCallReads: Reads[OpenAIToolCall] = Json.reads[OpenAIToolCall]

case class OpenAIToolCalls(tool_calls: Array[OpenAIToolCall])

implicit val openAIToolCallsReads: Reads[OpenAIToolCalls] = Json.reads[OpenAIToolCalls]

case class OpenAIRunRequiredAction(`type`: String, submit_tool_outputs: OpenAIToolCalls)

implicit val openAIRunRequiredActionReads: Reads[OpenAIRunRequiredAction] = Json.reads[OpenAIRunRequiredAction]

case class OpenAIRun(id: String, status: String, required_action: OpenAIRunRequiredAction = null)

implicit val openAIRunReads: Reads[OpenAIRun] = Json.reads[OpenAIRun]

case class OpenAISubmitToolsOutputs(id: String, status: String)

implicit val openAISubmitToolsOutputsReads: Reads[OpenAISubmitToolsOutputs] = Json.reads[OpenAISubmitToolsOutputs]

case class OpenAIObject(id: String)

implicit val openAIObjectReads: Reads[OpenAIObject] = Json.reads[OpenAIObject]