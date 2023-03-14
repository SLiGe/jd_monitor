package cn.zjiali.jd.monitor;

import cn.zjiali.jd.monitor.util.HttpUtil;
import cn.zjiali.jd.monitor.util.JsonUtil;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * @author zJiaLi
 * @since 2023-03-14 15:07
 */
public class HttpTest {

    @Test
    public void testBark() throws Exception {
        String url = "https://api.day.app/rxwFeqxhv9H6UpCDrzzBnN";
        Map<String, String> headerMap = new HashMap<>();
        headerMap.put("Content-Type", "application/json;charset=UTF-8");
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("title","触发任务");
        paramMap.put("key","rxwFeqxhv9H6UpCDrzzBnN");
        paramMap.put("body","替换变量[jd_wxCartKoi_activityId] => [7db3eb96b51d441290503115d732a110]\n执行任务[购物车锦鲤通用活动]");
        paramMap.put("group","京东监控");
        String post = HttpUtil.post(url, JsonUtil.obj2str(paramMap), headerMap);
        System.out.println(post);
    }
}
