from fastapi import APIRouter

from app.api.dependencies import OpenAIServiceDep
from app.schemas.chat import (
    ChatRequest,
    ChatResponse,
    EmbeddingRequest,
    EmbeddingResponse,
)

router = APIRouter(prefix="/chat", tags=["chat"])


@router.post("", response_model=ChatResponse)
async def chat(
    request: ChatRequest,
    openai_service: OpenAIServiceDep,
) -> ChatResponse:
    """Send a message to OpenAI and get a response."""
    messages = [{"role": "user", "content": request.message}]
    response = await openai_service.chat_completion(
        messages=messages,
        model=request.model,
        temperature=request.temperature,
        max_tokens=request.max_tokens,
    )
    return ChatResponse(response=response)


@router.post("/embedding", response_model=EmbeddingResponse)
async def embedding(
    request: EmbeddingRequest,
    openai_service: OpenAIServiceDep,
) -> EmbeddingResponse:
    """Generate an embedding for the given text."""
    result = await openai_service.generate_embedding(
        text=request.text,
        model=request.model,
    )
    return EmbeddingResponse(embedding=result)
