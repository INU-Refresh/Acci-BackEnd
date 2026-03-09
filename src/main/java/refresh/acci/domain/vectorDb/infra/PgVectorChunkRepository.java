package refresh.acci.domain.vectorDb.infra;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import refresh.acci.domain.vectorDb.presentation.dto.res.LegalChunkRow;

import java.util.List;

@Slf4j
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
        VALUES (?, ?, ?, ?, ?, convert_from(?::bytea, 'UTF8'), ?::vector)
        """;

        jdbcTemplate.update(con -> {
            var ps = con.prepareStatement(sql);
            ps.setObject(1, accidentType);
            ps.setString(2, docName);
            ps.setObject(3, page);
            ps.setString(4, section);
            ps.setString(5, caseId);
            ps.setString(6, chunkText);
            ps.setString(7, vectorLiteral(embedding));
            return ps;
        });
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

        return jdbcTemplate.query(
                sql,
                rowMapper,
                qVec,
                accidentTypeFilter, accidentTypeFilter,
                qVec,
                topK);
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

    // 법규 및 판례 관련 청크를 사고유형별로 선별하여 반환하는 메서드들
    public List<LegalChunkRow> pickLawChunks(Integer accidentType, int limit) {
        String sql = """
        SELECT id, accident_type, doc_name, page, section, case_id, chunk_text,
               0.0 AS distance
        FROM legal_chunks
        WHERE (? IS NULL OR accident_type = ?)
          AND section = 'LAW'
        ORDER BY page ASC, id ASC
        LIMIT ?
        """;
        return jdbcTemplate.query(sql, rowMapper, accidentType, accidentType, limit);
    }

    public List<LegalChunkRow> pickPrecedentChunks(Integer accidentType, int limit) {
        String sql = """
        SELECT id, accident_type, doc_name, page, section, case_id, chunk_text,
               0.0 AS distance
        FROM legal_chunks
        WHERE (? IS NULL OR accident_type = ?)
          AND section = 'PRECEDENT'
        ORDER BY page ASC, id ASC
        LIMIT ?
        """;
        return jdbcTemplate.query(sql, rowMapper, accidentType, accidentType, limit);
    }

    private final RowMapper<LegalChunkRow> rowMapper = (rs, rowNum) -> new LegalChunkRow(
            rs.getLong("id"),
            (Integer) rs.getObject("accident_type"),
            rs.getString("doc_name"),
            (Integer) rs.getObject("page"),
            rs.getString("section"),
            rs.getString("case_id"),
            rs.getString("chunk_text"),
            rs.getDouble("distance")
    );

    public int deleteByDocName(String docName) {
        String sql = "DELETE FROM legal_chunks WHERE doc_name = ?";
        return jdbcTemplate.update(sql, docName);
    }
}
