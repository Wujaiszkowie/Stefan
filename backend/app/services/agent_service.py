from typing import Annotated, TypedDict

from langchain_openai import ChatOpenAI
from langgraph.graph import StateGraph, START, END
from langgraph.graph.message import add_messages

from app.core.config import Settings
from app.core.logging import logger


class AgentState(TypedDict):
    """State for the conversational agent."""
    messages: Annotated[list, add_messages]


class AgentService:
    """LangGraph-based conversational agent service."""

    def __init__(self, settings: Settings):
        self.settings = settings
        self.llm = ChatOpenAI(
            model=settings.openai_default_model,
            api_key=settings.openai_api_key,
            base_url=settings.openai_base_url,
            temperature=0.7,
        )
        self.graph = self._build_graph()

    def _build_graph(self) -> StateGraph:
        """Build the LangGraph conversation graph."""
        graph_builder = StateGraph(AgentState)

        graph_builder.add_node("chatbot", self._chatbot_node)

        graph_builder.add_edge(START, "chatbot")
        graph_builder.add_edge("chatbot", END)

        return graph_builder.compile()

    async def _chatbot_node(self, state: AgentState) -> dict:
        """Process messages and generate a response."""
        logger.info("Processing chat request with LangGraph agent")
        response = await self.llm.ainvoke(state["messages"])
        return {"messages": [response]}

    async def chat(
        self,
        messages: list[dict],
        thread_id: str | None = None,
    ) -> str:
        """
        Process a chat request and return the agent's response.

        Args:
            messages: List of message dicts with 'role' and 'content' keys
            thread_id: Optional thread identifier for conversation tracking

        Returns:
            The agent's response as a string
        """
        logger.info(f"Agent chat request - thread_id: {thread_id}, messages: {len(messages)}")

        formatted_messages = [
            (msg["role"], msg["content"]) for msg in messages
        ]

        result = await self.graph.ainvoke(
            {"messages": formatted_messages},
            config={"configurable": {"thread_id": thread_id or "default"}},
        )

        response_message = result["messages"][-1]
        return response_message.content
