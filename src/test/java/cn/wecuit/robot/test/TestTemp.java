package cn.wecuit.robot.test;

import org.junit.Test;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Author jiyec
 * @Date 2021/5/21 14:23
 * @Version 1.0
 **/
public class TestTemp {
    Pattern compile = Pattern.compile("来(.*?)[点丶份张幅](.*?)的?(|r18)[色瑟涩\uD83D\uDC0D][图圖\uD83E\uDD2E]");

    @Test
    public void testT(){
        System.out.println("来(.*?)[点丶份张幅](.*?)的?(|r18)[色瑟涩\uD83D\uDC0D][图圖\uD83E\uDD2E]");
        subTest("来-1张色的r18色图");

        // System.out.println(Integer.parseUnsignedInt("1"));
    }

    public void subTest(String string){
        Matcher matcher = compile.matcher(string);
        if (matcher.find()) {
            int i = matcher.groupCount();
            System.out.println("group count:" + i);
            System.out.println("1--: " + matcher.group(1).replaceAll("[^0-9-]", ""));
            System.out.println("2--: " + matcher.group(2));
            System.out.println("3--: " + matcher.group(3));
        }

    }

    @Test
    public void testB(){
        String s = "<?xml version='1.0' encoding='UTF-8' standalone='yes' ?><msg serviceID=\"15\" templateID=\"1\" action=\"web\" actionData=\"group:940309953\" a_actionData=\"group:940309953\" i_actionData=\"group:940309953\" brief=\"推荐群聊：We成信大用户群\" sourceMsgId=\"0\" url=\"https://jq.qq.com/?_wv=1027&amp;k=3Iihqzly\" flag=\"0\" adverSign=\"0\" multiMsgFlag=\"0\"><item layout=\"0\" mode=\"1\" advertiser_id=\"0\" aid=\"0\"><summary>推荐群聊</summary><hr hidden=\"false\" style=\"0\" /></item><item layout=\"2\" mode=\"1\" advertiser_id=\"0\" aid=\"0\"><picture cover=\"https://p.qlogo.cn/gh/940309953/940309953/100\" w=\"0\" h=\"0\" needRoundView=\"0\" /><title>We成信大用户群</title><summary>成都信息工程大学 《We成信大》 小程序用户群。</summary></item><source name=\"\" icon=\"\" action=\"\" appid=\"-1\" /></msg>";
        Matcher matcher = Pattern.compile("\\<xml(.*?)推荐群聊").matcher(s);
        System.out.println(matcher.find());
        System.out.println(matcher.matches());
    }
}
