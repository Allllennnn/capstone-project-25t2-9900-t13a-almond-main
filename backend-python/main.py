from fastapi import FastAPI
from routers.task_assignment import router as task_assignment_router
from routers.conversation import router as conversation_router
from routers.agent import router as agent_router
from routers.weekly_goal import router as weekly_goal_router

app = FastAPI()
app.include_router(task_assignment_router, prefix="/task-assignment")
app.include_router(conversation_router, prefix="/conversation")
app.include_router(agent_router, prefix="/agent")
app.include_router(weekly_goal_router, prefix="/weekly-goal")

# 运行： uvicorn main:app --reload
