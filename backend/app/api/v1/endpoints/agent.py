from fastapi import APIRouter

from app.api.dependencies import AgentServiceDep
from app.schemas.agent import AgentChatRequest, AgentChatResponse

router = APIRouter(prefix="/agent", tags=["agent"])


@router.post("/chat", response_model=AgentChatResponse)
async def agent_chat(
    request: AgentChatRequest,
    agent_service: AgentServiceDep,
) -> AgentChatResponse:
    """
    Chat with the LangGraph agent.

    Send a list of messages and receive the agent's response.
    Optionally provide a thread_id to track conversation context.
    """
    messages = [msg.model_dump() for msg in request.messages]

    response = await agent_service.chat(
        messages=messages,
        thread_id=request.thread_id,
    )

    return AgentChatResponse(
        response=response,
        thread_id=request.thread_id,
    )
