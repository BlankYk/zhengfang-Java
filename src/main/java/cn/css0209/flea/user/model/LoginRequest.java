package cn.css0209.flea.user.model;

import lombok.Data;

import javax.validation.constraints.NotEmpty;

/**
 * @author paleBlue
 */
@Data
public class LoginRequest {
    @NotEmpty
    private String username;
    @NotEmpty
    private String password;
    @NotEmpty
    private String captcha;
}
