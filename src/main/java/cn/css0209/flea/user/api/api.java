package cn.css0209.flea.user.api;

import cn.css0209.flea.reptile.Zhengfang;
import cn.hutool.core.io.FileUtil;
import cn.hutool.json.JSONObject;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

@RestController
@RequestMapping("/user")
public class api {
    Zhengfang zf;

    public api() {
//        初始化爬虫
        zf = new Zhengfang();
    }

    /**
     * 获取验证码
     *
     * @return 图片
     * @throws IOException
     */
    @RequestMapping(value = "/captcha", produces = MediaType.IMAGE_JPEG_VALUE)
    @ResponseBody
    public byte[] captCha() throws IOException {
        zf.captCha();
        File file = FileUtil.file("cn/css0209/flea/user/img/captCha.jpg");
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

    @GetMapping("myinfo")
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

    @GetMapping("failed_grade")
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
     * @param jsonObject
     * {
     *     "year": "2018-2019",
     * 	    "semster": "2",
     * 	    "course_natrue": "",
     * 	    "btn": "btn_zg"
     * }
     * @return 查询成绩
     */
    @GetMapping("select_grade")
    public Result selectGrade(HttpServletRequest servletRequest, @RequestBody JSONObject jsonObject) {
        HttpSession session = servletRequest.getSession();
        String name = session.getAttribute("name").toString();
        String xh = session.getAttribute("xh").toString();

        Result result = zf.grade(name, xh,jsonObject);

        return result;
    }
}
