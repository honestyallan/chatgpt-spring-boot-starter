package io.github.ringle.chatgpt.service;

import io.github.ringle.chatgpt.dto.ChatRequest;
import io.github.ringle.chatgpt.dto.ChatResponse;

public interface ChatgptService {

    String sendMessage(String message);

    ChatResponse sendChatRequest(ChatRequest request);

}
