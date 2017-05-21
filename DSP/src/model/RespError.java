package model;

import lombok.Getter;
import lombok.Setter;
/**
 * 
 * @author haozhic1@student.unimelb.edu.au
 * @author mengyuz2@student.unimelb.edu.au
 * @author feifanl1@student.unimelb.edu.au
 * @author xiangl14@student.unimelb.edu.au
 * import Lombok.jar to Automatically generate Constructor, Setter and Getter
 */
@Getter
@Setter
public class RespError extends Resp {
    public static final String ERROR = "error";
    public RespError(String errorMessage) {
        super(ERROR);
        this.errorMessage = errorMessage;
    }

    private String errorMessage;
}
