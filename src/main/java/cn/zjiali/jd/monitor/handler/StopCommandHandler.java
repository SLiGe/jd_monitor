package cn.zjiali.jd.monitor.handler;

import cn.zjiali.jd.monitor.tg.TgClientFactory;
import cn.zjiali.jd.monitor.prop.TgProp;
import it.tdlight.client.CommandHandler;
import it.tdlight.client.SimpleTelegramClient;
import it.tdlight.jni.TdApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * @author zJiaLi
 * @since 2023-03-13 10:21
 */
@Component
public class StopCommandHandler implements CommandHandler {

    private final Logger logger = LoggerFactory.getLogger(StopCommandHandler.class);
    private final TgClientFactory tgClientFactory;
    private final TgProp tgProp;

    public StopCommandHandler(TgClientFactory tgClientFactory, TgProp tgProp) {
        this.tgClientFactory = tgClientFactory;
        this.tgProp = tgProp;
    }

    @Override
    public void onCommand(TdApi.Chat chat, TdApi.MessageSender commandSender, String arguments) {
        SimpleTelegramClient client = tgClientFactory.client();
        // Check if the sender is the admin
        if (isAdmin(commandSender)) {
            // Stop the client
            logger.info("Received stop command. closing...");
            client.sendClose();
        }
    }

    /**
     * Check if the command sender is admin
     */
    private boolean isAdmin(TdApi.MessageSender sender) {
        return sender.equals(
                new TdApi.MessageSenderUser(tgProp.adminId())
        );
    }

}
