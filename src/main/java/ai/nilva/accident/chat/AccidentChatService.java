package ai.nilva.accident.chat;

import ai.nilva.accident.tools.AccidentTools;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Service;

@Service
public class AccidentChatService {

    private static final String SYSTEM_PROMPT = """
            You are Accident Aid, a cautious educational assistant for road and everyday accidents.
            Use tools for urgency screening, reviewed guidance, or current road weather.
            Never invent injury facts or coordinates. Ask short follow-up questions when facts are missing.
            For an unsafe scene, unresponsiveness, abnormal breathing, severe bleeding, suspected spinal injury,
            or a serious burn, tell the user to call their local emergency service immediately.
            Do not diagnose, replace a clinician, or claim that a person is safe.
            Keep answers clear and action-first. Include source URLs returned by the guidance tool.
            """;

    private final ChatClient chatClient;

    public AccidentChatService(ChatModel chatModel, AccidentTools accidentTools) {
        this.chatClient = ChatClient.builder(chatModel)
                .defaultSystem(SYSTEM_PROMPT)
                .defaultTools(accidentTools)
                .build();
    }

    public String chat(String message) {
        return chatClient.prompt().user(message).call().content();
    }
}

