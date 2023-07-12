package cn.zjiali.jd.monitor;

import cn.zjiali.jd.monitor.manager.NotifyManager;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@SpringBootApplication
@ConfigurationPropertiesScan
public class JdMonitorApplication {

    public static void main(String[] args) {
        SpringApplication.run(JdMonitorApplication.class, args);
    }

    @Component
    public static class ApplicationStartedListener implements ApplicationListener<ApplicationStartedEvent> {
        private final NotifyManager notifyManager;

        public ApplicationStartedListener(NotifyManager notifyManager) {
            this.notifyManager = notifyManager;
        }

        @Override
        public void onApplicationEvent(ApplicationStartedEvent event) {
            notifyManager.sendNotify("JdMonitor", "JdMonitor Start Success!");
        }

    }

}
