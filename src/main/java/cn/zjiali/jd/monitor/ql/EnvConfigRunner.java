package cn.zjiali.jd.monitor.ql;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class EnvConfigRunner implements ApplicationRunner {

    private final EnvConfigManager envConfigManager;

    public EnvConfigRunner(EnvConfigManager envConfigManager) {
        this.envConfigManager = envConfigManager;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        envConfigManager.refreshEnvConfigData();
    }
}
