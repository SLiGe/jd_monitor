package cn.zjiali.jd.monitor.handler;

import cn.zjiali.jd.monitor.tg.TgClientFactory;
import it.tdlight.client.SimpleTelegramClient;
import it.tdlight.jni.TdApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * @author zJiaLi
 * @since 2023-03-13 10:11
 */
@Component
public class BotChatMessageHandler {

    private final Logger logger = LoggerFactory.getLogger(BotChatMessageHandler.class);
    private final TgClientFactory tgClientFactory;

    public BotChatMessageHandler(TgClientFactory tgClientFactory) {
        this.tgClientFactory = tgClientFactory;
    }

    /**
     * Print new messages received via updateNewMessage
     */
    public void onUpdateNewMessage(TdApi.UpdateNewMessage update) {
        // Get the message content
        var messageContent = update.message.content;
        // Get the message text
        String text;
        if (messageContent instanceof TdApi.MessageText messageText) {
            // Get the text of the text message
            text = messageText.text.text;
        } else {
            // We handle only text messages, the other messages will be printed as their type
            text = String.format("(%s)", messageContent.getClass().getSimpleName());
        }
        SimpleTelegramClient client = tgClientFactory.bot();
        // Get the chat title
        client.send(new TdApi.GetChat(update.message.chatId), chatIdResult -> {
            // Get the chat response
            var chat = chatIdResult.get();
            // Get the chat name
            var chatName = chat.title;

            // Print the message
            logger.debug("New message from bot chat {} {}: {}", chat.id, chatName, text);
        });
    }
}
