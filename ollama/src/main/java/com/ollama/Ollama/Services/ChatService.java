package com.ollama.Ollama.Services;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.ollama.Ollama.model.ChatResponse;

@Service
public class ChatService {

	@Value("${projectPath}")
	private String projectPath;

	private final ChatModel chatModel;

	private final Object fileLock = new Object(); // Lock for thread safety
	private final Map<String, StringBuilder> chatContextMap = new ConcurrentHashMap<>(); // Store chat context

	private ChatService(ChatModel chatModel) {
		this.chatModel = chatModel;
	}

	public ChatResponse handleChat(String chatId, String prompt) {
		if (chatId == null || chatId.isEmpty()) {
			// Start a new chat
			chatId = startChat();
		}

		// Process prompt in the context of the chatId
		String response = processPrompt(chatId, prompt);
		ChatResponse chat = new ChatResponse();
		chat.setConversationId(chatId);
		chat.setResponseContent(response);

		return chat;
	}

	private String startChat() {
		String chatId = UUID.randomUUID().toString(); // Generate unique chat ID
		chatContextMap.put(chatId, new StringBuilder()); // Initialize context
		return chatId;
	}

	private String processPrompt(String chatId, String prompt) {
		if (!chatContextMap.containsKey(chatId)) {
			throw new IllegalArgumentException("Invalid chatId: Chat session not found!");
		}

		// Get the response from the chat model
		String response = chatModel.call(prompt);

		// Update chat context
		updateChatContext(chatId, prompt, response);

		// Save the context to file
		saveContextToFile(chatId);

		return response;
	}

	// Update chat context with prompt and response
	private void updateChatContext(String chatId, String prompt, String response) {
		StringBuilder context = chatContextMap.get(chatId);
		synchronized (context) {
			context.append("Prompt: ").append(prompt).append("\n");
			context.append("Response: ").append(response).append("\n");
			context.append("=========================================\n");
		}
	}

	// Save the chat context to a file
	private void saveContextToFile(String chatId) {
		String filePath = projectPath + File.separator + "responses/chat_" + chatId + ".txt";

		synchronized (fileLock) {
			File file = new File(filePath);
			file.getParentFile().mkdirs();

			try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
				writer.write("Chat ID: " + chatId + "\n");
				writer.write(chatContextMap.get(chatId).toString());
			} catch (IOException e) {
				e.printStackTrace();
				System.err.println("Failed to write chat context to file.");
			}
		}
	}
}
