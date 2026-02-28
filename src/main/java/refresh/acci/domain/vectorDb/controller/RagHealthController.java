package refresh.acci.domain.vectorDb.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/rag")
public class RagHealthController {

    @Qualifier("vectorDbJdbcTemplate")
    private final JdbcTemplate vectorJdbcTemplate;

    @GetMapping("/health")
    public String health() {
        Integer one = vectorJdbcTemplate.queryForObject("SELECT 1", Integer.class);
        return "OK " + one;
    }
}
