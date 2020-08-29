package cn.css0209.flea.user.controller;

import cn.css0209.flea.user.model.Result;
import cn.css0209.flea.user.types.ResultStatus;
import cn.css0209.flea.user.utils.EnumUtil;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import static reactor.core.publisher.Mono.just;

/**
 * @author paleBlue
 */
@RestController
@RequestMapping("/dictionaries")
public class EnumListController {
    @GetMapping("/enums")
    public Mono<Result> enums() {
        return just(EnumUtil.getEnumMapList())
                .map(list -> Result.builder().result(ResultStatus.success)
                        .msg("枚举字典")
                        .item(list)
                        .build())
                .onErrorResume(throwable -> just(Result.builder().result(ResultStatus.fail).msg("error:" + throwable).build()));
    }
}
