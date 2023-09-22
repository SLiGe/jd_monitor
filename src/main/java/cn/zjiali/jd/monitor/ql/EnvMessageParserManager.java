package cn.zjiali.jd.monitor.ql;

import cn.zjiali.jd.monitor.db.Config;
import cn.zjiali.jd.monitor.tg.TgManager;
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

/**
 * @author zJiaLi
 * @since 2023-03-13 16:56
 */
@Component
public class EnvMessageParserManager {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final Cache<String, String> runIgnoreCache;
    private final MQTaskQueueProcessor mqTaskQueueProcessor;
    private final TgManager tgManager;
    private final EnvConfigManager envConfigManager;

    public EnvMessageParserManager(MQTaskQueueProcessor mqTaskQueueProcessor, TgManager tgManager, EnvConfigManager envConfigManager) {
        this.mqTaskQueueProcessor = mqTaskQueueProcessor;
        this.tgManager = tgManager;
        this.envConfigManager = envConfigManager;
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
        List<Config> configInfos = envConfigManager.getConfigList();
        Map<CronScript, Map<String, String>> scriptEnvMap = new HashMap<>();
        for (Config configInfo : configInfos) {
            String keyword = configInfo.getKeyword();
            String valueRegex = configInfo.getValueRegex();
            String env = configInfo.getEnv();
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
                    Map<String, String> envMap = scriptEnvMap.computeIfAbsent(new CronScript(configInfo.getName(), configInfo.getScript()), k -> new HashMap<>());
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
                    Set<CronContext.Env> envs = new HashSet<>();
                    env.forEach((name, value) -> envs.add(new CronContext.Env(name, value, cronScript.cron)));
                    this.mqTaskQueueProcessor.addCron(new CronContext(cronScript.cron, cronScript.script, envs));
                    this.runIgnoreCache.put(cronScript.cron, envMd5);
                } else {
                    logger.info("忽略任务[{}]", cronScript.cron);
                }
            });
        } else {
            this.tgManager.sendMessage(-1001818798939L, text + "\n未检测到此变量");
        }
    }

    private static Matcher getValueMatcher(String valueRegex, String envValue) {
        Pattern valuePattern = Pattern.compile(valueRegex);
        return valuePattern.matcher(envValue);
    }
}
