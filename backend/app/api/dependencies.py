from typing import Annotated

from fastapi import Depends

from app.core.config import Settings, get_settings
from app.services.openai_service import OpenAIService

# Singleton instance
_openai_service: OpenAIService | None = None


def get_openai_service() -> OpenAIService:
    global _openai_service
    if _openai_service is None:
        settings = get_settings()
        _openai_service = OpenAIService(settings)
    return _openai_service


# Type aliases for dependency injection
SettingsDep = Annotated[Settings, Depends(get_settings)]
OpenAIServiceDep = Annotated[OpenAIService, Depends(get_openai_service)]
