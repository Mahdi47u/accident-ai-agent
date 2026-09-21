package ai.nilva.accident.api;

import ai.nilva.accident.chat.AccidentChatService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class ChatController {

    private final AccidentChatService chatService;

    public ChatController(AccidentChatService chatService) {
        this.chatService = chatService;
    }

    @GetMapping("/health")
    Map<String, String> health() {
        return Map.of("status", "ok");
    }

    @PostMapping("/chat")
    ChatResponse chat(@Valid @RequestBody ChatRequest request) {
        return new ChatResponse(chatService.chat(request.message()));
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.BAD_GATEWAY)
    Map<String, String> handleFailure(Exception exception) {
        return Map.of(
                "error", "The AI service could not answer right now.",
                "detail", exception.getClass().getSimpleName());
    }

    public record ChatRequest(@NotBlank @Size(max = 4_000) String message) {}

    public record ChatResponse(String answer) {}
}

