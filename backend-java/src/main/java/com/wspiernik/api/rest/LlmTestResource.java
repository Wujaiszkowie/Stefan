package com.wspiernik.api.rest;

import com.wspiernik.api.websocket.ConversationSessionManager;
import com.wspiernik.domain.survey.SurveyService;
import com.wspiernik.infrastructure.llm.LlmClient;
import com.wspiernik.infrastructure.llm.PromptTemplates;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

/**
 * REST endpoint for testing LLM integration.
 * Remove or disable in production.
 */
@Path("/api/test/llm")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class LlmTestResource {

    @Inject
    LlmClient llmClient;

    @Inject
    PromptTemplates promptTemplates;

    @Inject
    SurveyService surveyService;

    public record TestRequest(String message) {}
    public record TestResponse(String response, boolean success, String error) {}

    /**
     * Simple ping test - just returns config info.
     */
    @GET
    @Path("/ping")
    public String ping() {
        return "{\"status\": \"LLM test endpoint ready\"}";
    }

    /**
     * Test LLM with a simple message.
     *
     * Usage: POST /api/test/llm/chat
     * Body: {"message": "Cześć, jak się masz?"}
     */
    @POST
    @Path("/chat")
    public TestResponse testChat(TestRequest request) {
        try {
            String systemPrompt = "Jesteś pomocnym asystentem. Odpowiadaj krótko po polsku.";
            String response = llmClient.generate(systemPrompt, request.message());
            return new TestResponse(response, true, null);
        } catch (Exception e) {
            return new TestResponse(null, false, e.getMessage());
        }
    }

    /**
     * Test survey prompt generation.
     *
     * Usage: GET /api/test/llm/survey-prompt
     */
    @GET
    @Path("/survey-prompt")
    public String getSurveyPrompt() {
        return promptTemplates.buildSurveyPrompt();
    }

    /**
     * Test LLM with survey prompt.
     *
     * Usage: POST /api/test/llm/survey?message=Mój podopieczny ma 75 lat
     */
    @POST
    @Path("/survey")
    public TestResponse testSurvey(@QueryParam("message") String message) {
        try {
            String systemPrompt = promptTemplates.buildSurveyPrompt();
            String response = llmClient.generate(systemPrompt, message != null ? message : "Dzień dobry, chcę zarejestrować podopiecznego");
            return new TestResponse(response, true, null);
        } catch (Exception e) {
            return new TestResponse(null, false, e.getMessage());
        }
    }

    @POST
    @Path("/survey-start")
    public TestResponse surveyStart(ConversationSessionManager.ConversationSession session) {
        try {

            var result = surveyService.startSurvey(session);
            return new TestResponse(result.toString(), true, null);
        } catch (Exception e) {
            return new TestResponse(null, false, e.getMessage());
        }
    }

    @POST
    @Path("/survey-continue")
    public TestResponse surveyContinue(ConversationSessionManager.ConversationSession session,
                                       @QueryParam("message") String message) {
        try {
            var result = surveyService.processMessage(session, message);
            return new TestResponse(result.toString(), true, null);
        } catch (Exception e) {
            return new TestResponse(null, false, e.getMessage());
        }
    }

}
