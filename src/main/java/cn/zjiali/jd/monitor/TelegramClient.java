package cn.zjiali.jd.monitor;

import it.tdlight.client.*;
import it.tdlight.common.Init;
import it.tdlight.common.utils.CantLoadLibrary;
import it.tdlight.jni.TdApi;

/**
 * @author zJiaLi
 * @since 2023-03-07 16:34
 */
public class TelegramClient {

    /**
     * Admin user id, used by the stop command example
     */
    private static final TdApi.MessageSender ADMIN_ID = new TdApi.MessageSenderUser(667900586);

    private static SimpleTelegramClient client;

    public void init() throws CantLoadLibrary, InterruptedException {
        Init.start();
        APIToken apiToken = APIToken.example();
        TDLibSettings settings = TDLibSettings.create(apiToken);
        TdApi.AddProxy proxy = new TdApi.AddProxy("127.0.0.1", 7890, true, new TdApi.ProxyTypeHttp());
        client = new SimpleTelegramClient(settings);
        client.execute(proxy);
        // Configure the authentication info
        var authenticationData = AuthenticationData.consoleLogin();
        // Add an example update handler that prints when the bot is started
        client.addUpdateHandler(TdApi.UpdateAuthorizationState.class, TelegramClient::onUpdateAuthorizationState);
        // Add an example update handler that prints every received message
        client.addUpdateHandler(TdApi.UpdateNewMessage.class, TelegramClient::onUpdateNewMessage);
        // Add an example command handler that stops the bot
        client.addCommandHandler("stop", new StopCommandHandler());
        // Start the client
        client.start(authenticationData);
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
