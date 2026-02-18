package refresh.acci.domain.analysis.application.port.out;

public interface TaskExecutorPort {
    void execute(Runnable task);
}
