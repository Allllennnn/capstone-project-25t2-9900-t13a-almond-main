import os
from dotenv import load_dotenv
from fastapi import FastAPI
from pydantic import BaseModel
from typing import List, Dict, Any, Optional
import requests
import aiohttp
import asyncio
import json
import logging

# Configure logger
logger = logging.getLogger(__name__)

# Load environment variables from .env
load_dotenv()

# Import updated LangChain components
from langchain_openai import OpenAI
from langchain.prompts import PromptTemplate
from langchain.output_parsers import StructuredOutputParser, ResponseSchema
from langchain.schema.runnable import RunnablePassthrough

# === FastAPI application and LLM configuration ===
app = FastAPI()

# 1. Read OpenAI API Key
api_key = os.getenv("OPENAI_API_KEY")
if not api_key:
    raise ValueError("OPENAI_API_KEY environment variable is not set")

# Java backend address
JAVA_BACKEND_URL = os.getenv("JAVA_BACKEND_URL", "http://localhost:8080")

# 2. Initialize LLM (using OpenAI model)
llm = OpenAI(
    temperature=0.7,             # Slightly higher creativity for chat
    openai_api_key=api_key,
    model="gpt-4o-mini",         # Correct model name
    max_tokens=2000              # Increase max tokens to avoid truncation
)

# Conversation prompt template
conversation_prompt = PromptTemplate(
    input_variables=["conversation_history", "task_context", "user_message"],
    template="""
You are an AI assistant helping students with project management and task assignments.

Task Context:
{task_context}

Previous Conversation:
{conversation_history}

Student Message: {user_message}

Please provide a helpful, informative response. Consider the conversation history and task context when responding.
Be supportive, constructive, and provide specific guidance when possible.

Response:"""
)

# Conversation chain
conversation_chain = conversation_prompt | llm

# Weekly analysis prompt template
weekly_analysis_prompt = PromptTemplate(
    input_variables=[
        "task_content",
        "initial_assignments",
        "weekly_goals",
        "meeting_content",
        "week_no"
    ],
    template="""
As a project management AI assistant, please analyze the group's progress for week {week_no} based on the following information:

**Task Requirements:**
{task_content}

**Initial Group Assignments:**
{initial_assignments}

**Goals for Week {week_no}:**
{weekly_goals}

**Meeting Notes for Week {week_no}:**
{meeting_content}

Please address the following points:
1. **Progress Analysis:** Did each member complete this week's goals as planned?
2. **Work Quality:** Does the completed work meet the requirements?
3. **Collaboration:** Was the team's collaboration smooth and effective?
4. **Issue Identification:** Are there any delays, quality issues, or collaboration obstacles?
5. **Improvement Suggestions:** Provide concrete recommendations to address identified issues.

Please provide a detailed analysis and constructive feedback.
"""
)

# Weekly analysis chain
weekly_analysis_chain = weekly_analysis_prompt | llm

# Define output format for weekly goal generation
weekly_goal_response_schemas = [
    ResponseSchema(name="goal", description="The weekly goal content for the student"),
    ResponseSchema(name="reason", description="The reasoning behind this weekly goal, based on task requirements and previous progress")
]

weekly_goal_output_parser = StructuredOutputParser.from_response_schemas(weekly_goal_response_schemas)
weekly_goal_format_instructions = weekly_goal_output_parser.get_format_instructions()

# Prompt template for first-week goal
first_week_goal_prompt = PromptTemplate(
    input_variables=[
        "student_name",
        "task_content",
        "student_assignment",
        "initial_assignments",
        "format_instructions"
    ],
    template="""
As a project management AI assistant, please generate a specific, achievable weekly goal for week 1 for the following student:

**Student Name:** {student_name}

**Task Requirements:**
{task_content}

**This Student's Assignment:**
{student_assignment}

**All Team Members' Assignments:**
{initial_assignments}

Based on the task requirements and the student's specific assignment, create a concrete, measurable goal that this student should aim to complete in the first week of the project.

{format_instructions}
"""
)

# Prompt template for subsequent-week goals
subsequent_week_goal_prompt = PromptTemplate(
    input_variables=[
        "student_name",
        "task_content",
        "student_assignment",
        "initial_assignments",
        "previous_goals",
        "previous_meeting_content",
        "week_no",
        "format_instructions"
    ],
    template="""
As a project management AI assistant, please generate a specific, achievable weekly goal for week {week_no} for the following student:

**Student Name:** {student_name}

**Task Requirements:**
{task_content}

**This Student's Assignment:**
{student_assignment}

**All Team Members' Assignments:**
{initial_assignments}

**Previous Weekly Goals:**
{previous_goals}

**Previous Meeting Notes:**
{previous_meeting_content}

Based on the task requirements, the student's specific assignment, previous goals, and meeting notes, create a concrete, measurable goal that this student should aim to complete in week {week_no} of the project.

{format_instructions}
"""
)

# Chains for weekly goal generation
first_week_goal_chain = first_week_goal_prompt | llm
subsequent_week_goal_chain = subsequent_week_goal_prompt | llm

# === Request model definitions ===
class WeeklyProgressRequest(BaseModel):
    task_id: int
    week_no: int
    meeting_document_url: str
    meeting_document_content: Optional[str] = None  # Content pre-parsed by Java backend
    task_content: Optional[str] = None              # Task description content
    initial_assignments: Optional[str] = None       # Initial assignments content
    weekly_goals: Optional[str] = None              # Weekly goals content


# === Conversation support function ===
def get_conversational_response(
    task_id: int,
    group_id: int,
    user_message: str,
    conversation_history: List[Dict[str, Any]],
    task_context: Dict[str, str] = None
) -> str:
    """
    Generate an AI response based on conversation history and task context.
    """
    try:
        # Build task context string
        if task_context:
            context_info = (
                f"Project: {task_context.get('task_name', '')}\n"
                f"Description: {task_context.get('task_description', '')}\n"
                f"Team: {task_context.get('group_name', '')}\n"
                f"Task ID: {task_id}, Group ID: {group_id}"
            )
        else:
            context_info = get_task_context(task_id, group_id)

        # Format conversation history
        formatted_history = format_conversation_history(conversation_history)

        # Invoke the conversation chain
        response = conversation_chain.invoke({
            "conversation_history": formatted_history,
            "task_context": context_info,
            "user_message": user_message
        })

        return response.strip()

    except Exception as e:
        print(f"Error generating conversational response: {e}")
        return "I'm sorry, I encountered an error while processing your message. Please try again."


def get_task_context(task_id: int, group_id: int) -> str:
    """
    Retrieve basic task context information.
    """
    try:
        # Optionally call Java backend API to get real task info
        return f"Task ID: {task_id}, Group ID: {group_id}. This is a collaborative project assignment."
    except Exception:
        return "Project management context."


def format_conversation_history(history: List[Dict[str, Any]]) -> str:
    """
    Format conversation history as a string.
    """
    if not history:
        return "No previous conversation."

    lines = []
    for msg in history:
        sender = msg.get('sender', 'Unknown')
        content = msg.get('content', '')
        lines.append(f"{sender}: {content}")
    return "\n".join(lines)


@app.post("/agent/analyze-weekly-progress")
async def analyze_weekly_progress(req: WeeklyProgressRequest):
    """Analyze weekly progress"""
    print(f"=== Starting analysis for task {req.task_id}, week {req.week_no} ===")
    print("=" * 80)
    print("Received request details:")
    print(f"  task_id: {req.task_id}")
    print(f"  week_no: {req.week_no}")
    print(f"  meeting_document_url: {req.meeting_document_url}")
    print(f"  meeting_document_content provided: {req.meeting_document_content is not None}")
    if req.meeting_document_content:
        content = req.meeting_document_content
        print(f"  meeting_document_content length: {len(content)}")
        preview = content[:200] if len(content) > 200 else content
        print(f"  meeting_document_content preview: {preview}")
    print(f"  task_content provided: {req.task_content is not None}")
    if req.task_content:
        content = req.task_content
        print(f"  task_content length: {len(content)}")
        preview = content[:200] if len(content) > 200 else content
        print(f"  task_content preview: {preview}")
    print(f"  initial_assignments provided: {req.initial_assignments is not None}")
    if req.initial_assignments:
        content = req.initial_assignments
        print(f"  initial_assignments length: {len(content)}")
        print(f"  initial_assignments content: {content}")
    print(f"  weekly_goals provided: {req.weekly_goals is not None}")
    if req.weekly_goals:
        content = req.weekly_goals
        print(f"  weekly_goals length: {len(content)}")
        print(f"  weekly_goals content: {content}")
    print("=" * 80)

    try:
        # Use pre-parsed or fetch data as needed
        if req.task_content:
            task_file_content = req.task_content
            print(f"Using provided task_content, length: {len(task_file_content)}")
        else:
            task_file_content = await get_task_file_content(req.task_id)
            print(f"Fetched task file content, length: {len(task_file_content)}")

        if req.initial_assignments:
            initial_assignments = req.initial_assignments
            print(f"Using provided initial_assignments, length: {len(initial_assignments)}")
        else:
            initial_assignments = await get_initial_assignments(req.task_id)
            print(f"Fetched initial_assignments, length: {len(initial_assignments)}")

        if req.weekly_goals:
            weekly_goals = req.weekly_goals
            print(f"Using provided weekly_goals, length: {len(weekly_goals)}")
        else:
            weekly_goals = await get_weekly_goals(req.task_id, req.week_no)
            print(f"Fetched weekly_goals, length: {len(weekly_goals)}")

        # Use meeting_document_content if available
        if req.meeting_document_content:
            meeting_content = req.meeting_document_content
            print(f"Using provided meeting_document_content, length: {len(meeting_content)}")
            preview = meeting_content[:200] if len(meeting_content) > 200 else meeting_content
            print(f"Meeting content preview: {preview}")
        else:
            print(f"No meeting_document_content provided; fetching from URL: {req.meeting_document_url}")
            meeting_content = await get_meeting_document_content(req.meeting_document_url)
            preview = meeting_content[:200] if len(meeting_content) > 200 else meeting_content
            print(f"Fetched meeting content preview: {preview}")

        print("Preparing to invoke LLM for analysis...")
        print(f"  task_content length: {len(task_file_content)}")
        print(f"  initial_assignments length: {len(initial_assignments)}")
        print(f"  weekly_goals length: {len(weekly_goals)}")
        print(f"  meeting_content length: {len(meeting_content)}")

        # Display the full prompt for debugging
        complete_prompt = weekly_analysis_prompt.format(
            task_content=task_file_content,
            initial_assignments=initial_assignments,
            weekly_goals=weekly_goals,
            meeting_content=meeting_content,
            week_no=req.week_no
        )
        print("\n=== Full prompt ===")
        print(complete_prompt)
        print("=== End of prompt ===\n")

        # Confirm the chain being used
        print("=== Chain configuration ===")
        print("Using chain: weekly_analysis_chain")
        print("=== End chain config ===\n")

        # Invoke the chain
        analysis_result = weekly_analysis_chain.invoke({
            "task_content": task_file_content,
            "initial_assignments": initial_assignments,
            "weekly_goals": weekly_goals,
            "meeting_content": meeting_content,
            "week_no": req.week_no
        })

        print(f"LLM analysis complete, result length: {len(analysis_result)}")
        print(f"Analysis result: {analysis_result}")

        return {
            "status": "success",
            "analysis": analysis_result,
            "task_id": req.task_id,
            "week_no": req.week_no
        }

    except Exception as e:
        print(f"Error during analysis: {e}")
    return {
            "status": "error",
            "message": f"Analysis failed: {e}"
        }


async def get_task_file_content(task_id: int) -> str:
    """Fetch the task file content from the Java backend."""
    try:
        async with aiohttp.ClientSession() as session:
            async with session.get(f"{JAVA_BACKEND_URL}/api/tasks/{task_id}") as resp:
                if resp.status == 200:
                    task_data = await resp.json()
                    file_url = task_data.get('data', {}).get('fileUrl')
                    if file_url:
                        async with session.get(file_url) as file_resp:
                            if file_resp.status == 200:
                                return await file_resp.text()
                            else:
                                return f"Failed to download task file: HTTP {file_resp.status}"
                    else:
                        return "No task file attached."
                else:
                    return f"Failed to get task info: HTTP {resp.status}"
    except Exception as e:
        return f"Error fetching task file content: {e}"


async def get_initial_assignments(task_id: int) -> str:
    """Fetch the initial assignments for a task."""
    try:
        async with aiohttp.ClientSession() as session:
            async with session.get(f"{JAVA_BACKEND_URL}/api/student/task-assignments/{task_id}/finalized") as resp:
                if resp.status == 200:
                    data = await resp.json()
                    assignments = data.get('data', [])
                    if assignments:
                        text = ""
                        for a in assignments:
                            name = a.get('memberName', 'Unknown')
                            desc = a.get('description', '')
                            text += f"- {name}: {desc}\n"
                        return text
                    else:
                        return "No initial assignments found."
                else:
                    return f"Failed to fetch assignments: HTTP {resp.status}"
    except Exception as e:
        return f"Error fetching initial assignments: {e}"


async def get_weekly_goals(task_id: int, week_no: int) -> str:
    """Fetch the weekly goals for a given week."""
    try:
        async with aiohttp.ClientSession() as session:
            async with session.get(f"{JAVA_BACKEND_URL}/api/student/tasks/{task_id}/weekly-goals") as resp:
                if resp.status == 200:
                    data = await resp.json()
                    goals = [g for g in data.get('data', []) if g.get('weekNo') == week_no]
                    if goals:
                        text = ""
                        for g in goals:
                            name = g.get('studentName', f"Student {g.get('studentId')}")
                            content = g.get('goal', '')
                            status = g.get('status', '')
                            text += f"- {name} ({status}): {content}\n"
                        return text
                    else:
                        return f"No goals found for week {week_no}."
                else:
                    return f"Failed to fetch weekly goals: HTTP {resp.status}"
    except Exception as e:
        return f"Error fetching weekly goals: {e}"


async def get_meeting_document_content(document_url: str) -> str:
    """Fetch meeting document content from a URL."""
    try:
        async with aiohttp.ClientSession() as session:
            async with session.get(document_url) as resp:
                if resp.status == 200:
                    ctype = resp.headers.get('content-type', '').lower()
                    if 'text' in ctype or 'markdown' in ctype:
                        return await resp.text()
                    elif 'pdf' in ctype:
                        return f"PDF document at {document_url} (parsing not implemented)"
                    elif 'word' in ctype or 'doc' in ctype:
                        return f"Word document at {document_url} (parsing not implemented)"
                    else:
                        return await resp.text()
                else:
                    return f"Failed to download meeting document: HTTP {resp.status}"
    except Exception as e:
        return f"Error fetching meeting document: {e}"


def generate_weekly_goals(
    task_id: int,
    student_id: int,
    week_no: int,
    task_content: str = None,
    task_file_content: str = None,
    initial_assignments: str = None,
    previous_goals: List[Dict[str, Any]] = None,
    previous_meeting_content: str = None
) -> Dict[str, str]:
    """
    Generate a weekly goal and reasoning.

    Args:
        task_id: ID of the task.
        student_id: ID of the student.
        week_no: Week number.
        task_content: Task description content.
        task_file_content: Content of any attached task file.
        initial_assignments: Initial task assignments.
        previous_goals: Goals from previous weeks.
        previous_meeting_content: Content of previous meeting notes.

    Returns:
        A dict with "goal" and "reason" keys.
    """
    import re

    print(f"=== Generating week {week_no} goal for student {student_id} on task {task_id} ===")
    print("=" * 50)

    # Log inputs
    if task_content:
        print(f"task_content length: {len(task_content)}")
        print(f"task_content preview: {task_content[:200]}...")
    else:
        print("No task_content provided")

    if task_file_content:
        print(f"task_file_content length: {len(task_file_content)}")
        print(f"task_file_content preview: {task_file_content[:200]}...")
    else:
        print("No task_file_content provided")

    if initial_assignments:
        print(f"initial_assignments length: {len(initial_assignments)}")
        print(f"initial_assignments: {initial_assignments}")
    else:
        print("No initial_assignments provided")

    if previous_goals:
        print(f"previous_goals count: {len(previous_goals)}")
        for i, g in enumerate(previous_goals[:3]):
            print(f"Goal {i+1}: {json.dumps(g, ensure_ascii=False)}")
        if len(previous_goals) > 3:
            print(f"... and {len(previous_goals)-3} more")
    else:
        print("No previous_goals provided")

    if previous_meeting_content:
        print(f"previous_meeting_content length: {len(previous_meeting_content)}")
        print(f"previous_meeting_content preview: {previous_meeting_content[:200]}...")
    else:
        print("No previous_meeting_content provided")

    print("=" * 50)

    # Combine content
    combined_content = (task_content or "") + "\n\n" + (task_file_content or "")
    combined_content = combined_content.strip() or "No task content available."

    # Determine student's assignment text
    student_name = f"Student {student_id}"
    student_assignment = "No assignment found for this student."
    if initial_assignments:
        for line in initial_assignments.splitlines():
            if f"{student_id}" in line:
                student_assignment = line
                break

    print(f"Student assignment: {student_assignment}")

    try:
        if week_no == 1:
            print("Using first-week goal template")
            params = {
                "student_name": student_name,
                "task_content": combined_content[:2000],
                "student_assignment": student_assignment,
                "initial_assignments": (initial_assignments or "")[:1000],
                "format_instructions": weekly_goal_format_instructions
            }
            prompt = first_week_goal_prompt.format(**params)
            print("="*50)
            print("Full prompt:")
            print(prompt)
            print("="*50)
            result = first_week_goal_chain.invoke(params)
            print(f"LLM raw output: {result}")
        else:
            print("Using subsequent-week goal template")
            formatted_goals = ""
            if previous_goals:
                for g in previous_goals:
                    formatted_goals += f"Week {g.get('weekNo')}: {g.get('goal')} ({g.get('status')})\n"
            else:
                formatted_goals = "No previous goals."

            params = {
                "student_name": student_name,
                "task_content": combined_content[:2000],
                "student_assignment": student_assignment,
                "initial_assignments": (initial_assignments or "")[:1000],
                "previous_goals": formatted_goals[:1000],
                "previous_meeting_content": (previous_meeting_content or "")[:1000],
                "week_no": week_no,
                "format_instructions": weekly_goal_format_instructions
            }
            prompt = subsequent_week_goal_prompt.format(**params)
            print("="*50)
            print("Full prompt:")
            print(prompt)
            print("="*50)
            result = subsequent_week_goal_chain.invoke(params)
            print(f"LLM raw output: {result}")

        # Parse structured output
        try:
            parsed = weekly_goal_output_parser.parse(result)
            print(f"Parsed output: {json.dumps(parsed, ensure_ascii=False)}")
            return parsed
        except Exception as parse_err:
            print(f"Failed to parse LLM output: {parse_err}")
            # Fallback parsing
            lower = result.lower()
            if "goal:" in lower and "reason:" in lower:
                goal_part = lower.split("goal:")[1].split("reason:")[0].strip()
                reason_part = lower.split("reason:")[1].strip()
                return {"goal": goal_part, "reason": reason_part}
            # Attempt JSON extraction
            match = re.search(r'\{.*\}', result, re.DOTALL)
            if match:
                try:
                    data = json.loads(match.group(0))
                    return {
                        "goal": data.get("goal", ""),
                        "reason": data.get("reason", "")
                    }
                except json.JSONDecodeError:
                    print("JSON decode error in fallback")
            # Final fallback
    return {
                "goal": result.strip(),
                "reason": "Generated by AI; no explicit reason parsed."
            }

    except Exception as e:
        print(f"Error generating weekly goal: {e}")
    return {
            "goal": f"Error generating goal: {e}",
            "reason": "An error occurred during goal generation."
    }
