package cn.css0209.flea.reptile;

import cn.css0209.flea.user.model.QueryParams;
import cn.css0209.flea.user.model.Result;
import cn.css0209.flea.user.model.UserInfo;
import cn.css0209.flea.user.types.BtnType;
import cn.css0209.flea.user.types.ResultStatus;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.URLUtil;
import cn.hutool.http.Header;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.dictionary.py.Pinyin;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;
import sun.tools.tree.NullExpression;

import java.io.FileInputStream;
import java.util.*;

/**
 * @author blankyk
 */
@Slf4j
@Service
public class Zhengfang {
    public Zhengfang() {
        log.info("爬虫启动");
    }

    /**
     * 获取token
     */
    public String getToken() {
        HttpRequest request = HttpRequest.get("http://220.167.53.63:95").setFollowRedirects(false);
        HttpResponse response = request.execute();
        Document html = Jsoup.parse(response.body());
        //获取token
        String token = html.getElementsByTag("a").attr("href").substring(2, 26);
        return token;
    }

    /**
     * 爬取验证码
     */
    public byte[] captCha(String path, String token) throws Exception {
        String host = "http://220.167.53.63:95/(" + token + ")";
        String url = host + "/CheckCode.aspx";
        HttpUtil.downloadFile(url, FileUtil.file("cn/css0209/flea/user/img/captCha" + path + ".jpg"));
        FileInputStream inputStream = new FileInputStream(FileUtil.file("cn/css0209/flea/user/img/captCha" + path + ".jpg"));
        byte[] bytes = new byte[inputStream.available()];
        inputStream.read(bytes, 0, inputStream.available());
        log.info("==>返回图片:captCha{}.jpg", path);
        return bytes;
    }

    private String VIEWSTATE(String token) {
        String host = "http://220.167.53.63:95/(" + token + ")";
        String url = host + "/default2.aspx";
        String result = "";
        Document html = Jsoup.parse(HttpRequest.get(url).execute().body());
        Elements inputTags = html.getElementsByTag("input");
        for (Element item : inputTags) {
            if ("__VIEWSTATE".equals(item.attr("name"))) {
                log.info("name:{},value:{}", item.attr("name"), item.attr("value"));
                result = item.attr("value");
            }
        }
        return result;
    }

    /**
     * 登录
     *
     * @param username 用户名
     * @param password 密码
     * @param captcha  验证码
     */
    public Result login(String username, String password, String captcha, String token) {
        String host = "http://220.167.53.63:95/(" + token + ")";
        String url = host + "/default2.aspx";
        Map<String, Object> param = new HashMap<>();
        param.put("__VIEWSTATE", VIEWSTATE(token));
        param.put("TextBox1", username);
        param.put("TextBox2", password);
        param.put("TextBox3", captcha);
        param.put("Button1", "");
        HttpRequest request = HttpRequest.post(url)
                .form(param)
                .setFollowRedirects(true);
        HttpResponse response = request.execute();
        List<String> names = ReUtil.findAll("[\\u4E00-\\u9FA5]+同学", response.body(), 0, new ArrayList<>());
        List<String> captchas = ReUtil.findAll("language=\'javascript\'", response.body(), 0, new ArrayList<>());
        List<String> error = ReUtil.findAll("ERROR", response.body(), 0, new ArrayList<>());
        List<String> outSchool = ReUtil.findAll("用户名不存在或未按照要求参加教学活动", response.body(), 0, new ArrayList<>());
        List<String> pwdNeedChange = ReUtil.findAll("您的密码为弱密码，存在安全风险，需修改后才能登陆", response.body(), 0, new ArrayList<>());
        if (names.size() > 0) {
            return Result.builder()
                    .result(ResultStatus.success)
                    .msg("登陆成功!")
                    .item(names.get(0).replaceAll("同学", ""))
                    .build();
        } else if (captchas.size() > 0) {
            return Result.builder()
                    .result(ResultStatus.fail)
                    .msg("验证码错误!")
                    .build();
        } else if (error.size() > 0) {
            return Result.builder()
                    .result(ResultStatus.fail)
                    .msg("登录失败！账号或密码错误！")
                    .build();
        } else if (outSchool.size() > 0) {
            return Result.builder()
                    .result(ResultStatus.fail)
                    .msg("你已经是大人了!(该账户无法再登录教务系统)")
                    .build();
        } else if (pwdNeedChange.size() > 0) {
            return Result.builder()
                    .result(ResultStatus.fail)
                    .msg("教务系统需要您修改密码，请通过http://zf.css0209.cn或http://220.167.53.63:95修改密码")
                    .build();
        }
        log.info(response.body());
        return Result.builder().build();
    }

    /**
     * 查询成绩页面
     *
     * @param xh   学号
     * @param name 姓名
     * @return response对象
     */
    private HttpResponse gradePage(String xh, String name, String token) {
        String host = "http://220.167.53.63:95/(" + token + ")";
        String url = host + "/xscjcx.aspx?xh=" + xh + "&xm=" + URLUtil.encode(name) + "&gnmkdm=N121605";
        HttpRequest request = HttpRequest.get(url)
                .header(Header.REFERER, host + "xs_main.aspx?xh=" + xh)
                .header(Header.USER_AGENT, "Mozilla/5.0 (Macintosh; Intel Mac OS X 10.13; rv:67.0) Gecko/20100101 Firefox/67.0");
        HttpResponse response = request.execute();
        return response;
    }

    /**
     * 获取个人信息
     *
     * @param name 姓名
     * @param xh   学号
     * @return 学生基本信息
     */
    public UserInfo myInfo(String name, String xh, String token) {
        //获取页面内容
        HttpResponse response = gradePage(xh, name, token);
        //获取个人信息存入集合
        List<String> xys = ReUtil.findAll("学院：[\\u4e00-\\u9fa5]+", response.body(), 0, new ArrayList<>());
        List<String> zys = ReUtil.findAll("\"lbl_zymc\">[\\u4e00-\\u9fa5]+", response.body(), 0, new ArrayList<>());
        List<String> xzbs = ReUtil.findAll("行政班：[\\u4E00-\\u9FA5A-Za-z0-9_]+", response.body(), 0, new ArrayList<>());
        //获取信息
        String xy = xys.get(0).replaceAll("学院：", "");
        String zy = zys.get(0).replaceAll("\"lbl_zymc\">", "");
        String xzb = xzbs.get(0).replaceAll("行政班：", "");
        return UserInfo.builder().name(name)
                .sid(xh)
                .faculty(xy)
                .profession(zy)
                .asClass(xzb)
                .build();
    }


    /**
     * 挂科的成绩
     *
     * @param xh   学号
     * @param name 姓名
     * @return 未通过成绩
     */
    public JSONObject failedGrade(String xh, String name, String token) {
        JSONObject result = new JSONObject();
        //获取查询成绩页面
        HttpResponse response = gradePage(xh, name, token);

        JSONArray grades = getTableValue(response.body());
        result.put("failedGrade", grades);
        return result;
    }

    /**
     * 数据映射
     *
     * @param response
     * @return
     */
    private JSONArray getTableValue(String response) {
        //解析获取数据
        Document html = Jsoup.parse(response);
        Element table = html.select("table.datelist").first();
        return getItems(table);
    }

    /**
     * 表格内容获取
     *
     * @param table 表格
     * @return 获取内容
     */
    private JSONArray getItems(Element table) {
        Elements trs = table.getElementsByTag("tr");
        Elements tableTitleTd = trs.get(0).getElementsByTag("td");
        String[] tableTitle = new String[tableTitleTd.size()];
        for (int i = 0; i < tableTitle.length; i++) {
            String titleStr = tableTitleTd.get(i).text();
            List<Pinyin> pibyinList = HanLP.convertToPinyinList(titleStr);
            StringBuilder titlePy = new StringBuilder();
            for (Pinyin pinyin : pibyinList) {
                String py = pinyin.getPinyinWithoutTone();
                titlePy.append(py);
            }
            tableTitle[i] = titlePy.toString();
        }
        trs.remove(0);
        JSONArray jsonArray = new JSONArray();
        for (Element tr : trs) {
            JSONObject json = new JSONObject();
            Elements tds = tr.getElementsByTag("td");
            for (int x = 0; x < tds.size(); x++) {
                json.put(tableTitle[x], tds.get(x).text());
            }
            jsonArray.put(json);
        }
        return jsonArray;
    }

    /**
     * 查询成绩
     *
     * @param name 姓名
     * @param xh   学号
     * @return Result对象
     */
    public Result grade(String name, String xh, QueryParams queryParams, String token) {
        //返回结果
        JSONObject json = new JSONObject();
        //获得查询结果
        HttpRequest request = postGrade(name, xh, queryParams, token);
        HttpResponse response;
        try {
            response = request.execute();
        } catch (Exception e) {
            return Result.builder()
                    .result(ResultStatus.fail)
                    .msg("请重新登录")
                    .item(json)
                    .build();
        }
        //解析结果
        if (BtnType.Button1 == queryParams.getBtn()) {
            JSONObject grade = gradeStatistics(response.body());
            json.put("gradeStatistics", grade);
        } else {
            JSONArray jsonArray = getTableValue(response.body());
            json.put("grade", jsonArray);
        }
        return Result.builder()
                .result(ResultStatus.success)
                .msg("请求成功")
                .item(json)
                .build();
    }

    /**
     * 成绩统计查询
     *
     * @param response 成绩统计html页面
     * @return json
     */
    private JSONObject gradeStatistics(String response) {
        JSONObject jsonObject = new JSONObject();
        Document html = Jsoup.parse(response);
        try {
            jsonObject.put("data2", getItems(html.selectFirst("#Datagrid2")));
//            jsonObject.put("data6", getItems(html.selectFirst("#DataGrid6")));
            jsonObject.put("data7", getItems(html.selectFirst("#DataGrid7")));
            jsonObject.put("totalPeople", html.selectFirst("#zyzrs").text());
            jsonObject.put("averageScorePoint", html.selectFirst("#pjxfjd").text());
            jsonObject.put("sumOfGradePoints", html.selectFirst("#xfjdzh").text());
            jsonObject.put("creditStatistics", html.selectFirst("#xftj").text());
        } catch (Exception e) {
            log.warn(e.getMessage());
            return jsonObject;
        }

        return jsonObject;
    }

    /**
     * 获取成绩查询结果
     *
     * @param name        名字
     * @param xh          学号
     * @param queryParams 客户端传来数据
     * @return HttpRequest
     */
    private HttpRequest postGrade(String name, String xh, QueryParams queryParams, String token) {
        String host = "http://220.167.53.63:95/(" + token + ")";
        HttpResponse response = gradePage(xh, name, token);
        Document html = Jsoup.parse(response.body());
        //表单数据
        String EVENTTARGET;
        String EVENTARGUMENT;
        String VIEWSTATE;
        try {
            EVENTTARGET = html.selectFirst("input[name=__EVENTTARGET]").attr("value");
            EVENTARGUMENT = html.selectFirst("input[name=__EVENTARGUMENT]").attr("value");
            VIEWSTATE = html.selectFirst("input[name=__VIEWSTATE]").attr("value");
        } catch (Exception e) {
            log.warn(e.getMessage());
            return null;
        }


        String btnValue = URLUtil.encode(queryParams.getBtn().getDescription());
        Map<String, Object> parmes = new HashMap<>(7);
        parmes.put("__EVENTTARGET", EVENTTARGET);
        parmes.put("__EVENTARGUMENT", EVENTARGUMENT);
        parmes.put("__VIEWSTATE", VIEWSTATE);
        parmes.put("ddlXN", queryParams.getYear());
        parmes.put("ddlXQ", queryParams.getSemester());
        parmes.put("ddl_kcxz", queryParams.getCourseNature());
        parmes.put(queryParams.getBtn().toString(), btnValue);
        return HttpRequest.post(host + "/xscjcx.aspx?xh=" + xh + "&xm=" + URLUtil.encode(name) + "&gnmkdm=N121605")
                .header(Header.REFERER, host + "xs_main.aspx?xh=" + xh)
                .header(Header.USER_AGENT, "Mozilla/5.0 (Macintosh; Intel Mac OS X 10.13; rv:67.0) Gecko/20100101 Firefox/67.0; PaleBlueYk's reptiles")
                .form(parmes);
    }
}
