package cn.zjiali.jd.monitor;

import cn.zjiali.jd.monitor.db.ConfigRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class DbTest {

    @Autowired
    private ConfigRepository configRepository;

    @Test
    public void testDb() {
        configRepository.findAll().forEach(System.out::println);
    }
}
