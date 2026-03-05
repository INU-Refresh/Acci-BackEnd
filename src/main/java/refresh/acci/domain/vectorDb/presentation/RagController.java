package refresh.acci.domain.vectorDb.presentation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import refresh.acci.domain.vectorDb.application.PdfIndexingService;
import refresh.acci.domain.vectorDb.infra.PgVectorChunkRepository;

import java.nio.file.Path;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/rag")
@Tag(name = "RAG API", description = "RAG 관련 API")
public class RagController {

    private final PdfIndexingService pdfIndexingService;
    private final PgVectorChunkRepository repo;

    @Operation(summary = "PDF 인덱싱", description = "PDF 파일을 인덱싱하여 벡터 DB에 저장합니다.")
    @PostMapping("/index")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public String index(
            @RequestParam String path,
            @RequestParam String docName,
            @RequestParam(defaultValue="1") int startPage,
            @RequestParam(defaultValue="999999") int endPage
    ) throws Exception {
        pdfIndexingService.indexPdf(Path.of(path), docName, startPage, endPage);
        return "done. count=" + repo.countChunks();
    }
}