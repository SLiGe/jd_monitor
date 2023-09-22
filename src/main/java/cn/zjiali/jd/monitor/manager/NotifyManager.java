package cn.zjiali.jd.monitor.manager;

import cn.zjiali.jd.monitor.prop.NotifyProp;
import cn.zjiali.jd.monitor.prop.TgProp;
import cn.zjiali.jd.monitor.tg.TgManager;
import cn.zjiali.jd.monitor.util.HttpUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * @author zJiaLi
 * @since 2023-03-14 11:52
 */
@Component
public class NotifyManager {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final NotifyProp notifyProp;
    private final TgManager tgManager;
    private final TgProp tgProp;

    public NotifyManager(NotifyProp notifyProp, TgManager tgManager, TgProp tgProp) {
        this.notifyProp = notifyProp;
        this.tgManager = tgManager;
        this.tgProp = tgProp;
    }

    public boolean enable() {
        return notifyProp.enable();
    }

    public void sendNotify(String title, String content) {
        Map<String, String> paramMap = new HashMap<>();
        if (StringUtils.isNotBlank(title)) {
            paramMap.put("title", title);
        }
        paramMap.put("body", content);
        if (StringUtils.isNotBlank(notifyProp.group())) {
            paramMap.put("group", notifyProp.group());
        }
        if (StringUtils.isNotBlank(notifyProp.sound())) {
            paramMap.put("sound", notifyProp.sound());
        }
        try {
            HttpUtil.form(notifyProp.barkUrl(), paramMap);
        } catch (Exception e) {
            logger.error("发送通知失败!e:{}", ExceptionUtils.getStackTrace(e));
        }
        if (Boolean.TRUE.equals(tgProp.enable())) {
            tgManager.sendMessage(1881119157L, content);
        }
    }
}
