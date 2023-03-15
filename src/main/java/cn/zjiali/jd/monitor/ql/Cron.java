package cn.zjiali.jd.monitor.ql;

import java.util.List;

/**
 * @author zJiaLi
 * @since 2023-03-15 09:39
 */
public record Cron(List<CronInfo> data) {
    record CronInfo(Integer id, String name, String command, Integer isDisabled, Integer status) {

    }
}
