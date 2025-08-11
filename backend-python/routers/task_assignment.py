from fastapi import APIRouter, HTTPException
from pojo.task_assignment_models import (
    TaskAssignmentRequest,
    TaskAssignmentUpdateRequest,
    TaskAssignmentFollowupRequest,
    ProjectAnalysisRequest
)
from services.task_assignment_chains import (
    initial_assignment_chain,
    confirmation_assignment_chain,
    followup_assignment_chain,
    project_analysis_chain
)

router = APIRouter()

@router.post("/initial-advice")
async def get_initial_assignment_advice(req: TaskAssignmentRequest):
    """Get initial task assignment advice"""
    print("=== Processing initial task assignment advice request ===")
    print("Received request data:")
    print(f"  project_url: {req.project_url}")
    print(f"  task_name: {req.task_name}")
    print(f"  group_name: {req.group_name}")
    print(f"  task_description length: {len(req.task_description) if req.task_description else 0}")
    print(f"  task_file_content exists: {req.task_file_content is not None}")
    if req.task_file_content:
        print(f"  task_file_content length: {len(req.task_file_content)}")
        preview = req.task_file_content[:200] if len(req.task_file_content) > 200 else req.task_file_content
        print(f"  task_file_content preview: {preview}")
    print(f"  number of assignments: {len(req.assignments)}")

    try:
        # Format assignments
        assignments_text = "\n".join([
            f"- {assignment.user_name}: {assignment.description}"
            for assignment in req.assignments
        ])

        # Use parsed file content if available, otherwise use task description
        task_content = req.task_file_content or req.task_description

        print("\n=== Preparing to invoke LLM ===")
        print(f"Task content length: {len(task_content)}")
        print("Formatted assignments:")
        print(assignments_text)

        # Build prompt parameters
        prompt_params = {
            "project_url": req.project_url or "not provided",
            "task_description": req.task_description,
            "task_file_content": task_content,
            "group_name": req.group_name,
            "task_name": req.task_name,
            "assignments": assignments_text
        }

        print("\n=== LLM invocation parameters ===")
        for key, value in prompt_params.items():
            if isinstance(value, str) and len(value) > 200:
                print(f"{key}: {value[:200]}... (length: {len(value)})")
            else:
                print(f"{key}: {value}")

        # Invoke LangChain to get advice
        advice = initial_assignment_chain.invoke(prompt_params)

        print("\n=== LLM response ===")
        print(f"Advice length: {len(advice)}")
        print(f"Advice content: {advice}")

        return {
            "advice": advice,
            "status": "success",
            "conversation_id": f"{req.group_name}_{req.task_name}"
        }
    except Exception as e:
        print(f"Error processing initial assignment advice: {e}")
        raise HTTPException(status_code=500, detail=f"Error generating advice: {e}")

@router.post("/confirmation-advice")
async def get_confirmation_assignment_advice(req: TaskAssignmentUpdateRequest):
    """Get assignment confirmation advice"""
    try:
        # Format original assignments
        original_assignments_text = "\n".join([
            f"- {assignment.user_name}: {assignment.description}"
            for assignment in req.original_assignments
        ])

        # Format updated assignments
        updated_assignments_text = "\n".join([
            f"- {assignment.user_name}: {assignment.description}"
            for assignment in req.updated_assignments
        ])

        # Invoke LangChain to get advice
        advice = confirmation_assignment_chain.invoke({
            "project_url": req.project_url or "not provided",
            "task_description": req.task_description,
            "group_name": req.group_name,
            "task_name": req.task_name,
            "original_assignments": original_assignments_text,
            "updated_assignments": updated_assignments_text
        })

        return {
            "advice": advice,
            "status": "success",
            "conversation_id": f"{req.group_name}_{req.task_name}"
        }
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Error generating confirmation advice: {e}")

@router.post("/followup-advice")
async def get_followup_assignment_advice(req: TaskAssignmentFollowupRequest):
    """Get follow-up assignment advice"""
    try:
        # Format finalized assignments
        finalized_assignments_text = "\n".join([
            f"- {assignment.user_name}: {assignment.description}"
            for assignment in req.finalized_assignments
        ])

        # Invoke LangChain to get advice
        advice = followup_assignment_chain.invoke({
            "project_url": req.project_url or "not provided",
            "task_description": req.task_description,
            "group_name": req.group_name,
            "task_name": req.task_name,
            "finalized_assignments": finalized_assignments_text,
            "days_since_start": req.days_since_start
        })

        return {
            "advice": advice,
            "status": "success",
            "conversation_id": f"{req.group_name}_{req.task_name}"
        }
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Error generating follow-up advice: {e}")

@router.post("/project-analysis")
async def get_project_analysis(req: ProjectAnalysisRequest):
    """Get project analysis"""
    try:
        # Invoke LangChain to get project analysis
        analysis = project_analysis_chain.invoke({
            "project_url": req.project_url,
            "task_description": req.task_description
        })

        return {
            "analysis": analysis,
            "status": "success"
        }
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Error analyzing project: {e}")
