package cn.zjiali.jd.monitor.mq;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * @author zJiaLi
 * @since 2022-11-30 23:47
 */
@Configuration
public class QLTaskMQConfig {

    @Bean(value = "qlTaskExchange")
    public Exchange qlTaskExchange() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-delayed-type", "direct");
        return new CustomExchange("ql.task", "x-delayed-message", true, false, args);
    }

    @Bean(value = "qlRunTaskMessageQueue")
    public Queue qlRunTaskMessageQueue() {
        return QueueBuilder.durable(Queues.QL_RUN_TASK_MESSAGE.queue()).build();
    }

    @Bean(value = "qlWaitTaskMessageQueue")
    public Queue qlWaitTaskMessageQueue() {
        return QueueBuilder.durable(Queues.QL_WAIT_TASK_MESSAGE.queue()).build();
    }


    @Bean(value = "bindingQlRunTaskMessage")
    public Binding bindingQlRunTaskMessage(@Qualifier(value = "qlRunTaskMessageQueue") Queue queue, @Qualifier(value = "qlTaskExchange") Exchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(Queues.QL_RUN_TASK_MESSAGE.routingKey()).noargs();
    }

    @Bean(value = "bindingQlWaitTaskMessage")
    public Binding bindingQlWaitTaskMessage(@Qualifier(value = "qlWaitTaskMessageQueue") Queue queue, @Qualifier(value = "qlTaskExchange") Exchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(Queues.QL_WAIT_TASK_MESSAGE.routingKey()).noargs();
    }


}
