package ai.nilva.accident.knowledge;

import java.util.List;
import java.util.Map;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class AccidentKnowledgeService {

    static final String DATASET = "accident-guidance-v1";

    private final VectorStore vectorStore;
    private final JdbcTemplate jdbcTemplate;

    public AccidentKnowledgeService(VectorStore vectorStore, JdbcTemplate jdbcTemplate) {
        this.vectorStore = vectorStore;
        this.jdbcTemplate = jdbcTemplate;
    }

    public int seed() {
        Integer count = jdbcTemplate.queryForObject(
                "select count(*) from accident_guidance where metadata->>'dataset' = ?", Integer.class, DATASET);
        if (count != null && count > 0) {
            return 0;
        }

        List<Document> documents = List.of(
                document(
                        "Scene safety",
                        "Before helping at an accident, check for traffic, fire, fuel, electricity, unstable structures, "
                                + "or other immediate danger. Do not enter an unsafe scene. Call local emergency services.",
                        "https://www.redcross.org/take-a-class/first-aid/performing-first-aid/first-aid-steps"),
                document(
                        "Severe bleeding",
                        "Call local emergency services. Apply steady, firm direct pressure over the wound with a dressing "
                                + "or clean material. Keep pressure on. Use a tourniquet only if trained and it is for "
                                + "life-threatening bleeding from a limb.",
                        "https://www.redcross.org/take-a-class/resources/learn-first-aid/bleeding-life-threatening-external"),
                document(
                        "Unresponsive or abnormal breathing",
                        "Check responsiveness and breathing for no more than 10 seconds. If the person is unresponsive, "
                                + "not breathing normally, or only gasping, call local emergency services and start CPR "
                                + "and use an AED if trained.",
                        "https://www.redcross.org/take-a-class/first-aid/performing-first-aid/first-aid-steps"),
                document(
                        "Possible head, neck, or spine injury",
                        "Do not ask the person to move. Leave them in the position found unless movement is required for "
                                + "immediate safety, CPR, or bleeding control. Call local emergency services.",
                        "https://www.redcross.org/take-a-class/first-aid/performing-first-aid/first-aid-steps"),
                document(
                        "Burn or scald",
                        "Cool a burn under cool running water for 20 minutes. Remove nearby clothing or jewellery unless "
                                + "stuck. Do not use ice, creams, oils, or butter. Seek emergency care for large, deep, "
                                + "chemical, electrical, facial, or genital burns.",
                        "https://www.nhs.uk/conditions/burns-and-scalds/"));
        vectorStore.add(documents);
        return documents.size();
    }

    private Document document(String topic, String text, String source) {
        return new Document(text, Map.of("dataset", DATASET, "topic", topic, "source", source));
    }
}

