package cn.zjiali.jd.monitor.tg;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author zJiaLi
 * @since 2023-03-07 16:34
 */
@Component
@ConditionalOnProperty(value = "tg.enable", havingValue = "true")
public class TgClientLifecycle implements SmartLifecycle {


    private final AtomicBoolean startFlag = new AtomicBoolean(false);

    private final TgClientManager tgClientManager;

    public TgClientLifecycle(TgClientManager tgClientManager) {
        this.tgClientManager = tgClientManager;
    }


    @Override
    public void start() {
        tgClientManager.start();
        startFlag.set(true);
    }

    @Override
    public void stop() {
        tgClientManager.stop();
    }

    @Override
    public boolean isRunning() {
        return startFlag.get();
    }


}
