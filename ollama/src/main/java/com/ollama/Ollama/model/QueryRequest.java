package com.ollama.Ollama.model;

import lombok.Data;

@Data
public class QueryRequest {

	private String query;

	private String conversationId;

}
