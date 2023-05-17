package cn.zjiali.jd.monitor.tg;

import cn.zjiali.jd.monitor.handler.ChatMessageHandler;
import cn.zjiali.jd.monitor.handler.StopCommandHandler;
import cn.zjiali.jd.monitor.prop.ProxyProp;
import cn.zjiali.jd.monitor.prop.TgProp;
import it.tdlight.Init;
import it.tdlight.client.*;
import it.tdlight.jni.TdApi;
import it.tdlight.util.UnsupportedNativeLibraryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.nio.file.Paths;

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
            Init.init();
        } catch (UnsupportedNativeLibraryException e) {
            throw new RuntimeException(e);
        }
        try (SimpleTelegramClientFactory clientFactory = new SimpleTelegramClientFactory()) {
            APIToken apiToken = new APIToken(tgProp.apiId(), tgProp.apiHash());
            TDLibSettings settings = TDLibSettings.create(apiToken);
            // Configure the session directory
            var sessionPath = Paths.get("jd-monitor-session");
            settings.setDatabaseDirectoryPath(sessionPath.resolve("data"));
            settings.setDownloadedFilesDirectoryPath(sessionPath.resolve("downloads"));
            SimpleTelegramClientBuilder telegramClientBuilder = clientFactory.builder(settings);
            // Configure the authentication info
            SimpleAuthenticationSupplier<?> user = AuthenticationSupplier.user(tgProp.userPhone());
            // Add an example update handler that prints when the bot is started
            telegramClientBuilder.addUpdateHandler(TdApi.UpdateAuthorizationState.class, this::onUpdateAuthorizationState);
            // Add an example update handler that prints every received message
            telegramClientBuilder.addUpdateHandler(TdApi.UpdateNewMessage.class, chatMessageHandler::onUpdateNewMessage);
            // Add an example command handler that stops the bot
            telegramClientBuilder.addCommandHandler("stop", stopCommandHandler);
            // Start the client
            this.client = telegramClientBuilder.build(user);
            tgClientFactory.build(this.client);
            this.client.send(new TdApi.SetLogVerbosityLevel(5), (r) -> logger.info("set log verbosity level :{}", r.get()));
            this.client.send(new TdApi.SetLogStream(new TdApi.LogStreamFile("/home/gary/jd_monitor/td.log",
                    Long.parseLong("1073741824"), true)), (r) -> logger.info("set log stream level :{}", r.get()));
            proxySetting();
        }
    }

    public void stop() {
        client.sendClose();
    }

    private void proxySetting() {
        if (proxyProp.enable()) {
            logger.info("set tg proxy...");
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
            logger.info("Telegram Client Logged...");
        } else if (authorizationState instanceof TdApi.AuthorizationStateClosing) {
            logger.info("Telegram Client Closing... ");
        } else if (authorizationState instanceof TdApi.AuthorizationStateClosed) {
            logger.info("Telegram Client Closed...");
        } else if (authorizationState instanceof TdApi.AuthorizationStateLoggingOut) {
            logger.info("Telegram Client Logging out...");
        }
    }


}
