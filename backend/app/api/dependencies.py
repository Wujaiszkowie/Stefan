from typing import Annotated

from fastapi import Depends

from app.core.config import Settings, get_settings
from app.services.agent_service import AgentService
from app.services.openai_service import OpenAIService

# Singleton instances
_openai_service: OpenAIService | None = None
_agent_service: AgentService | None = None


def get_openai_service() -> OpenAIService:
    global _openai_service
    if _openai_service is None:
        settings = get_settings()
        _openai_service = OpenAIService(settings)
    return _openai_service


def get_agent_service() -> AgentService:
    global _agent_service
    if _agent_service is None:
        settings = get_settings()
        _agent_service = AgentService(settings)
    return _agent_service


# Type aliases for dependency injection
SettingsDep = Annotated[Settings, Depends(get_settings)]
OpenAIServiceDep = Annotated[OpenAIService, Depends(get_openai_service)]
AgentServiceDep = Annotated[AgentService, Depends(get_agent_service)]
