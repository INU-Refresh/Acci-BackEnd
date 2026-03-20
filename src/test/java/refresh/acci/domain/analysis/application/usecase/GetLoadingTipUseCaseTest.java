package refresh.acci.domain.analysis.application.usecase;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import refresh.acci.domain.analysis.application.port.out.LoadingTipPort;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class GetLoadingTipUseCaseTest {

    private LoadingTipPort loadingTipPort;
    private GetLoadingTipUseCase getLoadingTipUseCase;

    @BeforeEach
    void setUp() {
        loadingTipPort = mock(LoadingTipPort.class);
        getLoadingTipUseCase = new GetLoadingTipUseCase(loadingTipPort);
    }

    @Test
    @DisplayName("로딩 팁을 조회한다.")
    void getLoadingTip() {
        // given
        String expectedTip = "안전거리를 유지하세요";
        when(loadingTipPort.getLoadingTip()).thenReturn(expectedTip);

        // when
        String result = getLoadingTipUseCase.getLoadingTip();

        // then
        assertThat(result).isEqualTo(expectedTip);
        verify(loadingTipPort, times(1)).getLoadingTip();
    }
}
