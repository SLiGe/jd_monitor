package cn.zjiali.jd.monitor.mq;


/**
 * @author zJiaLi
 * @since 2022-12-01 00:12
 */
public enum Queues {

    QL_RUN_TASK_MESSAGE("ql.run.task.message", "ql.task", "ql.run.task"),
    QL_WAIT_TASK_MESSAGE("ql.wait.task.message", "ql.task", "ql.wait.task"),

    ;

    private final String queue;

    private final String exchange;

    private final String routingKey;

    public String queue() {
        return this.queue;
    }

    public String exchange() {
        return this.exchange;
    }

    public String routingKey() {
        return this.routingKey;
    }

    Queues(String queue, String exchange, String routingKey) {
        this.queue = queue;
        this.exchange = exchange;
        this.routingKey = routingKey;
    }
}
