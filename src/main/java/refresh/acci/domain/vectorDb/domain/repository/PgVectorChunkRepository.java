package refresh.acci.domain.vectorDb.domain.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import refresh.acci.domain.vectorDb.presentation.dto.res.LegalChunkRow;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class PgVectorChunkRepository {

    @Qualifier("vectorDbJdbcTemplate")
    private final JdbcTemplate jdbcTemplate;

    public void insertChunk(Integer accidentType,
                            String docName,
                            Integer page,
                            String section,
                            String caseId,
                            String chunkText,
                            float[] embedding) {
        String sql = """
                INSERT INTO legal_chunks(accident_type, doc_name, page, section, case_id, chunk_text, embedding)
                VALUES (?, ?, ?, ?, ?, ?, ?::vector)
                """;

        jdbcTemplate.update(sql, accidentType, docName, page, section, caseId, chunkText, vectorLiteral(embedding));
    }

    public List<LegalChunkRow> searchTopK(
            Integer accidentTypeFilter,
            float[] queryEmbedding,
            int topK
    ) {
        // cosine distance: embedding <=> query (작을수록 유사)
        String sql = """
                SELECT id, accident_type, doc_name, page, section, case_id, chunk_text,
                       (embedding <=> ?::vector) AS distance
                FROM legal_chunks
                WHERE embedding IS NOT NULL
                AND (? IS NULL OR accident_type = ?)
                ORDER BY embedding <=> ?::vector
                LIMIT ?
                """;

        String qVec = vectorLiteral(queryEmbedding);

        return jdbcTemplate.query(sql,
                (rs, rowNum) -> new LegalChunkRow(
                        rs.getLong("id"),
                        (Integer) rs.getObject("accident_type"),
                        rs.getString("doc_name"),
                        (Integer) rs.getObject("page"),
                        rs.getString("section"),
                        rs.getString("case_id"),
                        rs.getString("chunk_text"),
                        rs.getDouble("distance")
                ),
                qVec,
                accidentTypeFilter, accidentTypeFilter,
                qVec,
                topK);
    }

    public void insertChunkWithoutEmbedding(Integer accidentType, String docName, Integer page, String chunkText) {
        jdbcTemplate.update("""
        INSERT INTO legal_chunks(accident_type, doc_name, page, chunk_text)
        VALUES (?, ?, ?, ?)
    """, accidentType, docName, page, chunkText);
    }

    public Integer countChunks() {
        return jdbcTemplate.queryForObject("SELECT COUNT(*) FROM legal_chunks", Integer.class);
    }

    // float[]를 PostgreSQL의 vector 리터럴 형식으로 변환하는 헬퍼 메서드
    // pgvector 입력 리터럴: '[0.1,0.2,...]'
    private String vectorLiteral(float[] vector) {
        StringBuilder sb = new StringBuilder();
        sb.append('[');
        for (int i = 0; i < vector.length; i++) {
            if (i > 0) sb.append(',');
            sb.append(vector[i]);
        }
        sb.append(']');
        return sb.toString();
    }
}
