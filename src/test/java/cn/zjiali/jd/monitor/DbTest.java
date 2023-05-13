package cn.zjiali.jd.monitor;

import cn.zjiali.jd.monitor.db.ConfigRepository;
import cn.zjiali.jd.monitor.prop.MonitorProp;
import cn.zjiali.jd.monitor.util.JsonUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class DbTest {

    @Autowired
    private ConfigRepository configRepository;
    @Autowired
    private MonitorProp monitorProp;

    @Test
    public void testDb() {
        System.out.println(JsonUtil.obj2str(monitorProp.config()));
        configRepository.findAll().forEach(System.out::println);
    }
}
