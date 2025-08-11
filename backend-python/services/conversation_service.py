import json
import os
from datetime import datetime
from typing import List, Dict, Any
from pathlib import Path


class ConversationService:
    def __init__(self):
        # Store conversations on the filesystem; could be switched to a database
        self.storage_dir = Path("conversations")
        self.storage_dir.mkdir(exist_ok=True)

    def _get_conversation_file(self, task_id: int, group_id: int) -> Path:
        """Get the path to the conversation file for a given task and group."""
        return self.storage_dir / f"conversation_{group_id}_{task_id}.json"

    def _load_conversation(self, task_id: int, group_id: int) -> List[Dict[str, Any]]:
        """Load the conversation history from disk."""
        file_path = self._get_conversation_file(task_id, group_id)
        if file_path.exists():
            try:
                with open(file_path, 'r', encoding='utf-8') as f:
                    return json.load(f)
            except (json.JSONDecodeError, IOError):
                # Return an empty list if the file is corrupted or unreadable
                return []
        # No file yet: return empty history
        return []

    def _save_conversation(self, task_id: int, group_id: int, messages: List[Dict[str, Any]]):
        """Save the conversation history to disk."""
        file_path = self._get_conversation_file(task_id, group_id)
        try:
            with open(file_path, 'w', encoding='utf-8') as f:
                json.dump(messages, f, ensure_ascii=False, indent=2, default=str)
        except IOError as e:
            raise Exception(f"Failed to save conversation: {e}")

    def add_message(self,
                    task_id: int,
                    group_id: int,
                    sender_type: str,
                    content: str,
                    sender_id: int = None) -> Dict[str, Any]:
        """Add a new message to the conversation history."""
        messages = self._load_conversation(task_id, group_id)

        new_message = {
            "message_id": len(messages) + 1,
            "sender_type": sender_type,
            "sender_id": sender_id,
            "content": content,
            "timestamp": datetime.now().isoformat()
        }

        messages.append(new_message)
        self._save_conversation(task_id, group_id, messages)

        return new_message

    def get_conversation_history(self, task_id: int, group_id: int) -> List[Dict[str, Any]]:
        """Retrieve the full conversation history."""
        return self._load_conversation(task_id, group_id)

    def clear_conversation(self, task_id: int, group_id: int):
        """Delete the conversation history file, effectively clearing it."""
        file_path = self._get_conversation_file(task_id, group_id)
        if file_path.exists():
            file_path.unlink()

    def get_conversation_summary(self, task_id: int, group_id: int) -> Dict[str, Any]:
        """Return a summary of the conversation history."""
        messages = self._load_conversation(task_id, group_id)

        user_messages = [m for m in messages if m["sender_type"] == "USER"]
        agent_messages = [m for m in messages if m["sender_type"] == "AGENT"]

        return {
            "conversation_id": f"{group_id}_{task_id}",
            "total_messages": len(messages),
            "user_messages": len(user_messages),
            "agent_messages": len(agent_messages),
            "last_activity": messages[-1]["timestamp"] if messages else None
        }
