package refresh.acci.domain.vectorDb.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import refresh.acci.domain.vectorDb.presentation.dto.res.LegalChunkRow;
import refresh.acci.domain.vectorDb.presentation.dto.res.RagInfoResponse;
import refresh.acci.domain.analysis.model.Analysis;
import refresh.acci.domain.analysis.model.enums.AccidentType;
import refresh.acci.domain.vectorDb.infra.PgVectorChunkRepository;
import refresh.acci.global.exception.CustomException;
import refresh.acci.global.exception.ErrorCode;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RagSearchService {

    private final PgVectorChunkRepository pgVectorChunkRepository;
    private final GeminiEmbeddingService embeddingService;

    public RagInfoResponse search(Analysis analysis) {
        int type = normalizeForSearch(analysis.getAccidentType());
        String query = buildQueryText(analysis);
        float[] qEmb = embeddingService.embed(query);

        var topK = pgVectorChunkRepository.searchTopK(type, qEmb, 5);
        var laws = pgVectorChunkRepository.pickLawChunks(type, 3);
        var precedents = pgVectorChunkRepository.pickPrecedentChunks(type, 3);

        // id 기준 중복 제거 + 합치기
        Map<Long, LegalChunkRow> merged = new LinkedHashMap<>();
        topK.forEach(r -> merged.put(r.id(), r));
        laws.forEach(r -> merged.putIfAbsent(r.id(), r));
        precedents.forEach(r -> merged.putIfAbsent(r.id(), r));

        List<RagInfoResponse.LegalChunkHit> hits = merged.values().stream()
                .map(r -> new RagInfoResponse.LegalChunkHit(
                        r.id(),
                        r.accidentType(),
                        r.docName(),
                        r.page(),
                        r.chunkText(),
                        r.distance()
                )).toList();

        return new RagInfoResponse(type, query, hits);
    }

    private String buildQueryText(Analysis a) {
        // query는 짧고 “상황 키워드”가 잘 보이게 작성
        // AccidentType 자체의 서술도 함께 넣어 검색 품질 상승
        AccidentType type = a.getAccidentType();
        String typeHint = (type == null) ? "" :
                "유형힌트: " + type.getObjectType() + ", " + type.getPlace() + ", " + type.getSituation() + ", " + type.getVehicleADirection();

        return """
                %s
                사고 장소: %s
                사고 상황: %s
                A 진행: %s
                B 진행: %s
                """.formatted(
                typeHint,
                nullToEmpty(a.getPlace()),
                nullToEmpty(a.getSituation()),
                nullToEmpty(a.getVehicleASituation()),
                nullToEmpty(a.getVehicleBSituation())
        ).trim();
    }

    private String nullToEmpty(String s) { return s == null ? "" : s; }

    private int normalizeForSearch(AccidentType t) {
        if (t == null) throw new CustomException(ErrorCode.ACCIDENT_TYPE_NOT_DETECTED);
        int i = t.getAccidentType();
        return switch (i) {
            case 8 -> 7;
            case 0 -> 1;
            case 15 -> 14;
            default -> i;
        };
    }
}
