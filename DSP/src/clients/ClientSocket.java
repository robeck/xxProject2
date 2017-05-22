package clients;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
// create socket using TCP to connect server
// using I/O stream to transform data
// close socket

/**
 * 
 * @author mengyuz2@student.unimelb.edu.au
 * @author haozhic1@student.unimelb.edu.au
 * @author feifanl1@student.unimelb.edu.au
 * @author xiangl14@student.unimelb.edu.au
 *
 */
public class ClientSocket {
	//default address;
//	private static int portConnect = 3009;
//	private static String ip = "localhost";
  private static String ip = "localhost";
  private static int portConnect = 3781;
/**
 * 
 * @param args
 * @throws UnknownHostException
 * @throws ParseException
 * @throws org.apache.commons.cli.ParseException
 * @throws InterruptedException
 */
	public static void main(String args[]) throws UnknownHostException, ParseException, org.apache.commons.cli.ParseException, InterruptedException{  
		  // Create a Parser  
		CommandLineParser parser = new DefaultParser();  
		Options options = new Options( );  
        options.addOption("exchange","exchange server list with server");  
		options.addOption("debug", "print debug information" );  
		options.addOption("fetch", "fetch resources from server" ); 
		options.addOption("publish", "publish resource on server" ); 
		options.addOption("query", "query for resources from server" ); 
		options.addOption("remove", "remove resource from server" ); 
		options.addOption("share", "share resource on server" ); 
		options.addOption("secure","connect secure socket");
		  
		Option name = Option.builder("name").argName("arg").hasArg().desc("resource name").build();
		options.addOption(name);
		Option channel = Option.builder("channel").argName("arg").hasArg().desc("channel").build();
		options.addOption(channel);
		Option description = Option.builder("description").argName("arg").hasArg().desc("resource description").build();
		options.addOption(description);
		Option host = Option.builder("host").argName("arg").hasArg().desc("server host, a domain name or IP address").build();
		options.addOption(host);
		Option owner = Option.builder("owner").argName("arg").hasArg().desc("owner").build();
		options.addOption(owner);
		Option port = Option.builder("port").argName("arg").hasArg().desc("server port, an integer").build();
		options.addOption(port);
		Option secret = Option.builder("secret").argName("arg").hasArg().desc("secret").build();
		options.addOption(secret);
		Option servers = Option.builder("servers").argName("arg").hasArg().desc("server list, host1:port1,host2:port2,...").build();
		options.addOption(servers);
		Option tags = Option.builder("tags").argName("arg").hasArg().desc("resource tags, tag1,tag2,tag3,...").build();
		options.addOption(tags);
		Option uri = Option.builder("uri").argName("args").hasArg().desc("resource URI").build();
		options.addOption(uri);	
		
		
		// Parse the program arguments  
		CommandLine commandLine = parser.parse( options, args );  
		
		if(commandLine.hasOption("port")){
			 portConnect = Integer.parseInt(commandLine.getOptionValue("port"));
		}
		if(commandLine.hasOption("host")){
			 ip = commandLine.getOptionValue("host");
		}
		
		if(commandLine.hasOption("secure"))
		{
			try{
				
			System.setProperty("javax.net.ssl.keyStore", "bin/Client.jks");
			
			System.setProperty("javax.net.ssl.trustStore", "bin/Client.jks");
			
			System.setProperty("javax.net.ssl.keyStorePassword","comp90015");
			
			
			System.setProperty("javax.net.debug","all");
			
			//Create SSL socket and connect it to the remote server 
			SSLSocketFactory sslsocketfactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
			SSLSocket sslsocket = (SSLSocket) sslsocketfactory.createSocket(ip, portConnect);
			
			if(commandLine.hasOption("debug")){
				System.out.println("setting debug on");
				System.out.println("Doing "+ args[0] + " to host " +ip+ ": " + portConnect);
			}
			
			DataInputStream input = new DataInputStream(sslsocket.getInputStream());
			DataOutputStream output = new DataOutputStream(sslsocket.getOutputStream());
			
			
			JSONObject command = new JSONObject();
		    command = ClientCommand.toJSON(commandLine);
		    
//		    System.out.println(command.toJSONString());
		    output.writeUTF(command.toJSONString());
	    	output.flush();
			
			

	    	if(commandLine.hasOption("fetch")){
	    		Download.DownloadFile(input, commandLine);
	    	}else{
	    		String response = "";
	    		do{
	    						
	    			String responsePart;
					responsePart = input.readUTF();	
	    			response += responsePart + "\n";
	    			//Thread.sleep(1000);
	    			
	    		}while(input.available() > 0);
	    		//Debug command line option
				if(commandLine.hasOption("debug")){
					System.out.println("RECEIVED: "+response);
				}
	    	}


//			System.out.println("RECEIVED: "+ "all done");

				sslsocket.close();
			}catch(IOException e)
			{
				e.printStackTrace();
			}
		}
		
		else{
		try{
		//•	创建一个 Socket 实例：构造函数向指定的远程主机和端口建立一个 TCP 连接；
		Socket client = new Socket(ip,portConnect);
		
		if(commandLine.hasOption("debug")){
			System.out.println("setting debug on");
			System.out.println("Doing "+ args[0] + " to host " +ip+ ": " + portConnect);
		}
		//•	通过套接字的 I/O 流与服务端通信；
		
		DataInputStream input = new DataInputStream(client.getInputStream());
	    DataOutputStream output = new DataOutputStream(client.getOutputStream());

		//transform commandLine to JSON and output
	    JSONObject command = new JSONObject();
	    command = ClientCommand.toJSON(commandLine);
	    
//	    System.out.println(command.toJSONString());
	    output.writeUTF(command.toJSONString());
    	output.flush();
    	
//    	try {
//    		client.setSoTimeout(100 * 100);
//        } catch (Exception e) {
//            System.out.println("connection fail");
//            System.exit(1);
//        }

    	//if command is fetch, download file
		
    	if(commandLine.hasOption("fetch")){
    		Download.DownloadFile(input, commandLine);
    	}else{
    		String response = "";
    		do{
    			Thread.sleep(1000); 			
    			String responsePart;
				responsePart = input.readUTF();	
    			response += responsePart + "\n";
    			//Thread.sleep(1000);
    			Thread.sleep(1000);
    		}while(input.available() > 0);
    		//Debug command line option
			if(commandLine.hasOption("debug")){
				System.out.println("RECEIVED: "+response);
			}
    	}


//		System.out.println("RECEIVED: "+ "all done");

			client.close();
		} catch (IOException e) {
			System.out.println("There is something worry, please try again!");
			e.printStackTrace();

		}
  	
	 }
	}
			
}
    		
    	
    	

		
    	
    	
    	
	

