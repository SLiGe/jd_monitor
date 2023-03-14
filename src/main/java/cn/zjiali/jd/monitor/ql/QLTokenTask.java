package cn.zjiali.jd.monitor.ql;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * @author zJiaLi
 * @since 2023-03-14 14:38
 */
@Configuration
@EnableScheduling
public class QLTokenTask {

    private final QLClient qlClient;

    public QLTokenTask(QLClient qlClient) {
        this.qlClient = qlClient;
    }

    @Scheduled(cron = "0 0 0/12 * * ?")
    public void genQLToken(){
        qlClient.authToken();
    }

}
