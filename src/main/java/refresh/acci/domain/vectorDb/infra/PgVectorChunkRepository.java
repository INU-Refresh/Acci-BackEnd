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

        jdbcTemplate.execute((java.sql.Connection con) -> {
            // 진짜 insert 커넥션 로그
            try (var st = con.createStatement()) {
                try (var rs = st.executeQuery("SHOW client_encoding")) {
                    if (rs.next()) {
                        log.info("INSERT(conn={}) client_encoding={}", System.identityHashCode(con), rs.getString(1));
                    }
                }
                st.execute("SET client_encoding TO 'UTF8'");
                try (var rs2 = st.executeQuery("SHOW client_encoding")) {
                    if (rs2.next()) log.info("AFTER SET(conn={}) client_encoding={}", System.identityHashCode(con), rs2.getString(1));
                }
            }

            // PreparedStatement는 con에서 만들고, 값 바인딩 후 실행
            try (var ps = con.prepareStatement(sql)) {
                ps.setObject(1, accidentType);
                ps.setString(2, docName);
                ps.setObject(3, page);
                ps.setString(4, section);
                ps.setString(5, caseId);

                byte[] chunkBytes = chunkText.getBytes(java.nio.charset.StandardCharsets.UTF_8);
                ps.setBytes(6, chunkBytes);

                ps.setString(7, vectorLiteral(embedding));
                ps.executeUpdate();
            }
            return null;
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
