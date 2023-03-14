package cn.zjiali.jd.monitor.ql;

import cn.zjiali.jd.monitor.manager.NotifyManager;
import cn.zjiali.jd.monitor.prop.MonitorProp;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author zJiaLi
 * @since 2023-03-13 16:56
 */
@Component
public class EnvMessageParserManager {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final MonitorProp monitorProp;
    private final QLClient qlClient;
    private final NotifyManager notifyManager;
    private final Cache<String, String> runIgnoreCache;

    public EnvMessageParserManager(MonitorProp monitorProp, QLClient qlClient, NotifyManager notifyManager) {
        this.monitorProp = monitorProp;
        this.qlClient = qlClient;
        this.notifyManager = notifyManager;
        this.runIgnoreCache = Caffeine.newBuilder().expireAfterWrite(Duration.ofSeconds(120)).build();
    }

    record CronScript(String cron, String script) {
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            CronScript that = (CronScript) o;
            return Objects.equals(cron, that.cron) && Objects.equals(script, that.script);
        }

        @Override
        public int hashCode() {
            return Objects.hash(cron, script);
        }
    }

    public void envParser(String text) {
        List<MonitorProp.ConfigInfo> configInfos = monitorProp.config();
        Map<CronScript, Map<String, String>> scriptEnvMap = new HashMap<>();
        List<String> logList = new ArrayList<>();
        for (MonitorProp.ConfigInfo configInfo : configInfos) {
            String keyword = configInfo.keyword();
            String valueRegex = configInfo.valueRegex();
            String env = configInfo.env();
            String[] envArray = env.split(",");
            String[] keywordArray = keyword.split(",");
            int index = 0;
            for (String key : keywordArray) {
                String envValue = null;
                if (StringUtils.isNotBlank(valueRegex)) {
                    Matcher valueMatcher = getValueMatcher(valueRegex, text);
                    if (valueMatcher.find()) {
                        envValue = valueMatcher.group(1);
                    }
                } else {
                    Matcher matcher = getValueMatcher("export\\s*%s\\s*=\"([^\"]*)\"".formatted(key), text);
                    if (matcher.find()) {
                        envValue = matcher.group(1);
                    }
                }
                if (StringUtils.isNotBlank(envValue)) {
                    Map<String, String> envMap = scriptEnvMap.computeIfAbsent(new CronScript(configInfo.name(), configInfo.script()), k -> new HashMap<>());
                    envMap.put(envArray[index], envValue);
                }
                index++;
            }
        }
        if (!scriptEnvMap.isEmpty()) {
            scriptEnvMap.forEach((cronScript, env) -> {
                StringBuilder allEnvBuilder = new StringBuilder();
                env.keySet().stream().sorted().forEach(key -> allEnvBuilder.append(key).append(env.get(key)));
                String envMd5 = DigestUtils.md5DigestAsHex(allEnvBuilder.toString().getBytes(StandardCharsets.UTF_8));
                String runningCronEnv = this.runIgnoreCache.getIfPresent(cronScript.cron);
                if (StringUtils.isBlank(runningCronEnv) || !runningCronEnv.equals(envMd5)) {
                    env.forEach((name, value) -> {
                        qlClient.updateEnv(name, value, cronScript.cron);
                        logList.add("替换变量[%s] => [%s]".formatted(name, value));
                    });
                    qlClient.runCron(cronScript.cron, cronScript.script);
                    this.runIgnoreCache.put(cronScript.cron, envMd5);
                    logList.add("执行任务[%s]".formatted(cronScript.cron));
                } else {
                    logList.add("忽略任务[%s]".formatted(cronScript.cron));
                }

            });
        }
        logList.forEach(logger::info);
        if (notifyManager.enable() && !logList.isEmpty()) {
            notifyManager.sendNotify("触发任务",
                    logList.stream().map(String::trim)
                            .collect(Collectors.joining("\n")));
        }
    }

    private static Matcher getValueMatcher(String valueRegex, String envValue) {
        Pattern valuePattern = Pattern.compile(valueRegex);
        return valuePattern.matcher(envValue);
    }
}
