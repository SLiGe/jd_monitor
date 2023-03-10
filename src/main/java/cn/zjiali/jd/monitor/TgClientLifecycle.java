package cn.zjiali.jd.monitor;

import it.tdlight.client.*;
import it.tdlight.common.Init;
import it.tdlight.common.utils.CantLoadLibrary;
import it.tdlight.jni.TdApi;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author zJiaLi
 * @since 2023-03-07 16:34
 */
@Component
public class TgClientLifecycle implements SmartLifecycle {

    /**
     * Admin user id, used by the stop command example
     */
    private static final TdApi.MessageSender ADMIN_ID = new TdApi.MessageSenderUser(667900586);

    private final TgProp tgProp;


    private static SimpleTelegramClient client;

    private final AtomicBoolean START_FLAG = new AtomicBoolean(false);

    public TgClientLifecycle(TgProp tgProp) {
        this.tgProp = tgProp;
    }

    public void init() throws CantLoadLibrary, InterruptedException {
        Init.start();
        APIToken apiToken = new APIToken(tgProp.getApiId(), tgProp.getApiHash());
        TDLibSettings settings = TDLibSettings.create(apiToken);
        TdApi.AddProxy proxy = new TdApi.AddProxy("127.0.0.1", 7890, true, new TdApi.ProxyTypeSocks5());
        client = new SimpleTelegramClient(settings);

        // Configure the authentication info
//        var authenticationData = AuthenticationData.user("+8617611058853");
        var authenticationData = AuthenticationData.consoleLogin();
        // Add an example update handler that prints when the bot is started
        client.addUpdateHandler(TdApi.UpdateAuthorizationState.class, TgClientLifecycle::onUpdateAuthorizationState);
        // Add an example update handler that prints every received message
        client.addUpdateHandler(TdApi.UpdateNewMessage.class, TgClientLifecycle::onUpdateNewMessage);
        // Add an example command handler that stops the bot
        client.addCommandHandler("stop", new StopCommandHandler());
        // Start the client

        client.start(authenticationData);
        client.send(proxy, result -> {
            System.out.println(result.isError());
            System.out.println(result.get());
        });
        // Wait for exit
        client.waitForExit();
    }


    /**
     * Print new messages received via updateNewMessage
     */
    private static void onUpdateNewMessage(TdApi.UpdateNewMessage update) {
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

        // Get the chat title
        client.send(new TdApi.GetChat(update.message.chatId), chatIdResult -> {
            // Get the chat response
            var chat = chatIdResult.get();
            // Get the chat name
            var chatName = chat.title;

            // Print the message
            System.out.printf("Received new message from chat %s: %s%n", chatName, text);
        });
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {
        client.sendClose();
    }

    @Override
    public boolean isRunning() {
        return START_FLAG.get();
    }

    /**
     * Close the bot if the /stop command is sent by the administrator
     */
    private static class StopCommandHandler implements CommandHandler {

        @Override
        public void onCommand(TdApi.Chat chat, TdApi.MessageSender commandSender, String arguments) {
            // Check if the sender is the admin
            if (isAdmin(commandSender)) {
                // Stop the client
                System.out.println("Received stop command. closing...");
                client.sendClose();
            }
        }
    }


    /**
     * Print the bot status
     */
    private static void onUpdateAuthorizationState(TdApi.UpdateAuthorizationState update) {
        var authorizationState = update.authorizationState;
        if (authorizationState instanceof TdApi.AuthorizationStateReady) {
            System.out.println("Logged in 啊啊啊啊啊啊啊啊啊啊啊");
        } else if (authorizationState instanceof TdApi.AuthorizationStateClosing) {
            System.out.println("Closing... ");
        } else if (authorizationState instanceof TdApi.AuthorizationStateClosed) {
            System.out.println("Closed");
        } else if (authorizationState instanceof TdApi.AuthorizationStateLoggingOut) {
            System.out.println("Logging out...");
        }
    }

    /**
     * Check if the command sender is admin
     */
    private static boolean isAdmin(TdApi.MessageSender sender) {
        return sender.equals(ADMIN_ID);
    }
}
