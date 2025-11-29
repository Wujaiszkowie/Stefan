from fastapi import APIRouter

from app.api.dependencies import SettingsDep
from app.schemas.health import HealthResponse

router = APIRouter(tags=["health"])


@router.get("/health", response_model=HealthResponse)
async def health_check(settings: SettingsDep) -> HealthResponse:
    """Check the health status of the API."""
    return HealthResponse(
        status="healthy",
        version=settings.app_version,
    )
