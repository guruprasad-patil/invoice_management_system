package com.gsp.aiims.ai.service;

import com.gsp.aiims.ai.dto.ChatResponse;
import com.gsp.aiims.ai.tools.InvoiceAiTools;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AiServiceImpl implements AiService {

    private static final String SYSTEM_PROMPT = """
            You are an intelligent invoice management assistant.
            You have access to real-time invoice data through tools.
            When asked about invoices, payments, revenue, or customers — use the available tools to fetch accurate data.
            Always provide clear, concise answers with actual numbers when available.
            Format currency values with $ prefix and two decimal places.
            """;

    private final ChatClient chatClient;

    public AiServiceImpl(ChatClient.Builder chatClientBuilder, InvoiceAiTools invoiceAiTools) {
        this.chatClient = chatClientBuilder
                .defaultSystem(SYSTEM_PROMPT)
                .defaultTools(invoiceAiTools)
                .build();
    }

    @Override
    public ChatResponse chat(String userMessage) {
        log.debug("AI chat request: {}", userMessage);
        String reply = chatClient
                .prompt()
                .user(userMessage)
                .call()
                .content();
        log.debug("AI chat reply length: {}", reply != null ? reply.length() : 0);
        return new ChatResponse(userMessage, reply);
    }
}
