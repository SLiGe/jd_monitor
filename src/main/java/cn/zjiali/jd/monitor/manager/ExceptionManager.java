package cn.zjiali.jd.monitor.manager;

import cn.zjiali.jd.monitor.prop.NotifyProp;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * @author zJiaLi
 * @since 2023-03-14 11:48
 */
@Component
public class ExceptionManager {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final NotifyManager notifyManager;

    public ExceptionManager(NotifyProp notifyProp, NotifyManager notifyManager) {
        this.notifyManager = notifyManager;
    }

    public void handleException(String function, String content, Exception ex) {
        logger.error("执行[{}]失败,e:{}", function, ExceptionUtils.getStackTrace(ex));
        if (notifyManager.enable()) {
            notifyManager.sendNotify("错误日志", function + "执行失败" + "\n" + content);
        }
    }
}
