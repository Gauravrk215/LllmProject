package com.ollama.Ollama.Controller;

import java.util.UUID;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.PromptChatMemoryAdvisor;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY;
import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_RETRIEVE_SIZE_KEY;

import com.ollama.Ollama.model.ChatResponse;
import com.ollama.Ollama.model.QueryRequest;

/**
 * Controller class for managing conversational interactions using chat memory.
 * Provides an endpoint for handling chat requests with memory context.
 */
@RestController
public class ConversationController {

	/** Chat client for handling prompts and responses with memory advisors. */
	private final ChatClient chatClient;

	/**
	 * Constructor to initialize the ChatClient with in-memory chat memory and
	 * advisors.
	 *
	 * @param builder A builder instance for configuring the ChatClient.
	 */
	public ConversationController(ChatClient.Builder builder) {
		InMemoryChatMemory memory = new InMemoryChatMemory();
		this.chatClient = builder
				.defaultAdvisors(new PromptChatMemoryAdvisor(memory), new MessageChatMemoryAdvisor(memory)).build();
	}

	/**
	 * API endpoint to handle chat requests with memory context. If the conversation
	 * ID is not provided, a new conversation ID is generated. The chat memory is
	 * used to store and retrieve context for ongoing conversations.
	 *
	 * @param request The {@link QueryRequest} containing the conversation ID and
	 *                user query.
	 * @return a {@link ChatResponse} object with the conversation ID and the
	 *         AI-generated response.
	 */
	@PostMapping("/chatMemory")
	public ChatResponse chatMemory(@RequestBody QueryRequest request) {
		if (request.getConversationId() == null || request.getConversationId().isEmpty()) {
			request.setConversationId(UUID.randomUUID().toString());
		}

		// Process the user query with chat memory and advisors
		String content = this.chatClient.prompt().user(request.getQuery())
				.advisors(a -> a.param(CHAT_MEMORY_CONVERSATION_ID_KEY, request.getConversationId())
						.param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 100))
				.call().content();

		// Prepare and return the chat response
		ChatResponse chatResponse = new ChatResponse();
		chatResponse.setResponseContent(content);
		chatResponse.setConversationId(request.getConversationId());

		return chatResponse;
	}
}
