package refresh.acci.domain.vectorDb.application;

import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import refresh.acci.domain.vectorDb.infra.PgVectorChunkRepository;
import refresh.acci.domain.vectorDb.presentation.dto.res.SectionBlock;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PdfIndexingService {

    private final PgVectorChunkRepository pgVectorChunkRepository;
    private final GeminiEmbeddingService geminiEmbeddingService;
    private final PageToAccidentTypeMapper pageToAccidentTypeMapper;

    // 법규 헤더
    private static final List<String> LAW_HEADERS = List.of("관련 법규", "관련법규", "도로교통법");
    // 판례 헤더
    private static final List<String> PRECEDENT_HEADERS = List.of("참고 판례", "참고판례");

    /**
     * PDF를 읽어서 페이지별 텍스트를 추출하고,
     * (1) 페이지 -> 사고유형 매핑(mappedType)
     * (2) 페이지 텍스트를 MAIN/LAW/PRECEDENT로 분리(section 태깅)
     * (3) chunk로 쪼개고 임베딩을 생성한 뒤
     * (4) pgvector 테이블(legal_chunks)에 저장한다.
     *
     * @param pdfPath   내부의 PDF 경로
     * @param docName   DB에 저장할 문서명
     * @param startPage 시작 페이지(1부터)
     * @param endPage   종료 페이지
     */
    public void indexPdf(Path pdfPath, String docName, int startPage, int endPage) throws IOException {
        // startPage가 1이면 전체 새로 인덱싱이므로 기존 docName에 해당하는 데이터 삭제
        if (startPage == 1) {
            pgVectorChunkRepository.deleteByDocName(docName);
        }

        // PDFBox로 PDF 로드
        try (PDDocument doc = PDDocument.load(pdfPath.toFile())) {
            PDFTextStripper stripper = new PDFTextStripper();

            // PDF 총 페이지 수
            int pages = doc.getNumberOfPages();

            // startPage, endPage 범위 보정
            int from = Math.max(1, startPage);
            int to = Math.min(pages, endPage);

            // 페이지 단위로 처리
            for (int page = from; page <= to; page++) {
                // 이번 루프에서 읽을 범위를 해당 페이지만으로 설정
                stripper.setStartPage(page);
                stripper.setEndPage(page);

                // 페이지 텍스트 추출
                String pageText = stripper.getText(doc).trim();
                if (pageText.isBlank()) continue;

                // 사고유형 매핑: (page -> accidentType)
                // 매핑된 타입이 없으면 skip
                Integer mappedType = pageToAccidentTypeMapper.findTypeByPage(page);
                if (mappedType == null) continue;

                // 페이지 텍스트를 섹션별로 분리 (MAIN / LAW / PRECEDENT)
                List<SectionBlock> blocks = splitBySection(pageText);

                // 섹션 단위로 저장
                for (SectionBlock block : blocks) {
                    // 섹션 텍스트가 너무 짧으면 저장 의미가 떨어져서 skip
                    if (block.text().isBlank() || block.text().length() < 30) continue;

                    // 섹션 텍스트를 chunk로 자른다
                    for (String chunk : chunk(block.text(), 2000, 250)) {
                        // 임베딩 생성
                        float[] emb = geminiEmbeddingService.embed(chunk);

                        // DB에 저장
                        pgVectorChunkRepository.insertChunk(
                                mappedType,
                                docName,
                                page,
                                block.section(),
                                null,
                                chunk,
                                emb
                        );

                        // Gemini 레이트리밋/쿼터 방지용 딜레이
                        Thread.sleep(1000);
                    }
                }
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 페이지 텍스트를 MAIN/LAW/PRECEDENT 구간으로 분리
     *
     * 로직:
     * 1) LAW 헤더의 "첫 등장 인덱스" 찾기
     * 2) PRECEDENT 헤더의 "첫 등장 인덱스" 찾기
     * 3) 등장한 헤더들을 등장 순서대로 정렬
     * 4) (헤더 이전) = MAIN, (헤더 ~ 다음헤더) = 해당 section
     */
    private List<SectionBlock> splitBySection(String pageText) {
        // PDF에서 줄바꿈이 \r\n 섞여 들어오는 경우가 있어 \r 제거
        String normalized = pageText.replace("\r", "");

        // LAW 헤더가 처음 등장하는 위치
        int lawIdx = findFirstIndex(normalized, LAW_HEADERS);
        // PRECEDENT 헤더가 처음 등장하는 위치
        int precIdx = findFirstIndex(normalized, PRECEDENT_HEADERS);

        // 둘 다 없으면 MAIN만
        if (lawIdx < 0 && precIdx < 0) {
            return List.of(new SectionBlock("MAIN", normalized));
        }

        // 발견된 헤더(마커)들을 리스트로 모으고 등장 순서대로 정렬
        List<Marker> markers = new ArrayList<>();
        if (lawIdx >= 0) markers.add(new Marker("LAW", lawIdx));
        if (precIdx >= 0) markers.add(new Marker("PRECEDENT", precIdx));
        markers.sort(Comparator.comparingInt(a -> a.idx));

        List<SectionBlock> blocks = new ArrayList<>();

        // 첫 마커 이전 텍스트는 MAIN
        int first = markers.get(0).idx;
        if (first > 0) {
            blocks.add(new SectionBlock("MAIN", normalized.substring(0, first).trim()));
        }

        // 마커 구간들: marker[i] ~ marker[i+1] (마지막은 끝까지)
        for (int i = 0; i < markers.size(); i++) {
            int start = markers.get(i).idx;
            int end = (i + 1 < markers.size()) ? markers.get(i + 1).idx : normalized.length();
            String sec = markers.get(i).section;
            blocks.add(new SectionBlock(sec, normalized.substring(start, end).trim()));
        }

        return blocks;
    }

    /**
     * 섹션 시작점을 표현하는 내부 클래스
     */
    private static class Marker {
        String section; // "LAW" or "PRECEDENT"
        int idx;        // 텍스트 내 시작 위치
        Marker(String section, int idx) {
            this.section = section;
            this.idx = idx;
        }
    }

    /**
     * 텍스트 내에서 keywords 중 "가장 먼저 등장하는 인덱스"를 반환
     * 없으면 -1
     */
    private int findFirstIndex(String text, List<String> keywords) {
        int best = -1;
        for (String k : keywords) {
            int idx = text.indexOf(k);
            if (idx >= 0 && (best == -1 || idx < best)) best = idx;
        }
        return best;
    }

    /**
     * 단순 문자 기반 chunking
     * - size: chunk 최대 길이
     * - overlap: 다음 chunk가 이전 chunk와 겹치는 길이
     */
    private List<String> chunk(String text, int size, int overlap) {
        List<String> out = new ArrayList<>();
        int start = 0;
        while (start < text.length()) {
            int end = Math.min(text.length(), start + size);
            out.add(text.substring(start, end));
            if (end == text.length()) break;
            start = end - overlap;
            if (start < 0) start = 0;
        }
        return out;
    }
}
