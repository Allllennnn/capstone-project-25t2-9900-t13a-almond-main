from fastapi import APIRouter, HTTPException
from pojo.task_assignment_models import ConversationRequest, ConversationHistoryRequest
from services.conversation_service import ConversationService
from services.agent_service import get_conversational_response
from datetime import datetime
from typing import List, Dict, Any
from pydantic import BaseModel


class ChatRequest(BaseModel):
    user_message: str
    task_id: int
    group_id: int
    task_name: str = ""
    task_description: str = ""
    group_name: str = ""
    conversation_history: List[Dict[str, Any]] = []


router = APIRouter()
conversation_service = ConversationService()


@router.post("/chat")
async def chat_with_agent(req: ChatRequest):
    """Conversational endpoint – generate a response based on conversation history and task context."""
    try:
        # Call AI service to get a smart reply
        ai_response = get_conversational_response(
            task_id=req.task_id,
            group_id=req.group_id,
            user_message=req.user_message,
            conversation_history=req.conversation_history,
            task_context={
                "task_name": req.task_name,
                "task_description": req.task_description,
                "group_name": req.group_name
            }
        )

        return {
            "status": "success",
            "response": ai_response
        }
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Error processing chat message: {e}")


@router.post("/send-message")
async def send_message(req: ConversationRequest):
    """Send a user message into the conversation and get an AI reply."""
    try:
        # Add the user's message to the conversation history
        conversation_service.add_message(
            task_id=req.task_id,
            group_id=req.group_id,
            sender_type="USER",
            sender_id=req.sender_id,
            content=req.message
        )

        # Retrieve the updated conversation history
        history = conversation_service.get_conversation_history(req.task_id, req.group_id)

        # Call AI service to get a reply
        ai_response = get_conversational_response(
            task_id=req.task_id,
            group_id=req.group_id,
            user_message=req.message,
            conversation_history=history
        )

        # Add the AI's reply to the conversation history
        conversation_service.add_message(
            task_id=req.task_id,
            group_id=req.group_id,
            sender_type="AGENT",
            sender_id=None,
            content=ai_response
        )

        return {
            "status": "success",
            "response": ai_response
        }
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Error processing message: {e}")


@router.post("/add-agent-message")
async def add_agent_message(task_id: int, group_id: int, content: str):
    """Internal endpoint to add an agent-generated message."""
    try:
        conversation_service.add_message(
            task_id=task_id,
            group_id=group_id,
            sender_type="AGENT",
            sender_id=None,
            content=content
        )

        return {
            "status": "success",
            "message": "Agent message added successfully"
        }
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Error adding agent message: {e}")


@router.get("/history")
async def get_conversation_history(task_id: int, group_id: int):
    """Retrieve the full conversation history for a given task and group."""
    try:
        messages = conversation_service.get_conversation_history(task_id, group_id)
        return {
            "status": "success",
            "messages": messages
        }
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Error retrieving conversation history: {e}")


@router.delete("/clear")
async def clear_conversation(task_id: int, group_id: int):
    """Clear all conversation messages (optional feature)."""
    try:
        conversation_service.clear_conversation(task_id, group_id)
        return {
            "status": "success",
            "message": "Conversation cleared successfully"
        }
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Error clearing conversation: {e}")
