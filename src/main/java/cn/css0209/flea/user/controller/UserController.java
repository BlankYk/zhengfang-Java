package cn.css0209.flea.user.controller;

import cn.css0209.flea.reptile.Zhengfang;
import cn.css0209.flea.user.model.LoginRequest;
import cn.css0209.flea.user.model.QueryParams;
import cn.css0209.flea.user.model.Result;
import cn.css0209.flea.user.types.BtnType;
import cn.css0209.flea.user.types.ResultStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.Valid;

/**
 * @author blankyk
 */
@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {
    @Autowired
    private Zhengfang zf;

    /**
     * 获取token
     */
    @GetMapping("/token")
    public Mono<Result> getToken(WebSession session) {
        return Mono.just(Result.builder().build()).map(res -> {
            String token = zf.getToken();
            res.setItem(token);
            log.info("==>token:{}", token);
            session.getAttributes().put("token", token);
            return res;
        });
    }

    /**
     * 获取验证码
     *
     * @return 图片
     */
    @RequestMapping(value = "/captcha", produces = MediaType.IMAGE_JPEG_VALUE)
    @ResponseBody
    public Flux<byte[]> captCha(String path, String token) {
        return Flux.just(path)
                .map(p -> {
                    // TODO 这个try catch 怎么优化一下啊，我淦
                    try {
                        return zf.captCha(path, token);
                    } catch (Exception e) {
                        log.error("获取验证码失败!");
                    }
                    return new byte[2];
                });
    }

    @PostMapping("/login")
    public Mono<Result> login(@Valid @RequestBody LoginRequest loginRequest, WebSession session, ServerHttpRequest request) {
        return Mono.just(zf.login(loginRequest.getUsername(), loginRequest.getPassword(), loginRequest.getCaptcha(), request.getHeaders().getFirst("token")))
                .doOnNext(result -> {
                    log.info("<=={}登录,验证码:{}", loginRequest.getUsername(), loginRequest.getCaptcha());
                    if (result.getResult() == ResultStatus.success) {
                        session.getAttributes().put("name", result.getItem());
                        session.getAttributes().put("xh", loginRequest.getUsername());
                        log.info("{}登录成功", result.getItem());
                    }
                });
    }

    @GetMapping("/info")
    public Mono<Result> myInfo(ServerHttpRequest request, WebSession session) {
        String name = session.getAttribute("name");
        String xh = session.getAttribute("xh");
        return Mono.just(zf.myInfo(name, xh, request.getHeaders().getFirst("token"))).map(userInfo ->
                Result.builder().result(ResultStatus.success)
                        .msg("获取信息成功")
                        .item(userInfo)
                        .build());
    }

    @GetMapping("/failedGrade")
    public Mono<Result> failedGrade(ServerHttpRequest request, WebSession session) {
        //从session中获取名字和学号
        String xh = session.getAttribute("xh");
        String name = session.getAttribute("name");
        return Mono.just(zf.failedGrade(xh, name, request.getHeaders().getFirst("token"))).map(jsonObject -> Result.builder()
                .result(ResultStatus.success)
                .msg("查询成功")
                .item(jsonObject)
                .build())
                .onErrorResume(e -> Mono.just(Result.builder().result(ResultStatus.fail).msg("查询失败").item(e).build()));
    }

    /**
     * 查询成绩
     *
     * @param session      session
     * @param year         年份
     * @param semester     学期
     * @param courseNature 课程性质
     * @param btn          查询按钮
     *                     {
     *                     "year": "2018-2019",
     *                     "semster": "2",
     *                     "course_natrue": "",
     *                     "btn": "btn_zg"
     *                     }
     * @return 查询成绩
     */
    @GetMapping("/selectGrade")
    public Mono<Result> selectGrade(WebSession session, ServerHttpRequest request, String year, String semester, String courseNature, BtnType btn) {
        String name = session.getAttribute("name");
        String xh = session.getAttribute("xh");
        return Mono.just(zf.grade(name, xh, QueryParams.builder()
                        .year(year)
                        .semester(semester)
                        .courseNature(courseNature)
                        .btn(btn)
                        .build(),
                request.getHeaders().getFirst("token")));
    }

    @DeleteMapping("/loginOut")
    public Mono<Result> loginOut(WebSession session) {
        return session.invalidate().map(s -> Result.builder().result(ResultStatus.success).msg("登出成功").build())
                .onErrorResume(e -> Mono.just(Result.builder().result(ResultStatus.fail).msg("error:" + e).build()));
    }
}
