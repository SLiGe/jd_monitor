package cn.zjiali.jd.monitor.prop;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author zJiaLi
 * @since 2023-03-13 09:35
 */
@ConfigurationProperties(prefix = "proxy")
public record ProxyProp(
        Boolean enable,
        String host,
        Integer port,
        //socks5 http mtproto
        String type,
        //for mtproto
        String secret,
        String username,
        String password,
        //for http
        Boolean httpOnly
) {


}
