package cn.zjiali.jd.monitor;

import it.tdlight.common.utils.CantLoadLibrary;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class JdMonitorApplication {

    public static void main(String[] args) throws CantLoadLibrary, InterruptedException {
        SpringApplication.run(JdMonitorApplication.class, args);
        TelegramClient telegramClient = new TelegramClient();
        telegramClient.init();
    }

}
