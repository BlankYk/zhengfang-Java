package cn.css0209.flea.reptile;

import cn.css0209.flea.user.api.Result;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.URLUtil;
import cn.hutool.http.Header;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import lombok.Getter;
import lombok.ToString;
import org.apache.catalina.Session;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author blankyk
 */
@Getter
@ToString
public class Zhengfang {
    Logger log = LoggerFactory.getLogger(this.getClass());
    private String host;
    private String VIEWSTATE;

    public Zhengfang() {
        HttpRequest request = HttpRequest.get("http://220.167.53.63:95").setFollowRedirects(false);
        HttpResponse response = request.execute();
        //获取token
        String token = response.body().substring(85, 109);
        host = "http://220.167.53.63:95/(" + token + ")";
        VIEWSTATE = VIEWSTATE();
        log.info("爬虫初始化完成");
    }

    /**
     * 爬取验证码
     */
    public void captCha() {
        String url = host + "/CheckCode.aspx";
        HttpUtil.downloadFile(url, FileUtil.file("cn/css0209/flea/user/img/captCha.jpg"));
    }

    private String VIEWSTATE() {
        String url = host + "/default2.aspx";
        String html = HttpRequest.get(url).execute().body();
        return html.substring(1706, 1754);
    }

    /**
     * 登录
     * @param username 用户名
     * @param password 密码
     * @param captcha 验证码
     */
    public Result login(String username, String password, String captcha) {
        String url = host + "/default2.aspx";
        Result result = new Result();
        Map<String, Object> param = new HashMap<>();
        param.put("__VIEWSTATE", VIEWSTATE);
        param.put("TextBox1", username);
        param.put("TextBox2", password);
        param.put("TextBox3", captcha);
        param.put("Button1","");
        HttpRequest request = HttpRequest.post(url)
                .form(param)
                .setFollowRedirects(true);
        HttpResponse response = request.execute();
        List<String> names = ReUtil.findAll("[\\u4E00-\\u9FA5]+同学",response.body(),0,new ArrayList<>());
        List<String> captchas = ReUtil.findAll("language=\'javascript\'",response.body(),0,new ArrayList<>());
        List<String> error = ReUtil.findAll("ERROR", response.body(), 0, new ArrayList<>());
        if(names.size()>0){
            String name = names.get(0).replaceAll("同学","");
            result.setResult("success");
            result.setMsg("登录成功！");
            JSONObject json = new JSONObject();
            json.put("name",name);
            result.setItem(json);
        }else if(captchas.size()>0){
            result.setResult("fail");
            result.setMsg("验证码错啦！用你的卡姿兰大眼睛康康！");
        }else if(error.size()>0){
            result.setResult("fail");
            result.setMsg("由于不知道什么原因，登录错误，可能是密码错哦！！");
        }
        return result;
    }

    /**
     * 查询成绩页面
     * @param xh 学号
     * @param name 姓名
     * @return response对象
     */
    private HttpResponse gradePage(String xh,String name){
        String url = host + "/xscjcx.aspx?xh=" + xh + "&xm=" + URLUtil.encode(name)+"&gnmkdm=N121605";
        HttpRequest request = HttpRequest.get(url)
                .header(Header.REFERER, host + "xs_main.aspx?xh=" + xh)
                .header(Header.USER_AGENT, "Mozilla/5.0 (Macintosh; Intel Mac OS X 10.13; rv:67.0) Gecko/20100101 Firefox/67.0");
        HttpResponse response = request.execute();
        return response;
    }

    /**
     * 获取个人信息
     * @param name 姓名
     * @param xh 学号
     * @return 学生基本信息
     */
    public JSONObject myInfo(String name,String xh){
        JSONObject result = new JSONObject();
        //获取页面内容
        HttpResponse response = gradePage(xh, name);
        //获取个人信息存入集合
        List<String> xys = ReUtil.findAll("学院：[\\u4e00-\\u9fa5]+", response.body(), 0, new ArrayList<>());
        List<String> zys = ReUtil.findAll("\"lbl_zymc\">[\\u4e00-\\u9fa5]+", response.body(), 0, new ArrayList<>());
        List<String> xzbs = ReUtil.findAll("行政班：[\\u4E00-\\u9FA5A-Za-z0-9_]+", response.body(), 0, new ArrayList<>());
        //获取信息
        String xy = xys.get(0).replaceAll("学院：","");
        String zy = zys.get(0).replaceAll("\"lbl_zymc\">", "");
        String xzb = xzbs.get(0).replaceAll("行政班：", "");
        result.put("name",name);
        result.put("student_id", xh);
        result.put("faculty",xy);
        result.put("profession", zy);
        result.put("asClass", xzb);
        return result;
    }


    /**
     * 挂科的成绩
     * @param xh 学号
     * @param name 姓名
     * @return 未通过成绩
     */
    public JSONObject failedGrade(String xh,String name){
        JSONObject result = new JSONObject();
        //获取查询成绩页面
        HttpResponse response = gradePage(xh, name);
        //解析获取数据
        Document html = Jsoup.parse(response.body());
        Element table = html.select("table.datelist").first();
        Elements trs = table.getElementsByTag("tr");
        Element titleTr = table.getElementsByTag("tr").first();
        Elements titlesTd = titleTr.getElementsByTag("td");
        String[] titles = new String[6];
        for(int i=0;i<titles.length;i++){
            titles[i] = titlesTd.get(i).text();
        }
        trs.remove(0);

        if(trs.size()==0){
            result.put(titles[0], "暂无");
        }

        JSONArray grades = getTableValue(trs, titles);
        result.put("failedGrade",grades);
        return result;
    }

    private JSONArray getTableValue(Elements trs, String[] titles) {
        JSONArray jsonArray = new JSONArray();
        for(int i=0;i<trs.size();i++) {
            JSONObject json = new JSONObject();
            Elements tds = trs.get(i).getElementsByTag("td");
            for (int x=0;x<tds.size();x++) {
                json.put(titles[x],tds.get(x).text());
            }
            jsonArray.put(json);
        }
        return jsonArray;
    }

    /**
     * 查询成绩
     * @param name 姓名
     * @param xh 学号
     * @return Result对象
     */
    public Result grade(String name, String xh,JSONObject jsonObject) {
        Result result = new Result();
        //获得查询结果
        HttpRequest request = postGrade(name, xh, jsonObject);
        HttpResponse response = request.execute();
        //解析结果
        Document html = Jsoup.parse(response.body());
        Element table = html.select("table.datelist").first();
        Elements trs = table.getElementsByTag("tr");
        Elements tableTitleTd = trs.get(0).getElementsByTag("td");
        String[] tableTitle = new String[tableTitleTd.size()];
        for(int i=0;i<tableTitle.length;i++){
            tableTitle[i] = tableTitleTd.get(i).text();
        }
        trs.remove(0);
        JSONArray jsonArray = getTableValue(trs, tableTitle);
        JSONObject json = new JSONObject();
        json.put("grade", jsonArray);

        result.setResult("success");
        result.setMsg("请求成功");
        result.setItem(json);
        return result;
    }

    /**
     * 获取成绩查询结果
     * @param name 名字
     * @param xh 学号
     * @param jsonObject 客户端传来数据
     * @return HttpRequest
     */
    private HttpRequest postGrade(String name, String xh,JSONObject jsonObject){
        HttpResponse response = gradePage(xh, name);
        Document html = Jsoup.parse(response.body());
        //表单数据
        String EVENTTARGET = html.selectFirst("input[name=__EVENTTARGET]").attr("value");
        String EVENTARGUMENT = html.selectFirst("input[name=__EVENTARGUMENT]").attr("value");
        String VIEWSTATE = html.selectFirst("input[name=__VIEWSTATE]").attr("value");
        String ddlXN = jsonObject.getStr("year");
        String ddlXQ = jsonObject.getStr("semester");
        String ddl_kcxz = jsonObject.getStr("course_nature");
        String btn = jsonObject.getStr("btn");
        String btnValue = getBtnValues(btn);
        Map<String,Object> parmes = new HashMap<>(7);
        parmes.put("__EVENTTARGET", EVENTTARGET);
        parmes.put("__EVENTARGUMENT", EVENTARGUMENT);
        parmes.put("__VIEWSTATE", VIEWSTATE);
        parmes.put("ddlXN", ddlXN);
        parmes.put("ddlXQ", ddlXQ);
        parmes.put("ddl_kcxz", ddl_kcxz);
        parmes.put(btn, btnValue);
        return HttpRequest.post(host + "/xscjcx.aspx?xh=" + xh + "&xm=" + URLUtil.encode(name)+"&gnmkdm=N121605")
                .header(Header.REFERER, host + "xs_main.aspx?xh=" + xh)
                .header(Header.USER_AGENT, "Mozilla/5.0 (Macintosh; Intel Mac OS X 10.13; rv:67.0) Gecko/20100101 Firefox/67.0")
                .form(parmes);
    }
    /**
     * 获取查询成绩按钮值
     * @param btn 按钮
     * @return 值
     */
    private String getBtnValues(String btn){
        switch (btn){
            case "btn_xq":
                return URLUtil.encode("学期成绩");
            case "btn_xn":
                return URLUtil.encode("学年成绩");
            case "btn_zcj":
                return URLUtil.encode("历年成绩");
            case "btn_zg":
                return URLUtil.encode("课程最高成绩");
            case "Button2":
                return URLUtil.encode("未通过成绩");
            case "Button1":
                return URLUtil.encode("成绩统计");
            default:
                throw new IllegalStateException("Unexpected value: " + btn);
        }
    }
}
