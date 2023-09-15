package cn.zjiali.jd.monitor.ql;

import cn.zjiali.jd.monitor.mq.Queues;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component(value = "MQTaskQueueProcessor")
public class MQTaskQueueProcessor extends TaskQueueProcessor {
    private final RabbitTemplate rabbitTemplate;

    public MQTaskQueueProcessor(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public void addCron(CronContext element) {
        this.rabbitTemplate.convertAndSend(Queues.QL_RUN_TASK_MESSAGE.exchange(), Queues.QL_RUN_TASK_MESSAGE.routingKey(), element, m -> {
            m.getMessageProperties().setDelay(5000);
            return m;
        });
    }


    @RabbitListener(queues = {"ql.run.task.message"})
    public void handleQlRunTaskMessage(CronContext cronContext, Message message, Channel channel) {
        if (checkCronStatus(cronContext)) {
            // 元素状态符合条件，处理业务逻辑
            processCron(cronContext);
        } else {
            // 元素状态不符合，放回队列
            this.rabbitTemplate.convertAndSend(Queues.QL_WAIT_TASK_MESSAGE.exchange(), Queues.QL_WAIT_TASK_MESSAGE.routingKey(), cronContext, m -> {
                m.getMessageProperties().setDelay(30000);
                return m;
            });
        }
    }

    @RabbitListener(queues = {"ql.wait.task.message"})
    public void handleQlWaitTaskMessage(CronContext cronContext, Message message, Channel channel) {
        if (checkCronStatus(cronContext)) {
            // 元素状态符合条件，处理业务逻辑
            processCron(cronContext);
        } else {
            // 元素状态不符合，放回队列
            this.rabbitTemplate.convertAndSend(Queues.QL_WAIT_TASK_MESSAGE.exchange(), Queues.QL_WAIT_TASK_MESSAGE.routingKey(), cronContext, m -> {
                m.getMessageProperties().setDelay(30000);
                return m;
            });
        }
    }
}
