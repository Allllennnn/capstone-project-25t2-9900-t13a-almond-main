from fastapi import APIRouter, HTTPException, Body
from pydantic import BaseModel
from typing import Optional, Dict, Any, List
import json
from services.agent_service import generate_weekly_goals

router = APIRouter()


class WeeklyGoalRequest(BaseModel):
    task_id: int
    student_id: int
    week_no: int
    task_content: Optional[str] = None
    task_file_url: Optional[str] = None
    task_file_content: Optional[str] = None
    initial_assignments: Optional[str] = None
    previous_goals: Optional[List[Dict[str, Any]]] = None
    previous_meeting_content: Optional[str] = None


@router.post("/generate")
async def generate_weekly_goal(req: WeeklyGoalRequest):
    """Generate a weekly goal based on provided task and context data."""
    print("=== Received request to generate weekly goal ===")
    print(f"task_id={req.task_id}, student_id={req.student_id}, week_no={req.week_no}")

    # Log received data
    print(f"Task content length: {len(req.task_content) if req.task_content else 0}")
    print(f"Task file URL: {req.task_file_url}")
    print(f"Task file content length: {len(req.task_file_content) if req.task_file_content else 0}")

    # Log first 200 characters of the task file content
    if req.task_file_content:
        preview = req.task_file_content[:200] + "..." if len(req.task_file_content) > 200 else req.task_file_content
        print(f"Task file content preview: {preview}")

    print(f"Initial assignments: {req.initial_assignments}")

    # Log previous goals
    if req.previous_goals:
        print(f"Number of previous goals: {len(req.previous_goals)}")
        for i, goal in enumerate(req.previous_goals[:3]):
            desc = goal.get('goal', '') or ''
            desc_preview = desc[:50] + "..." if len(desc) > 50 else desc
            print(
                f"Goal {i + 1}: weekNo={goal.get('weekNo')}, status={goal.get('status')}, goal preview: {desc_preview}")
        if len(req.previous_goals) > 3:
            remaining = len(req.previous_goals) - 3
            print(f"... {remaining} more goals not shown")
    else:
        print("No previous goals")

    # Log previous meeting content
    if req.previous_meeting_content:
        print(f"Previous meeting content length: {len(req.previous_meeting_content)}")
        preview = req.previous_meeting_content[:200] + "..." if len(
            req.previous_meeting_content) > 200 else req.previous_meeting_content
        print(f"Previous meeting content preview: {preview}")
    else:
        print("No previous meeting content")

    # Log full request JSON
    full_json = json.dumps(req.dict(), ensure_ascii=False, default=str)
    snippet = full_json[:1000] + "..." if len(full_json) > 1000 else full_json
    print(f"Full request JSON: {snippet}")

    try:
        # Call service to generate the weekly goal
        result = generate_weekly_goals(
            task_id=req.task_id,
            student_id=req.student_id,
            week_no=req.week_no,
            task_content=req.task_content,
            task_file_content=req.task_file_content,
            initial_assignments=req.initial_assignments,
            previous_goals=req.previous_goals,
            previous_meeting_content=req.previous_meeting_content
        )

        # Log generated result
        print(f"Generated weekly goal: {json.dumps(result, ensure_ascii=False)}")

        return {
            "status": "success",
            "weekly_goal": result
        }
    except Exception as e:
        print(f"Failed to generate weekly goal: {e}")
        raise HTTPException(status_code=500, detail=f"Failed to generate weekly goal: {e}")
