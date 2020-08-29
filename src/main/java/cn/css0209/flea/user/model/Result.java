package cn.css0209.flea.user.model;

import cn.css0209.flea.user.types.ResultStatus;
import lombok.Builder;
import lombok.Data;


/**
 * @author blankyk
 */
@Data
@Builder
public class Result {
    private ResultStatus result;
    private String msg;
    private Object item;

    @Override
    public String toString() {
        return "{\"result\":" + result + ", \"msg\":" + msg + ", \"item\":" + item + "}";
    }
}
