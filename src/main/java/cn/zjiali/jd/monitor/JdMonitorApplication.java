package cn.zjiali.jd.monitor;

import org.springframework.boot.SpringApplication;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class JdMonitorApplication {

    public static void main(String[] args) {
        SpringApplication.run(JdMonitorApplication.class, args);
    }

}
