package refresh.acci.domain.vectorDb.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import refresh.acci.domain.vectorDb.presentation.dto.res.LegalChunkHit;
import refresh.acci.domain.vectorDb.presentation.dto.res.RagInfoResponse;
import refresh.acci.domain.vectorDb.presentation.dto.res.RagSummaryResponse;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RagSummaryService {

    private final GeminiGenerateClient geminiGenerateClient;

    public RagSummaryResponse summarize(RagInfoResponse ragInfoResponse) {
        if (ragInfoResponse == null || ragInfoResponse.hits() == null || ragInfoResponse.hits().isEmpty()) {
            return null;
        }

        String context = buildContext(ragInfoResponse);

        String prompt = buildPrompt(context);

        String modelText = geminiGenerateClient.generateText(prompt);
        String json = geminiGenerateClient.extractJsonObject(modelText);

        try {
            return geminiGenerateClient.parseJson(json, RagSummaryResponse.class);
        } catch (Exception e) {
            log.warn("JSON 파싱 실패. 원본 모델 텍스트: {}", modelText, e);
            throw e;
        }
    }

    private String buildPrompt(String context) {
        return """
                너는 교통사고 과실 기준 문서를 근거로 요약하는 도우미다.
                아래 '근거 텍스트'에서만 정보를 사용해라. 추측하거나 새 사실을 만들지 마라.
                근거에 없는 내용은 "근거 없음"이라고 적어라.
                가능한 경우 각 항목 끝에 [문서명 p.X] 형태로 출처를 붙여라.
                반드시 JSON만 출력해라. (코드블록 금지, 설명 금지, 앞뒤 텍스트 금지)
                
                중요 규칙(환각 방지):
                - relatedLaws / precedentCases는 '근거 텍스트'에 해당 내용이 명시적으로 있을 때만 채워라.
                - 근거 텍스트에 관련 법규 내용이 없으면 relatedLaws는 반드시 [] 로 반환하라.
                - 근거 텍스트에 참고 판례 내용이 없으면 precedentCases는 반드시 [] 로 반환하라.
                - "있을 법하다" 같은 추측으로 법규/판례를 새로 만들어서 넣지 마라.
                - 절대 페이지/문서 출처 표기하지 마라. 예: [car_accident_law.pdf p.229] 같은 문자열을 포함하면 안 된다.
                - 과실비율 숫자(예: 100:0, 80:20 등)을 절대 작성하지 마라.

                출력 JSON 형식 (키 이름 변경 금지):
                {
                  "accidentSituation": "사고 상황 요약 (1~3문장)",
                  "accidentExplain": "과실비율/판단 근거 요약 (핵심만 3~6문장)",
                  "relatedLaws": [
                    {"lawName":"법 이름/조문", "lawContent":"관련 내용 요약"}
                  ],
                  "precedentCases": [
                    {"caseName":"판례명", "summary":"핵심 요지 요약", "dateOfJudgment":"YYYY-MM-DD 또는 null"}
                  ]
                }
                
                dateOfJudgment 규칙:
                 - 날짜가 근거 텍스트에 명확히 있으면 "YYYY-MM-DD"
                 - 없으면 null

                근거 텍스트:
                <<<%s>>>
                """.formatted(context);
    }

    private String buildContext(RagInfoResponse ragInfoResponse) {
        // distance 기준으로 상위 5개 hit만 사용
        List<LegalChunkHit> hits = ragInfoResponse.hits().stream()
                .sorted(Comparator.comparingDouble(LegalChunkHit::distance))
                .limit(5)
                .toList();

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < hits.size(); i++) {
            var h = hits.get(i);
            String text = h.chunkText();
            if (text == null) text = "";
            if (text.length() > 1800) text = text.substring(0, 1800);

            sb.append("[").append(i + 1).append("] ")
                    .append(h.docName()).append(" p.").append(h.page()).append("\n")
                    .append(text)
                    .append("\n\n");
        }
        return sb.toString();
    }
}
