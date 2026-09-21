package ai.nilva.accident.tools;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.ai.document.Document;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class AccidentTools {

    private final VectorStore vectorStore;
    private final RestClient weatherClient;

    public AccidentTools(VectorStore vectorStore, RestClient.Builder restClientBuilder) {
        this.vectorStore = vectorStore;
        this.weatherClient = restClientBuilder.baseUrl("https://api.open-meteo.com").build();
    }

    @Tool(description = "Screen accident urgency using only facts explicitly provided by the user. Never guess missing values.")
    public Map<String, Object> assessAccidentUrgency(
            @ToolParam(description = "Whether the scene is safe to approach", required = false) Boolean sceneSafe,
            @ToolParam(description = "Whether the injured person is awake and responding", required = false) Boolean responsive,
            @ToolParam(description = "Whether the person is breathing normally and not merely gasping", required = false)
                    Boolean breathingNormally,
            @ToolParam(description = "Whether blood is spurting, flowing continuously, or pooling heavily", required = false)
                    Boolean lifeThreateningBleeding,
            @ToolParam(description = "Whether a head, neck, or spine injury is suspected", required = false)
                    Boolean suspectedSpineInjury,
            @ToolParam(description = "Whether a large, deep, chemical, or electrical burn is present", required = false)
                    Boolean seriousBurn) {
        List<String> reasons = new java.util.ArrayList<>();
        if (Boolean.FALSE.equals(sceneSafe)) reasons.add("unsafe scene");
        if (Boolean.FALSE.equals(responsive)) reasons.add("unresponsive person");
        if (Boolean.FALSE.equals(breathingNormally)) reasons.add("abnormal or absent breathing");
        if (Boolean.TRUE.equals(lifeThreateningBleeding)) reasons.add("life-threatening bleeding");
        if (Boolean.TRUE.equals(suspectedSpineInjury)) reasons.add("possible head, neck, or spine injury");
        if (Boolean.TRUE.equals(seriousBurn)) reasons.add("serious burn");

        List<String> missing = new java.util.ArrayList<>();
        if (sceneSafe == null) missing.add("Is the scene safe to approach?");
        if (responsive == null) missing.add("Is the injured person awake and responding?");
        if (breathingNormally == null) missing.add("Are they breathing normally, not only gasping?");
        if (lifeThreateningBleeding == null) missing.add("Is blood spurting, flowing continuously, or pooling heavily?");

        String priority = !reasons.isEmpty() ? "EMERGENCY_NOW" : missing.isEmpty() ? "NO_RED_FLAG_REPORTED" : "ASK_MORE";
        return Map.of(
                "priority", priority,
                "emergencyReasons", reasons,
                "questionsToAsk", missing,
                "instruction", !reasons.isEmpty()
                        ? "Call local emergency services now and follow the dispatcher."
                        : "Continue screening; this result is not a diagnosis.");
    }

    @Tool(description = "Search the reviewed pgvector accident guidance. Use this before giving first-response instructions.")
    public List<Map<String, Object>> searchAccidentGuidance(
            @ToolParam(description = "The accident or injury guidance to find") String query) {
        return vectorStore.similaritySearch(SearchRequest.builder().query(query).topK(3).build()).stream()
                .map(this::asResult)
                .toList();
    }

    @Tool(description = "Get current modeled weather at supplied coordinates to identify basic road-weather cautions."
            + " This does not report road closures.")
    @SuppressWarnings("unchecked")
    public Map<String, Object> checkRoadWeather(
            @ToolParam(description = "WGS84 latitude from -90 to 90") double latitude,
            @ToolParam(description = "WGS84 longitude from -180 to 180") double longitude) {
        if (latitude < -90 || latitude > 90 || longitude < -180 || longitude > 180) {
            return Map.of("error", "Coordinates are outside the valid WGS84 range.");
        }
        Map<String, Object> response = weatherClient
                .get()
                .uri(uri -> uri.path("/v1/forecast")
                        .queryParam("latitude", latitude)
                        .queryParam("longitude", longitude)
                        .queryParam(
                                "current",
                                "temperature_2m,precipitation,snowfall,visibility,wind_speed_10m,wind_gusts_10m")
                        .queryParam("timezone", "auto")
                        .build())
                .retrieve()
                .body(Map.class);
        Map<String, Object> current = response == null
                ? Map.of()
                : (Map<String, Object>) response.getOrDefault("current", Map.of());
        return Map.of(
                "conditions", current,
                "source", "https://open-meteo.com/en/docs",
                "limitation", "Modeled weather only; check local authorities for closures and road conditions.");
    }

    private Map<String, Object> asResult(Document document) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("guidance", document.getText());
        result.put("topic", document.getMetadata().get("topic"));
        result.put("source", document.getMetadata().get("source"));
        return result;
    }
}

