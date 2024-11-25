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

@RestController
public class ConversationController {

    private final ChatClient chatClient;

    public ConversationController(ChatClient.Builder builder) {
        InMemoryChatMemory memory = new InMemoryChatMemory();
        this.chatClient = builder.
                defaultAdvisors(
                  new PromptChatMemoryAdvisor(memory),
                  new MessageChatMemoryAdvisor(memory)
                )
                .build();
    }

    @PostMapping("/chatMemory")
    public ChatResponse chatMemory(@RequestBody QueryRequest request) {
        if (request.getConversationId() == null || request.getConversationId().isEmpty()) {
            request.setConversationId(UUID.randomUUID().toString());
        }
        String content = this.chatClient.prompt()
                .user(request.getQuery())
                .advisors(a -> a
                        .param(CHAT_MEMORY_CONVERSATION_ID_KEY, request.getConversationId())
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 100))
                .call().content();

        ChatResponse chatResponse = new ChatResponse();
        chatResponse.setResponseContent(content);
        chatResponse.setConversationId(request.getConversationId());

        return chatResponse;
    }
}
