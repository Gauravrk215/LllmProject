package com.ollama.Ollama.Controller;

import java.util.Collections;
import java.util.stream.Collectors;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.ollama.Ollama.model.ChatResponse;
import com.ollama.Ollama.model.QueryRequest;

import jakarta.annotation.PostConstruct;

/**
 * Controller class for implementing Retrieval-Augmented Generation (RAG). RAG
 * combines information retrieval and AI-based generation to answer user
 * queries.
 */
@RestController
public class RAGController {

	/** Client for handling chat prompts and responses. */
	private final ChatClient chatClient;

	/** Model for embedding text into vector space for similarity searches. */
	private final EmbeddingModel embeddingModel;

	/** In-memory vector store for holding document embeddings. */
	private VectorStore vectorStore;

	/** Template used for generating system prompts with the knowledge base. */
	private String template;

	/** Path to the knowledge base file. */
	@Value("${file.path}")
	private Resource resource;

	/**
	 * Constructor to initialize the embedding model and chat client.
	 *
	 * @param embeddingModel    The {@link EmbeddingModel} used for creating vector
	 *                          embeddings.
	 * @param chatClientBuilder The {@link ChatClient.Builder} for constructing the
	 *                          chat client.
	 */
	public RAGController(EmbeddingModel embeddingModel, ChatClient.Builder chatClientBuilder) {
		this.embeddingModel = embeddingModel;
		this.chatClient = chatClientBuilder.build();
	}

	/**
	 * Initializes the vector store and processes the knowledge base after bean
	 * creation. Loads the knowledge base, splits it into tokens, and embeds it into
	 * the vector store.
	 */
	@PostConstruct
	public void init() {
		// Initialize vector store
		vectorStore = new SimpleVectorStore(embeddingModel);
		TextReader textReader = new TextReader(resource);
		TokenTextSplitter textSplitter = new TokenTextSplitter();

		// Process and store embeddings in the vector store
		vectorStore.accept(textSplitter.apply(textReader.get()));

		// Define the prompt template
//		template = """
//				Answer the questions only using the information in the provided knowledge base.
//				If you do not know the answer, please response with "I don't know."
//
//				KNOWLEDGE BASE
//				---
//				{documents}
//				""";

		template = "Answer the questions only using the information in the provided knowledge base.\n"
				+ "If you do not know the answer, please respond with \"I don't know.\"\n\n" + "KNOWLEDGE BASE\n---\n"
				+ "{documents}\n";
	}

	/**
	 * API endpoint for handling Retrieval-Augmented Generation (RAG) queries.
	 * Searches the knowledge base for relevant information and generates a
	 * response.
	 *
	 * @param request The {@link QueryRequest} containing the user query.
	 * @return a {@link ChatResponse} with the AI-generated response based on
	 *         retrieved documents.
	 */
	@PostMapping("/rag")
	public ChatResponse rag(@RequestBody QueryRequest request) {
		// Retrieve relevant documents from the vector store
		String relevantDocs = vectorStore.similaritySearch(request.getQuery()).stream().map(Document::getContent)
				.collect(Collectors.joining());

		// Create a system message using the retrieved documents and the template
//		Message systemMessage = new SystemPromptTemplate(template).createMessage(Map.of("documents", relevantDocs));
		Message systemMessage = new SystemPromptTemplate(template)
				.createMessage(Collections.singletonMap("documents", relevantDocs));

		// Create a user message from the query
		Message userMessage = new UserMessage(request.getQuery());

		// Combine the system and user messages into a prompt
//		Prompt prompt = new Prompt(List.of(systemMessage, userMessage));
		Prompt prompt = new Prompt(java.util.Arrays.asList(systemMessage, userMessage));

		// Generate a response using the chat client
		ChatClient.ChatClientRequest.CallPromptResponseSpec res = chatClient.prompt(prompt).call();

		// Prepare and return the chat response
		ChatResponse chatResponse = new ChatResponse();
		chatResponse.setResponseContent(res.content());

		return chatResponse;
	}
}
