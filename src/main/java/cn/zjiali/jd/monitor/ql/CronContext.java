package cn.zjiali.jd.monitor.ql;

import java.io.*;
import java.util.Set;

/**
 * @author zJiaLi
 * @since 2023-03-15 09:25
 */
public record CronContext(String cronName, String script, Set<Env> envs) implements Serializable {
    public record Env(String name, String value, String remarks) implements Serializable{

    }
}
