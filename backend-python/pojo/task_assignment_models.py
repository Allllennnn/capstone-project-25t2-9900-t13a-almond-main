from pydantic import BaseModel
from typing import List, Optional
from datetime import datetime

class TaskAssignment(BaseModel):
    user_id: int
    user_name: str
    description: str

class TaskAssignmentRequest(BaseModel):
    project_url: Optional[str] = None
    task_description: str
    task_file_content: Optional[str] = None
    group_name: str
    task_name: str
    assignments: List[TaskAssignment]

class TaskAssignmentUpdateRequest(BaseModel):
    project_url: Optional[str] = None
    task_description: str
    group_name: str
    task_name: str
    original_assignments: List[TaskAssignment]
    updated_assignments: List[TaskAssignment]

class TaskAssignmentFollowupRequest(BaseModel):
    project_url: Optional[str] = None
    task_description: str
    group_name: str
    task_name: str
    finalized_assignments: List[TaskAssignment]
    days_since_start: int

class ProjectAnalysisRequest(BaseModel):
    project_url: str
    task_description: str

class ConversationMessage(BaseModel):
    sender_type: str  # "USER" or "AGENT"
    sender_id: Optional[int] = None
    content: str
    timestamp: datetime

class ConversationRequest(BaseModel):
    task_id: int
    group_id: int
    message: str
    sender_id: int

class ConversationHistoryRequest(BaseModel):
    task_id: int
    group_id: int 