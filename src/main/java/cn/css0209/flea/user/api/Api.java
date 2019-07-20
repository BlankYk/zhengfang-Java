package cn.css0209.flea.user.api;

import cn.css0209.flea.reptile.Zhengfang;
import cn.hutool.core.io.FileUtil;
import cn.hutool.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * @author blankyk
 */
@RestController
@RequestMapping("/user")
public class Api {
    private Zhengfang zf;
    private Logger log = LoggerFactory.getLogger(this.getClass());

    /**
     * 获取验证码
     *
     * @return 图片
     * @throws IOException
     */
    @RequestMapping(value = "/captcha", produces = MediaType.IMAGE_JPEG_VALUE)
    @ResponseBody
    public byte[] captCha(String path) throws IOException {
        zf = new Zhengfang();
        zf.captCha(path);
        File file = FileUtil.file("cn/css0209/flea/user/img/captCha"+path+".jpg");
        FileInputStream inputStream = new FileInputStream(file);
        byte[] bytes = new byte[inputStream.available()];
        inputStream.read(bytes, 0, inputStream.available());
        return bytes;
    }

    @PostMapping("/login")
    public Result login(String username, String password, String captcha, HttpServletRequest servletRequest) {
        Result result = new Result();
        try {
            result = zf.login(username, password, captcha);
            HttpSession session = servletRequest.getSession();
            JSONObject loginJson = result.getItem();
            session.setAttribute("name", loginJson.get("name"));
            session.setAttribute("xh", username);
        } catch (StringIndexOutOfBoundsException e) {
            result.setResult("fail");
            result.setMsg("登录失败,教务系统又双叒叕崩了！！");
        } catch (NullPointerException e) {
            result.setResult("fail");
            result.setMsg("输入错误，确认账号密码验证码正确");
        }
        return result;
    }

    @GetMapping("myInfo")
    public Result myInfo(HttpServletRequest servletRequest) {
        Result result = new Result();
        HttpSession session = servletRequest.getSession();
        String name = session.getAttribute("name").toString();
        String xh = session.getAttribute("xh").toString();
        try {
            JSONObject infoJson = zf.myInfo(name, xh);
            result.setResult("success");
            result.setMsg("获取信息成功");
            result.setItem(infoJson);
        } catch (IndexOutOfBoundsException e) {
            result.setResult("fail");
            result.setMsg("出现异常了");
        }
        return result;
    }

    @GetMapping("failedGrade")
    public Result failedGrade(HttpServletRequest servletRequest) {
        Result result = new Result();
        //从session中获取名字和学号
        HttpSession session = servletRequest.getSession();
        String xh = session.getAttribute("xh").toString();
        String name = session.getAttribute("name").toString();
        try {
            JSONObject failedGradeJson = zf.failedGrade(xh, name);
            result.setResult("success");
            result.setMsg("查询到未通过成绩");
            result.setItem(failedGradeJson);
        } catch (NullPointerException e) {
            result.setMsg("请登录");
            result.setResult("fail");
        } catch (IndexOutOfBoundsException e) {
            result.setResult("fail");
            result.setMsg("查询失败");
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 查询成绩
     * @param servletRequest 请求
     * @param year 年份
     * @param semester 学期
     * @param courseNature 课程性质
     * @param btn 查询按钮
     * {
     *     "year": "2018-2019",
     * 	    "semster": "2",
     * 	    "course_natrue": "",
     * 	    "btn": "btn_zg"
     * }
     * @return 查询成绩
     */
    @GetMapping("selectGrade")
    public Result selectGrade(HttpServletRequest servletRequest, String year,String semester,String courseNature,String btn) {
        HttpSession session = servletRequest.getSession();
        String name = session.getAttribute("name").toString();
        String xh = session.getAttribute("xh").toString();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("year", year);
        jsonObject.put("semester", semester);
        jsonObject.put("course_nature", courseNature);
        jsonObject.put("btn", btn);
//        log.info(jsonObject.toString());
        Result result = zf.grade(name, xh,jsonObject);
        return result;
    }

    @DeleteMapping("loginOut")
    public Result loginOut(HttpServletRequest request) {
        HttpSession session = request.getSession();
        Result result = new Result();
        try {
            session.invalidate();
            result.setResult("success");
            result.setMsg("登出");
        } catch (NullPointerException e) {
            result.setResult("fail");
            result.setMsg("error:"+e);
        }
        return result;
    }
}
