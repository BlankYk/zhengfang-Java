package cn.css0209.flea.config;

import cn.css0209.flea.user.model.Result;
import cn.css0209.flea.user.types.ResultStatus;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.internal.StringUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import org.springframework.web.util.pattern.PathPattern;
import org.springframework.web.util.pattern.PathPatternParser;
import reactor.core.publisher.Mono;

/**
 * @author paleBlue
 * 拦截处理token
 */
@Component
@Slf4j
public class AuthFilter implements WebFilter {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        PathPattern pattern = new PathPatternParser().parse("/**");
        PathPattern exactPath = new PathPatternParser().parse("/user/token");
        PathPattern exactPath2 = new PathPatternParser().parse("/user/captcha");
        ServerHttpRequest request = exchange.getRequest();
        if (pattern.matches(request.getPath().pathWithinApplication()) &&
                !exactPath.matches(request.getPath().pathWithinApplication()) &&
                !exactPath2.matches(request.getPath().pathWithinApplication()) &&
                StringUtil.isBlank(request.getHeaders().getFirst("token"))) {
            log.info("请求被拦截");
            ServerHttpResponse response = exchange.getResponse();
            response.setStatusCode(HttpStatus.BAD_REQUEST);
            response.getHeaders().setContentType(MediaType.APPLICATION_JSON_UTF8);
            return response.writeWith(Mono.just(Result.builder().build()).map(res -> {
                res.setMsg("请求参数中无Token");
                res.setResult(ResultStatus.fail);
                return response.bufferFactory().wrap(res.toString().getBytes());
            }));
        }
        return chain.filter(exchange);
    }
}
