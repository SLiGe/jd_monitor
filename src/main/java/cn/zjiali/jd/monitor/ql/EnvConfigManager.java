package cn.zjiali.jd.monitor.ql;

import cn.zjiali.jd.monitor.db.Config;
import cn.zjiali.jd.monitor.db.ConfigRepository;
import cn.zjiali.jd.monitor.prop.MonitorProp;
import cn.zjiali.jd.monitor.util.HttpUtil;
import cn.zjiali.jd.monitor.util.JsonUtil;
import com.google.gson.reflect.TypeToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class EnvConfigManager {

    private final MonitorProp monitorProp;
    private final ConfigRepository configRepository;
    private final Logger logger = LoggerFactory.getLogger(EnvConfigManager.class);

    public EnvConfigManager(MonitorProp monitorProp, ConfigRepository configRepository) {
        this.monitorProp = monitorProp;
        this.configRepository = configRepository;
    }

    public void refreshEnvConfigData() {
        var dataUrl = monitorProp.dataUrl();
        try {
            String json = HttpUtil.get(dataUrl, Map.of(), false, Map.of());
            Type type = new TypeToken<List<Config>>() {
            }.getType();
            List<Config> configList = JsonUtil.toObjByType(json, type);
            configList.forEach(config -> {
                Optional<Config> configByEnv = configRepository.findConfigByEnv(config.getEnv());
                if (configByEnv.isEmpty()){
                    configRepository.save(config);
                    logger.info("save config: {}", config);
                }
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
