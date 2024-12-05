package com.ollama.Ollama.Controller;

import java.util.Arrays;
import java.util.List;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.ollama.Ollama.model.QueryRequest;

import reactor.core.publisher.Flux;

/**
 * The BasicController handles chat interactions with the AI chat client. It
 * provides endpoints for basic chat, chat with a system prompt, and streaming
 * responses.
 */
@RestController
public class BasicController {

	// Instance of the ChatClient, used for making requests to the AI system.

	private final ChatClient chatClient;

	/**
	 * Constructor for BasicController.
	 *
	 * @param chatClientBuilder Builder for creating a ChatClient instance.
	 */

	public BasicController(ChatClient.Builder chatClientBuilder) {
		this.chatClient = chatClientBuilder.build();
	}

	/**
	 * Handles a basic chat request using the query provided in the request body.
	 *
	 * @param request Contains the user query.
	 * @return A list of generations (AI-generated responses).
	 */

	@PostMapping("/basicChat")
	public List<Generation> basicChat(@RequestBody QueryRequest request) {
		// Create a prompt template using the user's query

		PromptTemplate promptTemplate = new PromptTemplate(request.getQuery());
		Prompt prompt = promptTemplate.create();

		// Make a synchronous call to the chat client and get the response

		ChatClient.ChatClientRequest.CallPromptResponseSpec res = chatClient.prompt(prompt).call();

		return res.chatResponse().getResults();
	}

	/**
	 * Handles a chat request with an added system message to guide the AI response.
	 *
	 * @param request Contains the user query.
	 * @return A list of generations (AI-generated responses).
	 */
	@PostMapping("/chatWithSystemPrompt")
	public List<Generation> basicChatWithSystemPrompt(@RequestBody QueryRequest request) {

		// Create a user message based on the query and a system message for context

		Message userMessage = new UserMessage(request.getQuery());
		Message systemMessage = new SystemMessage(
				"You are an expert in Java. Please provide to the point and accurate answers.");
//		Prompt prompt = new Prompt(List.of(systemMessage, userMessage));
		// Combine messages into a prompt
		Prompt prompt = new Prompt(Arrays.asList(systemMessage, userMessage));

		// Make a synchronous call to the chat client and get the response

		ChatClient.ChatClientRequest.CallPromptResponseSpec res = chatClient.prompt(prompt).call();
		// Return the list of generated responses
		return res.chatResponse().getResults();
	}

	/**
	 * Handles a chat request and streams responses back to the client.
	 *
	 * @param request Contains the user query.
	 * @return A Flux of ChatResponse objects, representing streamed AI responses.
	 */
	@PostMapping("/chatWithStreamingResponse")
	public Flux<ChatResponse> chatWithStreamingResponse(@RequestBody QueryRequest request) {
		// Create a user message and a system message for the prompt
		Message userMessage = new UserMessage(request.getQuery());
		Message systemMessage = new SystemMessage(
				"You are an expert in Java. Please provide to the point and accurate answers.");

		// Combine messages into a prompt
		Prompt prompt = new Prompt(Arrays.asList(systemMessage, userMessage));

		// Use the chat client to get a streaming response
		Flux<ChatResponse> flux = chatClient.prompt(prompt).stream().chatResponse();

		// Log the streaming output and handle errors
		flux.subscribe(chatResponse -> System.out.print(chatResponse.getResult().getOutput().getContent()), // onNext
				throwable -> System.err.println("Error occurred: " + throwable), // onError
				() -> System.out.println("Streaming complete") // onComplete
		);
		// Return the Flux to allow the client to process the streamed data

		return flux;
	}

}
