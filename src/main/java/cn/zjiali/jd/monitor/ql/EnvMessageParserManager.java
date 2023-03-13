package cn.zjiali.jd.monitor.ql;

import cn.zjiali.jd.monitor.prop.MonitorProp;
import cn.zjiali.jd.monitor.ql.QLClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author zJiaLi
 * @since 2023-03-13 16:56
 */
@Component
public class EnvMessageParserManager {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final MonitorProp monitorProp;
    private final QLClient qlClient;

    public EnvMessageParserManager(MonitorProp monitorProp, QLClient qlClient) {
        this.monitorProp = monitorProp;
        this.qlClient = qlClient;
    }

    public void envParser(String text) {
        List<MonitorProp.ConfigInfo> configInfos = monitorProp.config();
        Map<String, Map<String, String>> scriptEnvMap = new HashMap<>();
        for (MonitorProp.ConfigInfo configInfo : configInfos) {
            String keyword = configInfo.keyword();
            String[] keywordArray = keyword.split(",");
            for (String key : keywordArray) {
                String[] textLineArray = text.split("\\n");
                for (String textLine : textLineArray) {
                    if (textLine.contains(key) && textLine.contains("export") && textLine.contains("=")) {
                        String envValue = textLine.replace("export", "").replace(key, "")
                                .replace("\"", "").replace("=", "").trim();
                        if (!envValue.isBlank()) {
                            Map<String, String> envMap = scriptEnvMap.computeIfAbsent(configInfo.script(), k -> new HashMap<>());
                            envMap.put(key, envValue);
                        }
                    }
                }
            }
        }
        if (!scriptEnvMap.isEmpty()) {
            scriptEnvMap.forEach((script, env) -> {
                env.forEach((name, value) -> {
                    qlClient.updateEnv(name, value, script);
                    logger.info("替换变量[{}] => [{}]", name, value);
                });
                qlClient.runCron(script);
                logger.info("执行任务[{}] ", script);
            });
        }
    }
}
