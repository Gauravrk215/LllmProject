package com.ollama.Ollama.model;

import lombok.Data;

@Data
public class ChatResponse {

	private String conversationId;
	private String responseContent;

//	public ChatResponse(String chatId, String response) {
//		this.conversationId = chatId;
//		this.responseContent = response;
//	}
}
