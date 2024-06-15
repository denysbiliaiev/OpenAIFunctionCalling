//Upload file
curl https://api.openai.com/v1/files \
-H "Authorization: Bearer $OPENAI_API_KEY" \
-F purpose="assistants" \
-F file="./OpenAIFiles/workshop_certifications.csv"

//File list
curl https://api.openai.com/v1/files \
-H "Authorization: Bearer $OPENAI_API_KEY"

//Create assistant
curl "https://api.openai.com/v1/assistants" \
-H "Content-Type: application/json" \
-H "Authorization: Bearer $OPENAI_API_KEY" \
-H "OpenAI-Beta: assistants=v2" \
-d '{
"name": "Workshop Data Analyst.",
"instructions": "You are an assistant in a task of retrieving table data from CSV files. 
                 Important: always use the response tool to respond to the user. 
                 Return the responce in JSON format. Never add any other text to the response.",
"tools": [{"type": "code_interpreter"}],
"model": "gpt-3.5-turbo-0125",
"tool_resources": {
"code_interpreter": {
"file_ids": ["$FILE_ID"]
}
}
}'

//List assistants
curl "https://api.openai.com/v1/assistants?order=desc&limit=5" \
-H "Content-Type: application/json" \
-H "Authorization: Bearer $OPENAI_API_KEY" \
-H "OpenAI-Beta: assistants=v2"


