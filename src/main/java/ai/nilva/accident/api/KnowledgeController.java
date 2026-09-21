package ai.nilva.accident.api;

import ai.nilva.accident.knowledge.AccidentKnowledgeService;
import java.util.Map;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/knowledge")
public class KnowledgeController {

    private final AccidentKnowledgeService knowledgeService;

    public KnowledgeController(AccidentKnowledgeService knowledgeService) {
        this.knowledgeService = knowledgeService;
    }

    @PostMapping("/seed")
    Map<String, Object> seed() {
        int inserted = knowledgeService.seed();
        return Map.of("inserted", inserted, "message", inserted == 0 ? "Knowledge already exists." : "Knowledge indexed.");
    }
}

