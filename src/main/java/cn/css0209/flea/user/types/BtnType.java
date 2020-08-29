package cn.css0209.flea.user.types;

/**
 * @author blankyk
 */

public enum BtnType {
    Button1("成绩统计"),
    Button2("未通过成绩"),
    btn_zg("课程最高成绩"),
    btn_zcj("历年成绩"),
    btn_xn("学年成绩"),
    btn_xq("学期成绩");

    private String description;

    BtnType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
