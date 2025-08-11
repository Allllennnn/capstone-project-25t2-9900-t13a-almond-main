# ======================================
# services/models.py
# ======================================
from sqlalchemy import (
    Table, Column, BigInteger, Enum, Text, JSON, DateTime, MetaData
)
from sqlalchemy.sql import func

metadata = MetaData()

agent_conversations = Table(
    "agent_conversations",
    metadata,
    Column("id", BigInteger, primary_key=True, autoincrement=True),
    Column("task_id", BigInteger, nullable=False),
    Column("group_id", BigInteger, nullable=False),
    Column("sender", Enum("STUDENT","TEACHER","ADMIN","AGENT"), nullable=False),
    Column("message", Text, nullable=False),
    Column("context", JSON, nullable=True),
    Column("created_at", DateTime, server_default=func.now(), nullable=False),
)

