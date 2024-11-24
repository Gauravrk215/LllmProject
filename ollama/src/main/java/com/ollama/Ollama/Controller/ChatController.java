package com.ollama.Ollama.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ollama.Ollama.Services.ChatService;
import com.ollama.Ollama.model.ChatResponse;

@RestController
@RequestMapping("/ollama")
public class ChatController {

	@Autowired
	private ChatService chatService;

	@GetMapping("/prompt")
	public ChatResponse prompt(@RequestParam(required = false) String chartId, @RequestParam String m) {

		ChatResponse resp = chatService.handleChat(chartId, m);

		return resp;
	}

}
