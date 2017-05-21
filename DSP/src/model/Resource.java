package model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 
 * @author haozhic1@student.unimelb.edu.au
 * @author mengyuz2@student.unimelb.edu.au
 * @author feifanl1@student.unimelb.edu.au
 * @author xiangl14@student.unimelb.edu.au
 * import Lombok.jar to Automatically generate Constructor, Setter and Getter
 */
@NoArgsConstructor
public class Resource {
    @Setter @Getter
    private String name = "";
    @Setter @Getter
    private String description = "";
    @Setter @Getter
    private String tags = "";
    /**
     * URI  file:// http:// ftp:// 
     * if shared resource,  uri just as file://
     * 
     *
     */
    @Setter @Getter
    private String uri = "";
    /**
     * default channel means public channel, others private
     * channel can be used without owner
     */
    @Setter @Getter
    private String channel = "";
    /**
     * default owner means that anyone can update this resource
     * owner can be used without channel
     *
     * owner and channel can be used to refer to Resource in a later time
     */
    @Setter @Getter
    private String owner = "";
    @Setter @Getter
    private String ezServer = "";

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Resource resource = (Resource) o;
        return Objects.equals(uri, resource.uri) &&
                Objects.equals(channel, resource.channel) &&
                Objects.equals(owner, resource.owner);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uri, channel, owner);//primary key of Resource
    }

//    adapter method
    public List<String> getTagList(){
        if ("".equals(this.tags)){
            return Collections.EMPTY_LIST;
        }else{
            return Arrays.asList(this.tags.split(","));
        }
    }

//    utility method
    public String toValidString(String s) {
        if (null != s){
            return s.trim().replace("\\0", "");
        }
        return s;
    }

    public Resource toValid() {
        this.name = toValidString(this.name);
        this.description = toValidString(this.description);
        this.uri = toValidString(this.uri);
        this.channel = toValidString(this.channel);
        this.owner = toValidString(this.owner);
        this.ezServer = toValidString(this.ezServer);
        return this;
    }
}
