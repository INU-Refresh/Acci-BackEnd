package refresh.acci.domain.vectorDb.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import refresh.acci.domain.vectorDb.presentation.dto.res.AccidentPageRange;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

@Service
public class PageToAccidentTypeMapper {

    private final List<AccidentPageRange> ranges;

    public PageToAccidentTypeMapper(ObjectMapper objectMapper) throws IOException {
        Path path = Path.of("/opt/acci/docs/accident_page_map.json");
        this.ranges = Arrays.asList(
                objectMapper.readValue(path.toFile(), AccidentPageRange[].class)
        );
    }

    public Integer findTypeByPage(int page) {
        for (AccidentPageRange r : ranges) {
            if (page >= r.start() && page <= r.end()) return r.type();
        }
        return null;
    }
}
