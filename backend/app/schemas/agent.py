from pydantic import BaseModel, Field


class AgentMessage(BaseModel):
    """A single message in the conversation."""
    role: str = Field(..., description="The role of the message sender (user, assistant, system)")
    content: str = Field(..., description="The content of the message")


class AgentChatRequest(BaseModel):
    """Request model for agent chat endpoint."""
    messages: list[AgentMessage] = Field(..., description="List of conversation messages")
    thread_id: str | None = Field(None, description="Optional thread ID for conversation tracking")

    model_config = {
        "json_schema_extra": {
            "examples": [
                {
                    "messages": [
                        {"role": "user", "content": "Hello, how are you?"}
                    ],
                    "thread_id": "conversation-123"
                }
            ]
        }
    }


class AgentChatResponse(BaseModel):
    """Response model for agent chat endpoint."""
    response: str = Field(..., description="The agent's response")
    thread_id: str | None = Field(None, description="Thread ID for conversation tracking")
