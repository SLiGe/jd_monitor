package cn.zjiali.jd.monitor.ql;

import cn.zjiali.jd.monitor.prop.QLProp;
import cn.zjiali.jd.monitor.util.JsonUtil;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Arrays;
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

    private final HttpClient httpClient;

    public QLClient(QLProp qlProp) {
        this.qlProp = qlProp;
        this.httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(20)).build();
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
            String response = requestGet(QLUrl.GET_AUTH_TOKEN, Map.of("client_id", qlProp.clientId(), "client_secret", qlProp.clientSecret()), false);
            record GetTokenData(String token, @SerializedName("token_type") String tokenType) {
            }
            BaseResponse<GetTokenData> getToken = JsonUtil.toObjByType(response, new TypeToken<BaseResponse<GetTokenData>>() {
            }.getType());
            TOKEN.set(getToken.data.tokenType() + " " + getToken.data.token());
            logger.info("生成青龙Token:{}", getToken.data.token);
        } catch (Exception e) {
            logger.error("获取青龙token失败! {}", Arrays.toString(e.getStackTrace()));
        }

    }

    public void updateEnv(String envName, String envValue, String remark) {
        try {
            record Env(Integer id, String name, String value, String remark) {
                public Env(String name, String value, String remark) {
                    this(null, name, value, remark);
                }
            }
            String response = requestGet(QLUrl.ENVS, Map.of(), true);
            BaseResponse<List<Env>> listBaseResponse = JsonUtil.toObjByType(response, new TypeToken<BaseResponse<List<Env>>>() {
            }.getType());
            listBaseResponse.data().stream().filter(it -> envName.equals(it.name()))
                    .findFirst().ifPresentOrElse(it -> {
                        try {
                            requestPut(QLUrl.ENVS, JsonUtil.obj2str(new Env(it.id(), it.name(), envValue, null)));
                        } catch (Exception e) {
                            logger.error("更新青龙环境变量失败! {}", Arrays.toString(e.getStackTrace()));
                        }
                    }, () -> {
                        try {
                            requestPost(QLUrl.ENVS, JsonUtil.obj2str(new Env(envName, envValue, remark)));
                        } catch (Exception e) {
                            logger.error("添加青龙环境变量失败! {}", Arrays.toString(e.getStackTrace()));
                        }
                    });
        } catch (Exception e) {
            logger.error("更新青龙环境变量失败! {}", Arrays.toString(e.getStackTrace()));
        }

    }


    public void runCron(String script) {

        Map<String, String> searchParam = Map.of("searchText", script,
                "page", "1", "size", "5", "t", String.valueOf(System.currentTimeMillis()));
        try {
            record Cron(List<CronInfo> data) {
                record CronInfo(Integer id, String name, String command) {

                }
            }
            String cronListJson = requestGet(QLUrl.CRONS, searchParam, true);
            BaseResponse<Cron> cronBaseResponse = JsonUtil.toObjByType(cronListJson,new TypeToken<BaseResponse<Cron>>() {
            }.getType());
            cronBaseResponse.data.data().stream().findFirst().ifPresent(
                    it -> {
                        List<Integer> runIdParam = List.of(it.id());
                        try {
                            requestPut(QLUrl.CRONS_RUN, JsonUtil.obj2str(runIdParam));
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public String requestGet(String url, Map<String, String> param, boolean auth) throws Exception {
        String getUrl = buildParamUrl(url, param);
        HttpRequest.Builder httpRequestBuilder = HttpRequest.newBuilder().GET().uri(new URI(getUrl));
        if (auth) {
            httpRequestBuilder.header("Authorization", TOKEN.get());
        }
        HttpRequest httpRequest = httpRequestBuilder.build();
        HttpResponse<String> httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        return httpResponse.body();
    }


    public String requestPut(String url, String body) throws Exception {
        HttpRequest httpRequest = HttpRequest.newBuilder().PUT(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                .header("Authorization", TOKEN.get())
                .header("Content-Type", "application/json;charset=UTF-8")
                .uri(new URI(joinBaseUrl(url)))
                .build();
        HttpResponse<String> httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
        return httpResponse.body();
    }

    public String requestPost(String url, String body) throws Exception {
        HttpRequest httpRequest = HttpRequest.newBuilder().POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                .header("Authorization", TOKEN.get())
                .header("Content-Type", "application/json;charset=UTF-8")
                .uri(new URI(joinBaseUrl(url)))
                .build();
        HttpResponse<String> httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
        return httpResponse.body();
    }

    public <T> BaseResponse<T> baseConvert(String response, Class<T> clazz) {
        Type type = new TypeToken<BaseResponse<T>>() {
        }.getType();
        return JsonUtil.toObjByType(response, type);
    }

    public <T> BaseResponse<List<T>> baseListConvert(String response, Class<T> clazz) {
        Type type = new TypeToken<BaseResponse<List<T>>>() {
        }.getType();
        return JsonUtil.toObjByType(response, type);
    }


    public String joinBaseUrl(String api) {
        return this.qlProp.url().concat(api);
    }

    public String buildParamUrl(String api, Map<String, String> param) {
        if (param.isEmpty()) return joinBaseUrl(api);
        StringBuilder urlBuilder = new StringBuilder();
        urlBuilder.append(joinBaseUrl(api));
        urlBuilder.append("?");
        param.forEach((k, v) -> urlBuilder.append(k).append("=").append(URLEncoder.encode(v, StandardCharsets.UTF_8)).append("&"));
        return urlBuilder.toString();
    }


}
