package cn.zjiali.jd.monitor.prop;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author zJiaLi
 * @since 2023-03-10 17:21
 */

@ConfigurationProperties(prefix = "tg")
public record TgProp(Boolean enable, Integer apiId,
                     String apiHash,
                     String userPhone,
                     Long adminId, String botToken) {

}
