package cn.zjiali.jd.monitor.prop;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author zJiaLi
 * @since 2023-03-14 11:56
 */
@ConfigurationProperties("notify")
public record NotifyProp(Boolean enable, String barkUrl, String sound, String group) {
}
