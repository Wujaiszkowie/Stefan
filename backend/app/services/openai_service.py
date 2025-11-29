from openai import AsyncOpenAI, OpenAIError

from app.core.config import Settings
from app.core.exceptions import OpenAIServiceError
from app.core.logging import logger


class OpenAIService:
    def __init__(self, settings: Settings):
        self.settings = settings
        self.client = AsyncOpenAI(
            api_key=settings.openai_api_key,
            base_url=settings.openai_base_url,
        )

    async def chat_completion(
        self,
        messages: list[dict],
        model: str | None = None,
        temperature: float = 0.7,
        max_tokens: int = 1000,
    ) -> str:
        """
        Send a chat completion request to OpenAI.

        Args:
            messages: List of message dicts with 'role' and 'content' keys
            model: OpenAI model to use (defaults to settings)
            temperature: Sampling temperature (0-2)
            max_tokens: Maximum tokens in response

        Returns:
            The assistant's response text

        Raises:
            OpenAIServiceError: If the API call fails
        """
        model = model or self.settings.openai_default_model

        try:
            logger.debug(f"Sending chat completion request with model={model}")
            response = await self.client.chat.completions.create(
                model=model,
                messages=messages,
                temperature=temperature,
                max_tokens=max_tokens,
            )
            return response.choices[0].message.content
        except OpenAIError as e:
            logger.error(f"OpenAI chat completion failed: {e}")
            raise OpenAIServiceError(detail=f"Chat completion failed: {str(e)}")

    async def generate_embedding(
        self,
        text: str,
        model: str | None = None,
    ) -> list[float]:
        """
        Generate an embedding vector for the given text.

        Args:
            text: Text to embed
            model: Embedding model to use (defaults to settings)

        Returns:
            Embedding vector as a list of floats

        Raises:
            OpenAIServiceError: If the API call fails
        """
        model = model or self.settings.openai_embedding_model

        try:
            logger.debug(f"Generating embedding with model={model}")
            response = await self.client.embeddings.create(
                model=model,
                input=text,
            )
            return response.data[0].embedding
        except OpenAIError as e:
            logger.error(f"OpenAI embedding generation failed: {e}")
            raise OpenAIServiceError(detail=f"Embedding generation failed: {str(e)}")
