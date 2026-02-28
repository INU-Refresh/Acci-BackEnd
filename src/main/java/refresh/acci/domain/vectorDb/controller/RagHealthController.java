package refresh.acci.domain.vectorDb.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import refresh.acci.domain.vectorDb.domain.repository.PgVectorChunkRepository;
import refresh.acci.domain.vectorDb.port.out.EmbeddingPort;

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
}
