package cn.zjiali.jd.monitor.prop;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * @author zJiaLi
 * @since 2023-03-13 14:44
 */
@ConfigurationProperties("monitor")
public record MonitorProp(String dataUrl, List<String> channel) {

}
