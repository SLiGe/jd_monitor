package cn.zjiali.jd.monitor.ql;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * @author zJiaLi
 * @since 2023-03-14 14:38
 */
@Configuration
@EnableScheduling
public class QLTask {

    private final QLClient qlClient;
    private final EnvConfigManager envConfigManager;

    public QLTask(QLClient qlClient, EnvConfigManager envConfigManager) {
        this.qlClient = qlClient;
        this.envConfigManager = envConfigManager;
    }

    @Scheduled(cron = "0 0 0/12 * * ?")
    public void genQLToken() {
        qlClient.authToken();
    }

    @Scheduled(cron = "0 1/5 * * * ?")
    public void refreshEnvConfig() {
        envConfigManager.refreshEnvConfigData();
    }
}
