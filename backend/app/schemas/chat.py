from pydantic import BaseModel, Field


class ChatRequest(BaseModel):
    message: str = Field(..., min_length=1, description="The message to send to the AI")
    model: str | None = Field(None, description="OpenAI model to use")
    temperature: float = Field(0.7, ge=0.0, le=2.0, description="Sampling temperature")
    max_tokens: int = Field(1000, ge=1, le=4096, description="Maximum tokens in response")


class ChatResponse(BaseModel):
    response: str = Field(..., description="The AI's response")


class EmbeddingRequest(BaseModel):
    text: str = Field(..., min_length=1, description="Text to generate embedding for")
    model: str | None = Field(None, description="Embedding model to use")


class EmbeddingResponse(BaseModel):
    embedding: list[float] = Field(..., description="The embedding vector")
