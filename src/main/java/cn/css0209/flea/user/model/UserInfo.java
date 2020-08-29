package cn.css0209.flea.user.model;

import lombok.Builder;
import lombok.Data;

/**
 * @author paleBlue
 */
@Data
@Builder
public class UserInfo {
    /**
     * 姓名
     */
    private String name;
    /**
     * 学号
     */
    private String sid;
    /**
     * 院系
     */
    private String faculty;
    /**
     * 专业
     */
    private String profession;
    /**
     * 行政班
     */
    private String asClass;
}
