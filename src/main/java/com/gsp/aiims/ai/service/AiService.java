package com.gsp.aiims.ai.service;

import com.gsp.aiims.ai.dto.ChatResponse;

public interface AiService {

    ChatResponse chat(String userMessage);
}
