package cn.zjiali.jd.monitor.prop;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author zJiaLi
 * @since 2023-03-13 10:57
 */
@ConfigurationProperties("ql")
public record QLProp(String clientId, String clientSecret, String url) {


}
