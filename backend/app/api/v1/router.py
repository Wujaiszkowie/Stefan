from fastapi import APIRouter

from app.api.v1.endpoints import agent, chat, health

api_router = APIRouter(prefix="/api/v1")

api_router.include_router(health.router)
api_router.include_router(chat.router)
api_router.include_router(agent.router)
