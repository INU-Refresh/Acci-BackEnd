package refresh.acci.domain.vectorDb.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import refresh.acci.domain.vectorDb.domain.repository.PgVectorChunkRepository;
import refresh.acci.domain.vectorDb.port.out.EmbeddingPort;
import refresh.acci.domain.vectorDb.presentation.dto.res.LegalChunkRow;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/rag")
public class RagHealthController {

    @Qualifier("vectorDbJdbcTemplate")
    private final JdbcTemplate vectorJdbcTemplate;
    private final PgVectorChunkRepository repo;
    private final EmbeddingPort embeddingPort;

    @GetMapping("/health")
    public String health() {
        Integer one = vectorJdbcTemplate.queryForObject("SELECT 1", Integer.class);
        return "OK " + one;
    }

    @PostMapping("/insert")
    public String insert() {
        repo.insertChunkWithoutEmbedding(11, "sample.pdf", 1, "테스트 청크입니다.");
        return "count=" + repo.countChunks();
    }

    @GetMapping("/embed")
    public Map<String, Object> embed(@RequestParam String q) {
        float[] v = embeddingPort.embed(q);
        return Map.of(
                "dim", v.length,
                "sample", new float[]{ v[0], v[1], v[2] }
        );
    }

    @PostMapping("/e2e")
    public List<LegalChunkRow> e2e() {
        String chunk = "직선 도로에서 선행 차량이 차로변경 중 후행 차량과 충돌";
        float[] emb = embeddingPort.embed(chunk);

        repo.insertChunk(11, "test", 1, "test", "test", chunk, emb);

        String query = "차로변경하다가 뒤차와 부딪힘";
        float[] qEmb = embeddingPort.embed(query);

        return repo.searchTopK(11, qEmb, 5);
    }
}
