package refresh.acci.domain.analysis.adapter.out.executor;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import refresh.acci.domain.analysis.application.port.out.TaskExecutorPort;

import java.util.concurrent.Executor;

@Component
@RequiredArgsConstructor
public class SpringTaskExecutorAdapter implements TaskExecutorPort {

    private final Executor analysisExecutor;

    @Override
    public void execute(Runnable task) {
        analysisExecutor.execute(task);
    }
}
