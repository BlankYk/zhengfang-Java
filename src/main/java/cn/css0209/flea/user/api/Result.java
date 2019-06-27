package cn.css0209.flea.user.api;

import cn.hutool.json.JSONObject;
import lombok.Data;

@Data
public class Result {
    private String result;
    private String msg;
    private JSONObject item;
}
