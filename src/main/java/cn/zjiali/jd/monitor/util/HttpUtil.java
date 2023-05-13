package cn.zjiali.jd.monitor.util;

import com.github.mizosoft.methanol.MultipartBodyPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;

/**
 * @author zJiaLi
 * @since 2023-03-14 13:41
 */
public class HttpUtil {

    private static final Logger logger = LoggerFactory.getLogger(HttpUtil.class);
    private final static HttpClient httpClient;

    static {
        httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(20)).build();
    }

    public static String get(String url, Map<String, String> param, boolean encode, Map<String, String> header) throws Exception {
        String getUrl = buildParamUrl(url, param, encode);
        logger.debug("request get => {}", getUrl);
        HttpRequest.Builder httpRequestBuilder = HttpRequest.newBuilder().GET().uri(new URI(getUrl));
        if (!header.isEmpty()) {
            header.forEach(httpRequestBuilder::header);
        }
        HttpResponse<String> httpResponse = httpClient.send(httpRequestBuilder.build(), HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        String resBody = httpResponse.body();
        logger.debug("request get res=> {}", resBody);
        return resBody;
    }


    public static String put(String url, String body, Map<String, String> header) throws Exception {
        logger.debug("request put url=> {}", url);
        HttpRequest.Builder httpRequestBuilder = HttpRequest.newBuilder()
                .PUT(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8)).uri(new URI(url));
        if (!header.isEmpty()) {
            header.forEach(httpRequestBuilder::header);
        }
        HttpResponse<String> httpResponse = httpClient.send(httpRequestBuilder.build(), HttpResponse.BodyHandlers.ofString());
        var resBody = httpResponse.body();
        logger.debug("request put res=> {}", resBody);
        return resBody;
    }

    public static String post(String url, String body, Map<String, String> header) throws Exception {
        logger.debug("request post url=> {}", url);
        HttpRequest.Builder httpRequestBuilder = HttpRequest.newBuilder().POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                .uri(new URI(url));
        if (!header.isEmpty()) {
            header.forEach(httpRequestBuilder::header);
        }
        HttpResponse<String> httpResponse = httpClient.send(httpRequestBuilder.build(), HttpResponse.BodyHandlers.ofString());
        String resBody = httpResponse.body();
        logger.debug("request post res=> {}", resBody);
        return resBody;
    }

    public static String form(String url, Map<String, String> paramMap) throws Exception {
        var multipartBody = MultipartBodyPublisher.newBuilder();
        paramMap.forEach(multipartBody::textPart);
        MultipartBodyPublisher bodyPublisher = multipartBody.build();
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .POST(bodyPublisher)
                .header("Content-Type", "multipart/form-data; boundary=" + bodyPublisher.boundary())
                .build();
        HttpResponse<String> httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
        return httpResponse.body();
    }


    public static String buildParamUrl(String url, Map<String, String> param, boolean encode) {
        if (param.isEmpty()) return url;
        StringBuilder urlBuilder = new StringBuilder();
        urlBuilder.append(url);
        urlBuilder.append("?");
        param.forEach((k, v) -> urlBuilder.append(k).append("=").append(encode ? URLEncoder.encode(v, StandardCharsets.UTF_8) : v).append("&"));
        return urlBuilder.toString();
    }

}