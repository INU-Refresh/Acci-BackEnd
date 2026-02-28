package refresh.acci.domain.vectorDb.application;

import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import refresh.acci.domain.vectorDb.infra.PgVectorChunkRepository;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PdfIndexingService {

    private final PgVectorChunkRepository pgVectorChunkRepository;
    private final GeminiEmbeddingService geminiEmbeddingService;

    public void indexPdf(Path pdfPath, String docName, Integer accidentType) throws IOException {
        pgVectorChunkRepository.deleteByDocName(docName);

        try (PDDocument doc = PDDocument.load(pdfPath.toFile())) {
            PDFTextStripper stripper = new PDFTextStripper();
            int pages = doc.getNumberOfPages();

            for (int page = 1; page <= pages; page++) {
                stripper.setStartPage(page);
                stripper.setEndPage(page);
                String text = stripper.getText(doc).trim();
                if (text.isBlank()) continue;

                for (String chunk : chunk(text, 900, 150)) {
                    float[] emb = geminiEmbeddingService.embed(chunk);

                    pgVectorChunkRepository.insertChunk(
                            accidentType,
                            docName,
                            page,
                            null,
                            null,
                            chunk,
                            emb
                    );

                    Thread.sleep(200);
                }
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

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
