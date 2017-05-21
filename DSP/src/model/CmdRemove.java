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
public class CmdRemove {
    private final String command = "REMOVE";
    /**
     * using primaeykey: (channel, owner, uri)
     * 
     */
    private Resource resource;
}
