package server;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;
/**
 * 
 * @author haozhic1@student.unimelb.edu.au
 * @author mengyuz2@student.unimelb.edu.au
 * @author feifanl1@student.unimelb.edu.au
 * @author xiangl14@student.unimelb.edu.au
 * String the server, output the server information
 */
@Setter
@Getter
public class LoggerFormate {
	private static String host;
	private static int port;
	private static int sport;
	private static String secret;
	private static int exchangetime;
	
	
	public void Logger(String host,int port,int sport,String secret) {
		DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss.SSS");
		
		System.out.println(dateFormat.format(new Date())+" - [EZShare.Server.main] - [INFO] - Starting the EZShare Server ");
		
		System.out.println(dateFormat.format(new Date())+" - [EZShare.ServerControl] - [INFO] - using advertised hostname: "+host);
		
		System.out.println(dateFormat.format(new Date())+" - [EZShare.ServerControl] - [INFO] - using secret: " + secret);
		
		System.out.println(dateFormat.format(new Date())+" - [EZShare.ServerIO] - [INFO] - bound to port "+ port + " and " + sport);
		
		System.out.println(dateFormat.format(new Date())+" - [EZShare.ServerExchange] - [INFO] - started ");
		
		
    }
}
