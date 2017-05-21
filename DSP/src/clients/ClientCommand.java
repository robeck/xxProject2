package clients;

import org.apache.commons.cli.CommandLine;
import org.json.simple.JSONObject;
import org.json.simple.JSONArray;

/**
 * @author mengyuz2@student.unimelb.edu.au
 * @author haozhic1@student.unimelb.edu.au
 * @author feifanl1@student.unimelb.edu.au
 * @author xiangl14@student.unimelb.edu.au
 * Handling each command and making them to json object
 */

public class ClientCommand {
	@SuppressWarnings("unchecked")
	public static JSONObject toJSON(CommandLine commandLine){
		JSONObject command = new JSONObject();
		JSONObject resource = new JSONObject();

		  //handle publish command
		if(commandLine.hasOption("publish") ){
            command.put("command", "PUBLISH");
			if(commandLine.hasOption("name") ){
				resource.put("name", commandLine.getOptionValue("name")); 
			}else resource.put("name", "");
			if( commandLine.hasOption("tags") ) {		
		        
	            String tags = commandLine.getOptionValue("tags");
	            
			    resource.put("tags", tags);
			}else resource.put("tags",""); 
			if( commandLine.hasOption("description") ) {  
				resource.put("description", commandLine.getOptionValue("description")); 
			}else resource.put("description", ""); 
			if( commandLine.hasOption("uri") ) {  
				resource.put("uri", commandLine.getOptionValue("uri")); 
			}else {
				System.out.println("no valid uri!");
				System.exit(1);
			}
			if(commandLine.hasOption("channel") ){
				resource.put("channel", commandLine.getOptionValue("channel")); 
			}else resource.put("channel", "");
			if( commandLine.hasOption("owner") ) {  
				resource.put("owner", commandLine.getOptionValue("owner")); 
			}else resource.put("owner", ""); 
			resource.put("ezserver", null);
			  
			command.put("resource", resource);
		}
		
		//handle exchange command
		else if(commandLine.hasOption("exchange")){
			
	        JSONArray serverList = new JSONArray();
	        String servers = commandLine.getOptionValue("servers");
	        if(servers.equals(""))
	        {
	        	command.put("serverList", "");
	        }
	        else{
	        String[] sServers = servers.split(",");
	        for (String sServer : sServers) {
	            String[] tempServer = sServer.split(":");
	            JSONObject serv = new JSONObject();
	            serv.put("hostname", tempServer[0]);
	            serv.put("port", tempServer[1]);
	            serverList.add(serv);
	        }
	        command.put("serverList", serverList);
	        }
	        command.put("command", "EXCHANGE");
	        
		}
		  
		//handle remove command
		else if(commandLine.hasOption("remove") ){
			command.put("command", "REMOVE");
			if(commandLine.hasOption("name") ){
				resource.put("name", commandLine.getOptionValue("name")); 
			}else resource.put("name", "");
			if( commandLine.hasOption("tags") ) {		
				 String tags = commandLine.getOptionValue("tags");
		            
				    resource.put("tags", tags);
			}else resource.put("tags","");
			if( commandLine.hasOption("description") ) {  
				resource.put("description", commandLine.getOptionValue("description")); 
			}else resource.put("description", ""); 
			if( commandLine.hasOption("uri") ) {  
				resource.put("uri", commandLine.getOptionValue("uri")); 
			}else{
				System.out.println("no valid uri!");
				System.exit(1);
			}
			if(commandLine.hasOption("channel") ){
				resource.put("channel", commandLine.getOptionValue("channel")); 
			}else resource.put("channel", "");
			if( commandLine.hasOption("owner") ) {  
				resource.put("owner", commandLine.getOptionValue("owner")); 
			}else resource.put("owner", ""); 
			resource.put("ezserver", null);		  
			command.put("resource", resource);
		}
		  
		
		//handle share command
		else if(commandLine.hasOption("share") ){
			command.put("command", "SHARE"); 
			command.put("secret", commandLine.getOptionValue("secret"));		  
			if(commandLine.hasOption("name") ){
				resource.put("name", commandLine.getOptionValue("name")); 
			}else resource.put("name", "");
			if( commandLine.hasOption("tags") ) {		
				 String tags = commandLine.getOptionValue("tags");
		            
				    resource.put("tags", tags);
			}else resource.put("tags","");
			if( commandLine.hasOption("description") ) {  
				resource.put("description", commandLine.getOptionValue("description")); 
			}else resource.put("description", ""); 
			if( commandLine.hasOption("uri") ) {  
				resource.put("uri", commandLine.getOptionValue("uri")); 
			}else {
				System.out.println("no valid uri!");
				resource.put("uri", "");
			}
			if(commandLine.hasOption("channel") ){
				resource.put("channel", commandLine.getOptionValue("channel")); 
			}else resource.put("channel", "");
			if (commandLine.hasOption("owner")) {
				resource.put("owner", commandLine.getOptionValue("owner"));
			} else
				resource.put("owner", "");
			resource.put("ezserver", null);
			command.put("resource", resource);
		}

		//handle query command
		else if (commandLine.hasOption("query")) {
			command.put("command", "QUERY");
			command.put("relay", true);
			if (commandLine.hasOption("name")) {
				resource.put("name", commandLine.getOptionValue("name"));
			} else
				resource.put("name", "");
			if (commandLine.hasOption("tags")) {
				
				 String tags = commandLine.getOptionValue("tags");
		            
				    resource.put("tags", tags);
			} else
				resource.put("tags", "");
			if (commandLine.hasOption("description")) {
				resource.put("description", commandLine.getOptionValue("description"));
			} else
				resource.put("description", "");
			if (commandLine.hasOption("uri")) {
				resource.put("uri", commandLine.getOptionValue("uri"));
			} else {
				resource.put("uri","");
			}
			if (commandLine.hasOption("channel")) {
				resource.put("channel", commandLine.getOptionValue("channel"));
			} else
				resource.put("channel", "");
			if (commandLine.hasOption("owner")) {
				resource.put("owner", commandLine.getOptionValue("owner"));
			} else
				resource.put("owner", "");
			resource.put("ezserver", null);
			command.put("resourceTemplate", resource);
		}

		//handle fetch command
		else if (commandLine.hasOption("fetch")) {
			command.put("command", "FETCH");
			if (commandLine.hasOption("name")) {
				resource.put("name", commandLine.getOptionValue("name"));
			} else
				resource.put("name", "");
			if (commandLine.hasOption("tags")) {
				 String tags = commandLine.getOptionValue("tags");
		            
				    resource.put("tags", tags);
			} else
				resource.put("tags", "");
			if (commandLine.hasOption("description")) {
				resource.put("description", commandLine.getOptionValue("description"));
			} else
				resource.put("description", "");
			if (commandLine.hasOption("uri")) {
				resource.put("uri", commandLine.getOptionValue("uri"));
			} else {
				System.out.println("no valid uri!");
				System.exit(1);
			}
			if (commandLine.hasOption("channel")) {
				resource.put("channel", commandLine.getOptionValue("channel"));
			} else
				resource.put("channel", "");
			if (commandLine.hasOption("owner")) {
				resource.put("owner", commandLine.getOptionValue("owner"));
			} else
				resource.put("owner", "");
			resource.put("ezserver", null);
			command.put("resourceTemplate", resource);
		}

		else {
			System.out.println("lack of vaild cammand");
			System.exit(1);
		}

		// //Debug command line option
		if (commandLine.hasOption("debug")) {
			System.out.println("SENT: " + command.toString());
		}

		// System.out.println(command.toString());
		return command;
	}
}
