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

    public record Message(String role, Object content) {}

    public record ResponseFormat(String type) {}

    @Getter
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ContentPart {
        private String type;        //"text" or "image_url"

        private String text;        //type이 text

        @JsonProperty("image_url")
        private ImageUrl imageUrl;  //type이 image_url

        public static ContentPart ofText(String text) {
            return ContentPart.builder()
                    .type("text")
                    .text(text)
                    .build();
        }

        public static ContentPart ofImage(String base64, String mediaType) {
            return ContentPart.builder()
                    .type("image_url")
                    .imageUrl(new ImageUrl("data:" + mediaType + ";base64," + base64))
                    .build();
        }
    }

    public record ImageUrl(String url) {}
}
