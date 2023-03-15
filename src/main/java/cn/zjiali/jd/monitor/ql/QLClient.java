package cn.zjiali.jd.monitor.ql;

import cn.zjiali.jd.monitor.manager.ExceptionManager;
import cn.zjiali.jd.monitor.prop.QLProp;
import cn.zjiali.jd.monitor.util.HttpUtil;
import cn.zjiali.jd.monitor.util.JsonUtil;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author zJiaLi
 * @since 2023-03-13 11:00
 */
@Component
public class QLClient {

    private final QLProp qlProp;

    private final ExceptionManager exceptionManager;

    public QLClient(QLProp qlProp, ExceptionManager exceptionManager) {
        this.qlProp = qlProp;
        this.exceptionManager = exceptionManager;
    }

    public AtomicReference<String> TOKEN = new AtomicReference<>();

    private final Logger logger = LoggerFactory.getLogger(QLClient.class);

    record BaseResponse<T>(Integer code, T data) {

    }

    @PostConstruct
    public void init() {
        authToken();
    }

    public void authToken() {
        try {
            String response = HttpUtil.get(joinBaseUrl(QLUrl.GET_AUTH_TOKEN),
                    Map.of("client_id", qlProp.clientId(), "client_secret", qlProp.clientSecret()), false
                    , headerMap(false));
            record GetTokenData(String token, @SerializedName("token_type") String tokenType) {
            }
            BaseResponse<GetTokenData> getToken = JsonUtil.toObjByType(response, new TypeToken<BaseResponse<GetTokenData>>() {
            }.getType());
            TOKEN.set(getToken.data.tokenType() + " " + getToken.data.token());
            logger.info("生成青龙Token:{}", getToken.data.token);
        } catch (Exception e) {
            exceptionManager.handleException("生成青龙Token", "", e);
        }

    }

    public void updateEnv(String envName, String envValue, String remarks) {
        try {
            record Env(Integer id, String name, String value, String remarks) {
                public Env(String name, String value, String remarks) {
                    this(null, name, value, remarks);
                }
            }
            String response = HttpUtil.get(joinBaseUrl(QLUrl.ENVS), Map.of(), true, headerMap(true));
            BaseResponse<List<Env>> listBaseResponse = JsonUtil.toObjByType(response, new TypeToken<BaseResponse<List<Env>>>() {
            }.getType());
            listBaseResponse.data().stream().filter(it -> envName.equals(it.name()))
                    .findFirst().ifPresentOrElse(it -> {
                        try {
                            HttpUtil.put(joinBaseUrl(QLUrl.ENVS), JsonUtil.obj2str(new Env(it.id(), it.name(), envValue, null)), headerMap(true));
                        } catch (Exception e) {
                            exceptionManager.handleException("更新青龙环境变量", envLogTpl(envName, envValue), e);
                        }
                    }, () -> {
                        try {
                            HttpUtil.post(joinBaseUrl(QLUrl.ENVS), JsonUtil.obj2str(List.of(new Env(envName, envValue, remarks))), headerMap(true));
                        } catch (Exception e) {
                            exceptionManager.handleException("添加青龙环境变量", envLogTpl(envName, envValue), e);
                        }
                    });
        } catch (Exception e) {
            exceptionManager.handleException("更新青龙环境变量", envLogTpl(envName, envValue), e);
        }

    }

    public List<Cron.CronInfo> getCron(String script) throws Exception {
        Map<String, String> searchParam = Map.of("searchText", script,
                "page", "1", "size", "5", "t", String.valueOf(System.currentTimeMillis()));
        String cronListJson = HttpUtil.get(joinBaseUrl(QLUrl.CRONS), searchParam, true, headerMap(true));
        BaseResponse<Cron> cronBaseResponse = JsonUtil.toObjByType(cronListJson, new TypeToken<BaseResponse<Cron>>() {
        }.getType());
        return cronBaseResponse.data.data();
    }

    public void runCron(String cron, String script) {
        try {
            getCron(script).stream().filter(it -> 0 == it.isDisabled()).findFirst().ifPresent(
                    it -> {
                        List<Integer> runIdParam = List.of(it.id());
                        try {
                            HttpUtil.put(joinBaseUrl(QLUrl.CRONS_RUN), JsonUtil.obj2str(runIdParam), headerMap(true));
                        } catch (Exception e) {
                            exceptionManager.handleException("执行任务失败", cronLogTpl(cron, script), e);
                        }
                    }
            );
        } catch (Exception e) {
            exceptionManager.handleException("执行任务失败", cronLogTpl(cron, script), e);
        }
    }

    private String envLogTpl(Object... args) {
        String log = """
                变量名: %s
                变量值: %s
                """;
        return log.formatted(args);
    }

    private String cronLogTpl(Object... args) {
        String log = """
                任务: %s
                脚本: %s
                """;
        return log.formatted(args);
    }

    private Map<String, String> headerMap(boolean auth) {
        Map<String, String> headerMap = new HashMap<>();
        if (auth) {
            headerMap.put("Authorization", TOKEN.get());
        }
        headerMap.put("Content-Type", "application/json;charset=UTF-8");
        return headerMap;
    }


    public String joinBaseUrl(String api) {
        return this.qlProp.url().concat(api);
    }


}
