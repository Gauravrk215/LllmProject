package com.ollama.Ollama.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ollama.Ollama.Services.ChatService;
import com.ollama.Ollama.model.ChatResponse;

/**
 * Controller class for handling chat-related API requests. Provides endpoints
 * to interact with the chat service.
 */
@RestController
@RequestMapping("/ollama")
public class ChatController {

	/** Service layer for managing chat logic and context. */
	@Autowired
	private ChatService chatService;

	/**
	 * API endpoint to process a user prompt and return a chat response.
	 *
	 * @param chartId (Optional) The ID of the chat session. If not provided, a new
	 *                chat session is started.
	 * @param m       The user's input message (prompt).
	 * @return a {@link ChatResponse} object containing the conversation ID and the
	 *         generated response content.
	 */
	@GetMapping("/prompt")
	public ChatResponse prompt(@RequestParam(required = false) String chartId, @RequestParam String m) {

		ChatResponse resp = chatService.handleChat(chartId, m);

		return resp;
	}

}
