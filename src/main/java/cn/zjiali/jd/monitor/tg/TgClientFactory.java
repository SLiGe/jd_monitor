package cn.zjiali.jd.monitor.tg;

import it.tdlight.client.SimpleTelegramClient;
import org.springframework.stereotype.Component;

/**
 * @author zJiaLi
 * @since 2023-03-13 10:16
 */
@Component
public class TgClientFactory {

    private SimpleTelegramClient client;

    public void build(SimpleTelegramClient telegramClient) {
        this.client = telegramClient;
    }

    public SimpleTelegramClient client() {
        return client;
    }

}
