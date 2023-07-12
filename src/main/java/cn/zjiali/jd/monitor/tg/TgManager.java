package cn.zjiali.jd.monitor.tg;

import it.tdlight.client.GenericResultHandler;
import it.tdlight.client.Result;
import it.tdlight.jni.TdApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * @author zJiaLi
 * @since 2023-05-26 14:15
 */
@Component
public class TgManager {

    private final TgClientFactory tgClientFactory;

    public TgManager(TgClientFactory tgClientFactory) {
        this.tgClientFactory = tgClientFactory;
    }

    private static final GenericResultHandler<TdApi.Message> defaultHandler = new DefaultHandler<>();

    public void sendMessage(Long chatId, String message) {
        TdApi.InputMessageContent content = new TdApi.InputMessageText(new TdApi.FormattedText(message, null), false, true);
        this.tgClientFactory.bot().send(new TdApi.SendMessage(chatId, 0, 0, null, null, content), defaultHandler);
    }

    private static class DefaultHandler<T extends TdApi.Object> implements GenericResultHandler<T> {
        private final Logger logger = LoggerFactory.getLogger(getClass());

        @Override
        public void onResult(Result result) {
            logger.debug("Send Message Result:{}", result.get());
            if (result.isError()) {
                logger.error("Send Message Error:{}", result.getError());
            }
        }
    }
}
