from fastapi import APIRouter, HTTPException
from pydantic import BaseModel
from typing import List, Dict, Any, Optional
import httpx
import aiofiles
import os
import re
from services.agent_service import weekly_analysis_chain

router = APIRouter()


class WeeklyProgressRequest(BaseModel):
    task_id: int
    week_no: int
    meeting_document_url: str
    task_content: str = "Task content not provided"
    initial_assignments: str = "Initial assignments not provided"
    weekly_goals: str = "Weekly goals not provided"
    meeting_document_content: Optional[str] = None  # Optional: content parsed by Java backend


@router.post("/analyze-weekly-progress")
async def analyze_weekly_progress(req: WeeklyProgressRequest):
    """Analyze weekly progress"""
    print(f"=== Starting analysis of task {req.task_id}, week {req.week_no} ===")

    # Log all received request data
    print("Received request data:")
    print(f"  task_id: {req.task_id}")
    print(f"  week_no: {req.week_no}")
    print(f"  meeting_document_url: {req.meeting_document_url}")
    print(f"  meeting_document_content exists: {req.meeting_document_content is not None}")
    if req.meeting_document_content:
        print(f"  meeting_document_content length: {len(req.meeting_document_content)}")
        preview = req.meeting_document_content[:200] if len(
            req.meeting_document_content) > 200 else req.meeting_document_content
        print(f"  meeting_document_content preview: {preview}")
    print(f"  task_content length: {len(req.task_content)}")
    print(f"  initial_assignments length: {len(req.initial_assignments)}")
    print(f"  weekly_goals length: {len(req.weekly_goals)}")

    try:
        # Use data provided by Java backend
        task_file_content = req.task_content
        print(f"Using Java-provided task content, length: {len(task_file_content)}")

        initial_assignments = req.initial_assignments
        print(f"Using Java-provided initial assignments, length: {len(initial_assignments)}")

        weekly_goals = req.weekly_goals
        print(f"Using Java-provided weekly goals, length: {len(weekly_goals)}")

        # Prefer Java-parsed meeting document content if available
        if req.meeting_document_content:
            meeting_content = req.meeting_document_content
            print(f"Using Java-parsed meeting content, length: {len(meeting_content)}")
            preview = meeting_content[:200] if len(meeting_content) > 200 else meeting_content
            print(f"Meeting content preview: {preview}")
        else:
            # Fallback: fetch document from URL
            print(f"No Java-parsed content provided; fetching from URL: {req.meeting_document_url}")
            meeting_content = await get_meeting_document_content(req.meeting_document_url)
            preview = meeting_content[:200] if len(meeting_content) > 200 else meeting_content
            print(f"Fetched meeting content preview: {preview}")

        # Invoke the LLM with the appropriate chain and prompts
        print("=== Invoking LLM for analysis ===")
        print("Using weekly_analysis_chain...")
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


async def get_meeting_document_content(document_url: str) -> str:
    """Retrieve meeting document content from a URL"""
    try:
        async with httpx.AsyncClient() as client:
            response = await client.get(document_url)
            print(f"Meeting document download status: {response.status_code}")

            if response.status_code == 200:
                content_type = response.headers.get('content-type', '').lower()
                print(f"Meeting document content-type: {content_type}")

                # Determine file extension from URL
                file_extension = document_url.split('.')[-1].lower() if '.' in document_url else ''
                print(f"File extension: {file_extension}")

                # Handle various file types
                if 'text' in content_type or file_extension in ['txt', 'md']:
                    return response.text
                elif 'pdf' in content_type or file_extension == 'pdf':
                    return f"PDF document (size: {len(response.content)} bytes) – cannot extract text directly"
                elif any(ext in content_type for ext in ['word', 'docx']) or file_extension in ['doc', 'docx']:
                    return f"Word document (size: {len(response.content)} bytes) – please review manually"
                elif 'json' in content_type:
                    import json
                    return json.dumps(response.json(), ensure_ascii=False, indent=2)
                else:
                    # Attempt to parse as text
                    try:
                        text_content = response.text
                        # Check if it looks like readable text
                        printable_chars = sum(1 for c in text_content[:100] if c.isprintable())
                        if printable_chars > 50:
                            snippet = text_content[:2000] + "..." if len(text_content) > 2000 else text_content
                            return snippet
                        else:
                            return f"Binary file ({content_type}) – size: {len(response.content)} bytes"
                    except UnicodeDecodeError:
                        return f"Unrecognized document type: {content_type} – size: {len(response.content)} bytes"

            return "Failed to download meeting document"
    except Exception as e:
        print(f"Failed to fetch meeting document: {e}")
        return "Meeting document fetch failed"
