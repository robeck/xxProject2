package server;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

import static java.lang.Boolean.parseBoolean;
import static java.lang.Integer.parseInt;

/**
 * @author haozhic1@student.unimelb.edu.au
 * @author mengyuz2@student.unimelb.edu.au
 * @author feifanl1@student.unimelb.edu.au
 * @author xiangl14@student.unimelb.edu.au
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ServerOptions {

    public static final String HOSTNAME = "advertisedhostname";
    public static final String INTERVAL_CONN = "connectionintervallimit";
    public static final String INTERVAL_EX = "exchangeinterval";
    public static final String PORT = "port";
    public static final String SPORT = "sport";
    public static final String SECRET = "secret";
    public static final String DEBUG = "debug";

    private String hostname;
    private int connectionInterval;
    private int exchangeInterval;
    private int port;
    private int sport;
    private String secret;
    private boolean debug;
}
