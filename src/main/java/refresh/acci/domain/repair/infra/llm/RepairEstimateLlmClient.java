package refresh.acci.domain.repair.infra.llm;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import refresh.acci.domain.repair.infra.llm.dto.OpenAiRequest;
import refresh.acci.domain.repair.infra.llm.dto.OpenAiResponse;
import refresh.acci.domain.repair.infra.llm.dto.RepairEstimateLlmResponse;
import refresh.acci.global.exception.CustomException;
import refresh.acci.global.exception.ErrorCode;

import java.time.Duration;
import java.util.List;

@Slf4j
@Component
public class RepairEstimateLlmClient {

    private static final String CHAT_COMPLETIONS_URI = "/chat/completions";
    private static final Duration API_TIMEOUT = Duration.ofMinutes(2);
    private static final String JSON_RESPONSE_TYPE = "json_object";
    private static final String SYSTEM_ROLE = "system";
    private static final String USER_ROLE = "user";

    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private final String model;
    private final int maxTokens;
    private final double temperature;

    public RepairEstimateLlmClient(
            WebClient.Builder webClientBuilder,
            ObjectMapper objectMapper,
            @Value("${openai.api-key}") String apiKey,
            @Value("${openai.base-url}") String baseUrl,
            @Value("${openai.model}") String model,
            @Value("${openai.max-tokens}") int maxTokens,
            @Value("${openai.temperature}") double temperature) {
        this.webClient = webClientBuilder
                .baseUrl(baseUrl)
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .build();
        this.objectMapper = objectMapper;
        this.model = model;
        this.maxTokens = maxTokens;
        this.temperature = temperature;
    }

    //penAI API를 호출하여 수리비 견적을 생성
    public RepairEstimateLlmResponse call(String systemMessage, String userPrompt, String imageBase64) {
        OpenAiRequest requestBody = buildRequestBody(systemMessage, userPrompt, imageBase64);

        try {
            String responseString = webClient.post()
                    .uri(CHAT_COMPLETIONS_URI)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .retrieve()
                    .onStatus(
                            status -> status.is4xxClientError() || status.is5xxServerError(),
                            response -> response.bodyToMono(String.class)
                                    .flatMap(body -> {
                                        log.error("OpenAI API 호출 실패 - Status: {}, Body: {}",
                                                response.statusCode(), body);
                                        return Mono.error(new CustomException(ErrorCode.LLM_API_CALL_FAILED));
                                    })
                    )
                    .bodyToMono(String.class)
                    .timeout(API_TIMEOUT)
                    .block();

            return parseResponse(responseString);

        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.error("LLM API 호출 중 예기치 않은 오류 발생", e);
            throw new CustomException(ErrorCode.LLM_API_CALL_FAILED);
        }
    }

    //OpenAI 요청 바디 생성
    private OpenAiRequest buildRequestBody(String systemMessage, String userPrompt, String imageBase64) {
        OpenAiRequest.Message userMessage = buildUserMessage(userPrompt, imageBase64);

        return OpenAiRequest.builder()
                .model(model)
                .messages(List.of(
                        new OpenAiRequest.Message(SYSTEM_ROLE, systemMessage),
                        userMessage
                ))
                .responseFormat(new OpenAiRequest.ResponseFormat(JSON_RESPONSE_TYPE))
                .maxCompletionTokens(maxTokens)
                /*
                .temperature(temperature) 일부 모델에서 변경 불가 -> 기본값 사용
                */
                .build();
    }

    //이미지 유무에 따라 user 메시지 구성
    private OpenAiRequest.Message buildUserMessage(String userPrompt, String imageBase64) {
        //텍스트만
        if (imageBase64 == null) {
            return new OpenAiRequest.Message(USER_ROLE, userPrompt);
        }

        //텍스트 + 이미지 배열
        List<OpenAiRequest.ContentPart> contentParts = List.of(
                OpenAiRequest.ContentPart.ofText(userPrompt),
                OpenAiRequest.ContentPart.ofImage(imageBase64, "image/jpeg")
        );
        return new OpenAiRequest.Message(USER_ROLE, contentParts);
    }

    //OpenAI 응답 파싱
    private RepairEstimateLlmResponse parseResponse(String responseString) {
        try {
            //OpenAI 응답 전체를 OpenAiResponse 객체로 변환
            OpenAiResponse openAiResponse = objectMapper.readValue(responseString, OpenAiResponse.class);

            //choices[0].message.content 추출
            String content = openAiResponse.getChoices().stream()
                    .findFirst()
                    .map(OpenAiResponse.Choice::getMessage)
                    .map(OpenAiResponse.Message::getContent)
                    .filter(text -> !text.isBlank())
                    .orElseThrow(() -> {
                        log.error("OpenAI 응답에 content가 없습니다. Raw Response: {}", responseString);
                        return new CustomException(ErrorCode.LLM_RESPONSE_PARSE_FAILED);
                    });

            log.info("LLM content raw: {}", content);

            //content(JSON 문자열)를 RepairEstimateLlmResponse 객체로 변환
            return objectMapper.readValue(content, RepairEstimateLlmResponse.class);

        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.error("LLM 응답 파싱 실패: {}", responseString, e);
            throw new CustomException(ErrorCode.LLM_RESPONSE_PARSE_FAILED);
        }
    }
}