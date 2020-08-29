package cn.css0209.flea.user.model;

import cn.css0209.flea.user.types.BtnType;
import lombok.Builder;
import lombok.Data;

/**
 * @author paleBlue
 */
@Data
@Builder
public class QueryParams {
    private String year;
    private String semester;
    private String courseNature;
    private BtnType btn;
}
