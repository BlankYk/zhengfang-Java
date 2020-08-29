package cn.css0209.flea.user.types;

/**
 * @author blankyk
 */

public enum ResultStatus {
    success("success"),
    fail("fail");

    private String description;


    ResultStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
