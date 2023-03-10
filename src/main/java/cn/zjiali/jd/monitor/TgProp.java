package cn.zjiali.jd.monitor;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author zJiaLi
 * @since 2023-03-10 17:21
 */

@Configuration
@ConfigurationProperties(prefix = "tg")
public class TgProp {

    private Integer apiId;

    private String apiHash;

    public String getApiHash() {
        return apiHash;
    }

    public void setApiHash(String apiHash) {
        this.apiHash = apiHash;
    }

    public Integer getApiId() {
        return apiId;
    }

    public void setApiId(Integer apiId) {
        this.apiId = apiId;
    }
}
