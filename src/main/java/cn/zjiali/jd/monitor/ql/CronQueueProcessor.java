package cn.zjiali.jd.monitor.ql;

import cn.zjiali.jd.monitor.manager.NotifyManager;
import jakarta.annotation.PostConstruct;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * @author zJiaLi
 * @since 2023-03-15 09:24
 */
@Component
public class CronQueueProcessor {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final ScheduledExecutorService executor;
    private final BlockingQueue<CronContext> runCronQueue;
    private final BlockingQueue<CronContext> waitCronQueue;
    private final NotifyManager notifyManager;

    private final QLClient qlClient;

    public CronQueueProcessor(NotifyManager notifyManager, QLClient qlClient) {
        this.notifyManager = notifyManager;
        this.qlClient = qlClient;
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

    public void addCron(CronContext element) {
        runCronQueue.add(element);
    }

    private boolean checkCronStatus(CronContext cronContext) {
        try {
            List<Cron.CronInfo> cronInfoList = qlClient.getCron(cronContext.script());
            return cronInfoList.stream().filter(cronInfo -> cronInfo.status() == 0).findFirst().isEmpty();
        } catch (Exception e) {
            logger.error("检查任务状态失败:{}", ExceptionUtils.getStackTrace(e));
            return false;
        }
    }

    private void processCron(CronContext cronContext) {
        List<String> logList = new ArrayList<>();
        cronContext.envs().forEach(env -> {
            qlClient.updateEnv(env.name(), env.value(), env.remarks());
            logList.add("替换变量[%s] => [%s]".formatted(env.name(), env.value()));
        });
        qlClient.runCron(cronContext.cronName(), cronContext.script());

        logList.add("执行任务[%s]".formatted(cronContext.cronName()));
        logList.forEach(logger::info);
        if (notifyManager.enable() && !logList.isEmpty()) {
            notifyManager.sendNotify("触发任务",
                    logList.stream().map(String::trim)
                            .collect(Collectors.joining("\n")));
        }
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
