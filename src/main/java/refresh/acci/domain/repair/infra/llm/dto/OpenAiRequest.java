package refresh.acci.domain.repair.infra.llm.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OpenAiRequest {

    private String model;

    private List<Message> messages;

    @JsonProperty("response_format")
    private ResponseFormat responseFormat;

    @JsonProperty("max_completion_tokens")
    private Integer maxCompletionTokens;

    private Double temperature;

    public record Message(String role, String content) {}

    public record ResponseFormat(String type) {}
}
