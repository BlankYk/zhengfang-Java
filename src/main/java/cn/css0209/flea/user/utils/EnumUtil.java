package cn.css0209.flea.user.utils;

import cn.css0209.flea.user.types.BtnType;
import cn.css0209.flea.user.types.ResultStatus;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.extern.log4j.Log4j2;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author paleBlue
 * TODO 这段代码我也看不懂
 */
@Log4j2
public class EnumUtil {
    public static Map<String, Map<String, String>> getEnumMapList() {
        Map<String, Map<String, String>> result = Maps.newLinkedHashMap();
        log.info("获取字典...");
        List<Class<? extends Enum>> mapList = Lists.newArrayList(
                BtnType.class,
                ResultStatus.class
        );

        mapList.forEach(e -> result.put(e.getSimpleName(), getEnumDescriptionMap(e)));
        return result;
    }

    private static Map<String, String> getEnumDescriptionMap(Class<? extends Enum> clz) {
        return Lists.newArrayList(clz.getEnumConstants()).stream().collect(Collectors.toMap(Enum::name, e -> {
            Method getDescription = ReflectionUtils.findMethod(clz, "getDescription");
            if (Objects.nonNull(getDescription)) {
                try {
                    return getDescription.invoke(e).toString();
                } catch (IllegalAccessException | InvocationTargetException e1) {
                    log.error(e.toString(), e1);
                }
            }
            return e.toString();
        }));
    }
}
