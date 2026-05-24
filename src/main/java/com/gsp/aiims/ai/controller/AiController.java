package com.gsp.aiims.ai.controller;

import com.gsp.aiims.ai.dto.ChatRequest;
import com.gsp.aiims.ai.dto.ChatResponse;
import com.gsp.aiims.ai.service.AiService;
import com.gsp.aiims.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
@Tag(name = "AI Assistant", description = "Natural language invoice queries powered by Ollama")
public class AiController {

    private final AiService aiService;

    @PostMapping("/chat")
    @Operation(summary = "Chat with the AI assistant about invoices, revenue, customers, and payments")
    public ResponseEntity<ApiResponse<ChatResponse>> chat(@Valid @RequestBody ChatRequest request) {
        ChatResponse response = aiService.chat(request.getMessage());
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
