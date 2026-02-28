package refresh.acci.domain.vectorDb.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import refresh.acci.domain.vectorDb.domain.repository.PgVectorChunkRepository;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/rag")
public class RagHealthController {

    @Qualifier("vectorDbJdbcTemplate")
    private final JdbcTemplate vectorJdbcTemplate;
    private final PgVectorChunkRepository repo;

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
}
