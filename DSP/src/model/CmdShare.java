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
public class CmdShare {
    private final String command = "SHARE";
    /**
     * this server secret is required for the command to be successful.
     * secret must be handle
     */
    private String secret;
    /**
     * uri of SHARE resource must be file://
     * uri of PUBLISH resource can not be file://
     *
     * Sharing a resource with the same channel and URI but different owner is not allowed
     * Sharing a resource with the same primary key as an existing resource simply overwrites the existing resource
     *
     * primary key of share resource: (owner,channel,uri)
     */
    private Resource resource;
}
