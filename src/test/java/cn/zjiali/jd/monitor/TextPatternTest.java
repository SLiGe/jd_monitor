package cn.zjiali.jd.monitor;

import cn.zjiali.jd.monitor.ql.QLClient;
import cn.zjiali.jd.monitor.util.JsonUtil;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author zJiaLi
 * @since 2023-03-13 16:31
 */
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
