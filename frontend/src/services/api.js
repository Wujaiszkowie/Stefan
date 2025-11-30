/**
 * API Service
 *
 * REST API client for backend communication.
 * Handles onboarding status checks and other REST endpoints.
 */

const API_BASE_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080';

/**
 * Check if user has completed onboarding (has facts in the system).
 * Uses HEAD request to /api/fact endpoint.
 *
 * @returns {Promise<boolean>} true if onboarded (has facts), false otherwise
 */
export async function checkOnboardingStatus() {
  try {
    const response = await fetch(`${API_BASE_URL}/api/fact`, {
      method: 'HEAD',
    });

    // 200 OK = has facts (onboarded)
    // 204 No Content = no facts (needs onboarding)
    return response.status === 200;
  } catch (error) {
    console.error('[API] Failed to check onboarding status:', error);
    // On error, assume onboarded to not block user from using the app
    return true;
  }
}

export default {
  checkOnboardingStatus,
};
