package refresh.acci.domain.vectorDb.presentation;

import lombok.RequiredArgsConstructor;
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
public class RagController {

    private final PdfIndexingService pdfIndexingService;
    private final PgVectorChunkRepository repo;

    @PostMapping("/index")
    public String index(
            @RequestParam String path,
            @RequestParam String docName,
            @RequestParam(required = false) Integer accidentType,
            @RequestParam(defaultValue="1") int startPage,
            @RequestParam(defaultValue="999999") int endPage
    ) throws Exception {
        pdfIndexingService.indexPdf(Path.of(path), docName, accidentType, startPage, endPage);
        return "done. count=" + repo.countChunks();
    }
}
