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
    private SimpleTelegramClient botClient;

    public void build(SimpleTelegramClient telegramClient) {
        this.client = telegramClient;
    }

    public void buildBot(SimpleTelegramClient telegramClient) {
        this.botClient = telegramClient;
    }

    public SimpleTelegramClient client() {
        return client;
    }

    public SimpleTelegramClient bot() {
        return botClient;
    }

}
