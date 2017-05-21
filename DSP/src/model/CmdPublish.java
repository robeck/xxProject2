package model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 
 * @author haozhic1@student.unimelb.edu.au
 * @author mengyuz2@student.unimelb.edu.au
 * @author feifanl1@student.unimelb.edu.au
 * @author xiangl14@student.unimelb.edu.au
 * import Lombok.jar to Automatically generate Constructor, Setter and Getter
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class CmdPublish {
    private final String command = "PUBLISH";
    /**
     * valid resources must be given;
     * missing fields of resource may be filled with defaults
     * uri of resource must be present, must be absolute and cannot be a file://
     * publishing resource with the same primary key will simple overwrite the existing one.
     * unique key of resource on server: (channel, owner)
     */
    private Resource resource;
}
