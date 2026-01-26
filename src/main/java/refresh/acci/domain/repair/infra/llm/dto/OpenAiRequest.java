package refresh.acci.domain.repair.infra.llm.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class OpenAiRequest {

    private String model;

    private List<Message> messages;

    @JsonProperty("response_format")
    private ResponseFormat responseFormat;

    @JsonProperty("max_tokens")
    private int maxTokens;

    private double temperature;

    public record Message(String role, String content) {}

    public record ResponseFormat(String type) {}
}
