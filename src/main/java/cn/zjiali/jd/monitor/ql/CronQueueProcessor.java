package cn.zjiali.jd.monitor.ql;

import jakarta.annotation.PostConstruct;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.*;

/**
 * @author zJiaLi
 * @since 2023-03-15 09:24
 */
//@Component(value = "CronQueueProcessor")
public class CronQueueProcessor extends TaskQueueProcessor {

    private final ScheduledExecutorService executor;
    private final BlockingQueue<CronContext> runCronQueue;
    private final BlockingQueue<CronContext> waitCronQueue;

    public CronQueueProcessor() {
        this.executor = Executors.newScheduledThreadPool(2, new CustomizableThreadFactory("cron-q-pool-"));
        this.runCronQueue = new LinkedBlockingQueue<>();
        this.waitCronQueue = new LinkedBlockingQueue<>();
    }

    @PostConstruct
    public void init() {
        start();
    }

    public void start() {
        // 每3s执行一次
        executor.scheduleAtFixedRate(new ProcessRunQueueTask(), 0, 5, TimeUnit.SECONDS);
        // 每30s执行一次
        executor.scheduleAtFixedRate(new ProcessWaitQueueTask(), 0, 30, TimeUnit.SECONDS);
        logger.info("青龙任务队列已启动");
    }

    @Override
    public void addCron(CronContext element) {
        runCronQueue.add(element);
    }


    private class ProcessWaitQueueTask implements Runnable {

        @Override
        public void run() {
            while (!waitCronQueue.isEmpty()) {
                CronContext cronContext = waitCronQueue.poll();
                if (checkCronStatus(cronContext)) {
                    // 元素状态符合条件，处理业务逻辑
                    processCron(cronContext);
                } else {
                    // 元素状态不符合，放回队列
                    waitCronQueue.add(cronContext);
                }
            }
        }

    }

    private class ProcessRunQueueTask implements Runnable {
        @Override
        public void run() {
            while (!runCronQueue.isEmpty()) {
                CronContext cronContext = runCronQueue.poll();
                if (checkCronStatus(cronContext)) {
                    // 元素状态符合条件，处理业务逻辑
                    processCron(cronContext);
                } else {
                    // 元素状态不符合，放回队列
                    waitCronQueue.add(cronContext);
                }
            }
        }

    }

}
