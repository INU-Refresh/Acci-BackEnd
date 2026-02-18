package refresh.acci.domain.analysis.application.usecase;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import refresh.acci.domain.analysis.application.port.out.LoadingTipPort;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetLoadingTipUseCase {

    private final LoadingTipPort loadingTipPort;

    public String getLoadingTip() {
        return loadingTipPort.getLoadingTip();
    }
}
