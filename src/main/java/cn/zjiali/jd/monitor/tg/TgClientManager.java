package cn.zjiali.jd.monitor.tg;

import cn.zjiali.jd.monitor.handler.ChatMessageHandler;
import cn.zjiali.jd.monitor.handler.StopCommandHandler;
import cn.zjiali.jd.monitor.prop.ProxyProp;
import cn.zjiali.jd.monitor.prop.TgProp;
import it.tdlight.client.APIToken;
import it.tdlight.client.AuthenticationData;
import it.tdlight.client.SimpleTelegramClient;
import it.tdlight.client.TDLibSettings;
import it.tdlight.common.Init;
import it.tdlight.common.utils.CantLoadLibrary;
import it.tdlight.jni.TdApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * @author zJiaLi
 * @since 2023-03-13 09:31
 */
@Component
public class TgClientManager {

    private final TgProp tgProp;

    private final ProxyProp proxyProp;

    private final TgClientFactory tgClientFactory;

    private final ChatMessageHandler chatMessageHandler;

    private final StopCommandHandler stopCommandHandler;

    private final Logger logger = LoggerFactory.getLogger(TgClientManager.class);

    private SimpleTelegramClient client;

    public TgClientManager(TgProp tgProp, ProxyProp proxyProp, TgClientFactory tgClientFactory, ChatMessageHandler chatMessageHandler,
                           StopCommandHandler stopCommandHandler) {
        this.tgProp = tgProp;
        this.proxyProp = proxyProp;
        this.tgClientFactory = tgClientFactory;
        this.chatMessageHandler = chatMessageHandler;
        this.stopCommandHandler = stopCommandHandler;
    }


    public void start() {
        try {
            Init.start();
            APIToken apiToken = new APIToken(tgProp.apiId(), tgProp.apiHash());
            TDLibSettings settings = TDLibSettings.create(apiToken);
            client = new SimpleTelegramClient(settings);
            tgClientFactory.build(client);
            // Configure the authentication info
            var authenticationData = AuthenticationData.user(tgProp.userPhone());
            // Add an example update handler that prints when the bot is started
            client.addUpdateHandler(TdApi.UpdateAuthorizationState.class, this::onUpdateAuthorizationState);
            // Add an example update handler that prints every received message
            client.addUpdateHandler(TdApi.UpdateNewMessage.class, chatMessageHandler::onUpdateNewMessage);
            // Add an example command handler that stops the bot
            client.addCommandHandler("stop", stopCommandHandler);
            // Start the client
            client.start(authenticationData);
            proxySetting();
            // Wait for exit
            client.waitForExit();
        } catch (CantLoadLibrary | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void stop() {
        client.sendClose();
    }

    private void proxySetting() {
        if (proxyProp.enable()) {
            TdApi.ProxyType proxy = null;
            switch (proxyProp.type()) {
                case "socks5" -> proxy = new TdApi.ProxyTypeSocks5(proxyProp.username(), proxyProp.password());
                case "http" ->
                        proxy = new TdApi.ProxyTypeHttp(proxyProp.username(), proxyProp.password(), proxyProp.httpOnly());
                case "mtproto" -> proxy = new TdApi.ProxyTypeMtproto(proxyProp.secret());
            }
            TdApi.AddProxy addProxy = new TdApi.AddProxy(proxyProp.host(), proxyProp.port(), true, proxy);
            client.send(addProxy, result -> {
                logger.info("Telegram add proxy result: {}", !result.isError());
                result.error().ifPresent(e -> {
                    logger.info("Telegram add proxy error result: {}", e);
                });
            });
        }
    }


    /**
     * Print the bot status
     */
    private void onUpdateAuthorizationState(TdApi.UpdateAuthorizationState update) {
        var authorizationState = update.authorizationState;
        if (authorizationState instanceof TdApi.AuthorizationStateReady) {
            logger.info("bot Logged ");
        } else if (authorizationState instanceof TdApi.AuthorizationStateClosing) {
            logger.info("bot Closing... ");
        } else if (authorizationState instanceof TdApi.AuthorizationStateClosed) {
            logger.info("bot Closed");
        } else if (authorizationState instanceof TdApi.AuthorizationStateLoggingOut) {
            logger.info("bot Logging out...");
        }
    }


}
