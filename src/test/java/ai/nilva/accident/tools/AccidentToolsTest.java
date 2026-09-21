package ai.nilva.accident.tools;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.web.client.RestClient;

class AccidentToolsTest {

    private final AccidentTools tools = new AccidentTools(mock(VectorStore.class), RestClient.builder());

    @Test
    @SuppressWarnings("unchecked")
    void unresponsivePersonIsAnEmergencyWithoutGuessingMissingFacts() {
        Map<String, Object> result = tools.assessAccidentUrgency(true, false, null, null, null, null);

        assertThat(result.get("priority")).isEqualTo("EMERGENCY_NOW");
        assertThat((List<String>) result.get("emergencyReasons")).contains("unresponsive person");
        assertThat((List<String>) result.get("questionsToAsk"))
                .contains("Are they breathing normally, not only gasping?");
    }

    @Test
    void invalidCoordinatesAreRejectedBeforeCallingWeatherApi() {
        assertThat(tools.checkRoadWeather(91, 20)).containsEntry("error", "Coordinates are outside the valid WGS84 range.");
    }
}
