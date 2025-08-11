from langchain.memory import ConversationBufferMemory
from langchain_openai import ChatOpenAI
from langchain_core.runnables import RunnablePassthrough
from langchain_core.output_parsers import StrOutputParser
from prompts.task_assignment_prompts import (
    initial_task_assignment_prompt,
    confirmation_task_assignment_prompt, 
    followup_task_assignment_prompt,
    project_analysis_prompt
)
import os

# Initialize the LLM (using OpenAI here, can be replaced with other providers)
llm = ChatOpenAI(
    model="gpt-4.1",
    temperature=0.7,
    openai_api_key=os.getenv("OPENAI_API_KEY", "your-api-key-here")
)

# Define a string output parser to extract plain text responses from the LLM
output_parser = StrOutputParser()

# 1. Initial task assignment advice chain
def create_initial_assignment_chain():
    # Chains the prompt → LLM → output parsing
    return initial_task_assignment_prompt | llm | output_parser

# 2. Task confirmation advice chain
def create_confirmation_assignment_chain():
    return confirmation_task_assignment_prompt | llm | output_parser

# 3. Task follow-up advice chain
def create_followup_assignment_chain():
    return followup_task_assignment_prompt | llm | output_parser

# 4. Project analysis chain
def create_project_analysis_chain():
    return project_analysis_prompt | llm | output_parser

# Instantiate the chains for external use
initial_assignment_chain = create_initial_assignment_chain()
confirmation_assignment_chain = create_confirmation_assignment_chain()
followup_assignment_chain = create_followup_assignment_chain()
project_analysis_chain = create_project_analysis_chain()
