package cn.zjiali.jd.monitor.ql;

import cn.zjiali.jd.monitor.manager.NotifyManager;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public abstract class TaskQueueProcessor {

    @Autowired
    private NotifyManager notifyManager;

    @Autowired
    private QLClient qlClient;

    protected static final Logger logger = LoggerFactory.getLogger(TaskQueueProcessor.class);

    abstract void addCron(CronContext element);

    protected boolean checkCronStatus(CronContext cronContext) {
        try {
            List<Cron.CronInfo> cronInfoList = qlClient.getCron(cronContext.script());
            return cronInfoList.stream().filter(cronInfo -> cronInfo.status() == 0).findFirst().isEmpty();
        } catch (Exception e) {
            logger.error("检查任务状态失败:{}", ExceptionUtils.getStackTrace(e));
            return false;
        }
    }

    protected void processCron(CronContext cronContext) {
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
}
