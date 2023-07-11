package cn.zjiali.jd.monitor;

import cn.zjiali.jd.monitor.ql.EnvMessageParserManager;
import cn.zjiali.jd.monitor.ql.QLClient;
import cn.zjiali.jd.monitor.util.JsonUtil;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author zJiaLi
 * @since 2023-03-13 16:31
 */
@SpringBootTest
public class TextPatternTest {

    @Test
    public void regex(){
        Pattern pattern = Pattern.compile("export\\s*M_WX_LUCK_DRAW_URL\\s*=\"([^\"]*)\"");
        Matcher matcher = pattern.matcher("export M_WX_LUCK_DRAW_URL=\"https://lzkj-isv.isvjcloud.com/lzclient/1677428953464/cjwx/common/entry.html?activityId=a467addb75224c02bcce974b31edfa8d&gameType=wxTurnTable&shopid=1000001782\"");
//        System.out.println(matcher.find());
        while (matcher.find()){
            System.out.println(matcher.group(1));
        }

        String text = "export jd_lzkjInteractUrl=\"https://lzkj-isv.isvjcloud.com/prod/cc/interactsaas/index?activityType=10070&activityId=1630014926584877058&templateId=7fab7995-298c-44a1-af5a-f79c520fa8a888&nodeId=101001&prd=cjwx&adsource=tg_storePage\"\n" +
                "\n" +
                "100";
        String[] split = text.split("\\n");
        Arrays.stream(split).forEach(it->{
            if (it.contains("jd_lzkjInteractUrl")){
                String temp = it.replace("export","").replace("jd_lzkjInteractUrl","")
                        .replace("\"","").replace("=","");
                System.out.println(temp.trim());
            }
        });
    }

    record BaseResponse<T>(Integer code, T data) {

    }

    @Autowired
    private EnvMessageParserManager envMessageParserManager;

    @Test
    public void testMessage(){
        String message = """
                🎯 店铺抽奖 · 超级会员
                export LUCK_DRAW_URL="https://cjhy-isv.isvjcloud.com/wxDrawActivity/activity/1446376?activityId=b41b3477f267435886a777541004701b"
                """;
        envMessageParserManager.envParser(message);
    }

    @Test
    public void testFormat(){
        String key = "M_WX_LUCK_DRAW_URL";
        String pattern = "export\\s*%s\\s*=\"([^\"]*)\"".formatted(key);
        System.out.println(pattern);
        Pattern pattern1 = Pattern.compile(pattern);
        Matcher matcher = pattern1.matcher("export M_WX_LUCK_DRAW_URL=\"https://lzkj-isv.isvjcloud.com/lzclient/1677428953464/cjwx/common/entry.html?activityId=a467addb75224c02bcce974b31edfa8d&gameType=wxTurnTable&shopid=1000001782\"");
        if (matcher.find()){
            System.out.println(matcher.group(1));
        }
        Pattern valuePattern = Pattern.compile("[^?&]*activityId=([^&]*)");
        Matcher matcher1 = valuePattern.matcher("export M_WX_CENTER_DRAW_URL=\"https://lzkj-isv.isvjcloud.com/drawCenter/activity?activityId=2feb4bf007544d36bc103d3b0605d63e&shopid=790139\"");
        if (matcher1.find()){
            System.out.println(matcher1.group(1));
        }
    }

    @Test
    public void testEquals(){
        CronScript cronScript = new CronScript("1", "2");
        CronScript cronScript2 = new CronScript("1", "2");
        System.out.println(cronScript.equals(cronScript2));
        Map<CronScript,String> cronScriptStringMap = new HashMap<>();
        cronScriptStringMap.put(cronScript,"2");
        System.out.println(cronScriptStringMap.get(cronScript2));
    }

    record CronScript(String cron, String script) {
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            CronScript that = (CronScript) o;
            return Objects.equals(cron, that.cron) && Objects.equals(script, that.script);
        }

        @Override
        public int hashCode() {
            return Objects.hash(cron, script);
        }
    }

    @Test
    public void testGson(){
        record GetTokenData(String token, @SerializedName("token_type") String tokenType) {
        }
        String json = """
                {
                    "code": 200,
                    "data": {
                        "token": "40cc575a-a5cc-4d7f-878f-61855dd3c72e",
                        "token_type": "Bearer",
                        "expiration": 1681291626
                    }
                }
                """;
        Type type = new TypeToken<BaseResponse<GetTokenData>>() {
        }.getType();
        BaseResponse<GetTokenData> o = JsonUtil.toObjByType(json, type);
        System.out.println(o);
    }
}
