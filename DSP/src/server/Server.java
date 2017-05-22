package server;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import model.*;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import org.apache.commons.cli.*;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import javax.management.Query;
import javax.net.ServerSocketFactory;
import javax.net.ssl.SSLServerSocketFactory;

import java.io.*;
import java.net.*;
import java.time.LocalDateTime;
import java.util.*;

import static model.Resp.RESPONSE;
import static server.ServerOptions.*;
import static java.lang.Integer.parseInt;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;

/**
 * 
 * @author haozhic1@student.unimelb.edu.au
 * @author mengyuz2@student.unimelb.edu.au
 * @author feifanl1@student.unimelb.edu.au
 * @author xiangl14@student.unimelb.edu.au
 * 
 */

//还没有测试secure链接
//subscribe，unsubscribe 没有实现
//query 还需要测试
public class Server extends Thread{

    private static Logger logger = Logger.getRootLogger();

    /**
     * setting server args 
     */
    private static ServerOptions serverOptions;
    /**
     * servers list 
     */
    private static List<HostInfo> serverRecords = new ArrayList<>();
    
    private static List<HostInfo> secureServerRecords = new ArrayList<>();
    /**
     * resource list record in server
     */
    private static Set<Resource> resourceSet = new HashSet<>();
    
    private static Set<Resource> SecureresourceSet = new HashSet<>();
    /**
     * record the connection info
     */
    private static Map<String, LocalDateTime> clientAccessHistory = new HashMap<>();
    
    

    public static void main(String[] args) {
    	
    	serverOptions = parseOptions(args);
        if (serverOptions.isDebug()){
            logger.setLevel(Level.DEBUG);
            logger.debug("debug test log");//for test purpose
            logger.error("error test log");//for test purpose
        }else
        {
        LoggerFormate loggers = new LoggerFormate();
		loggers.Logger(serverOptions.getHostname(), serverOptions.getPort(), serverOptions.getSport(),serverOptions.getSecret());
        
        }
        
        Thread unsecureServer = new Thread(()->HandleUnSecure());
        unsecureServer.start();
        
        Thread SecureServer = new Thread(()->HandleSecure());
        SecureServer.start();
        
        
        
        
    }
    
    
    public static void HandleUnSecure() 
    {
        

//        add local host to server records
        
        
        System.out.println("server waiting for connection: port "+serverOptions.getPort()+" host "+serverOptions.getHostname());
        
        
//        start timer thread to exchange server records periodically
        startExchangeTimer();
        
              

//        open server socket and accept each client
        try(ServerSocket serverSocket = ServerSocketFactory.getDefault().createServerSocket(serverOptions.getPort());
        	) {
            logger.debug("ip of server:"+serverSocket.getInetAddress().getHostAddress().toString());
            while(true){
                try {
                	serverRecords.add(new HostInfo(serverOptions.getHostname(), serverOptions.getPort()));
                    Socket connection = serverSocket.accept();
                    
                    try {
//                      check successive access from the same ip
                      Optional<Resp> respOptional = checkAccessIntervalFromSameIp(connection, serverOptions.getConnectionInterval());

//                      handle client command
                      DataInputStream in = new DataInputStream(connection.getInputStream());
                      DataOutputStream out = new DataOutputStream(connection.getOutputStream());

                      Gson gson = new Gson();
                      if (respOptional.isPresent()){//force to close socket
                          out.writeUTF(gson.toJson(respOptional.get()));
                          out.flush();
                          connection.close();
                      }
                      
                      
                      
                      String line=in.readUTF();
                      
                      Resp response = null;
                      if(line==null)
                      {
                      	
                      response = new RespError("missing massage");
                      out.writeUTF(gson.toJson(response));
                      out.flush();
                      }else{
                     
                      
                      
                      //JSONParser parser = new JSONParser();
                      //JSONObject cmd=(JSONObject) parser.parse(line);
                      //System.out.println(cmd.toJSONString());
                      JsonObject reqCmd = gson.fromJson(line, JsonObject.class);//readUTF may cause interrupted
                      JsonElement command = reqCmd.get("command");
                      //JsonObject reqCmd = gson.fromJson(line,JsonObject.class);
                      //JsonObject reqCmd = new JsonParser().parse(line).getAsJsonObject();
                      //String command= reqCmd.get("command").toString();
                      if (command == null){
                          response = new RespError("missing or incorrect type for command");
                          out.writeUTF(gson.toJson(response));
                          out.flush();
                      }else{
                      	//switch each command and do porcess
                          switch (command.getAsString()){
                              case "EXCHANGE":
                                  response = handleExchange(reqCmd,true);
                                  break;
                              case "FETCH":
                                  handleFetch(reqCmd, out);
                                  break;
                              case "PUBLISH":
                                  response = handlePublish(reqCmd,true);
                                  break;
                              case "QUERY":
                                  response = handleQuery(reqCmd,true);
                                  break;
                              case "REMOVE":
                                  response = handleRemove(reqCmd,true);
                                  break;
                              case "SHARE":
                                  response = handleShare(reqCmd,true);
                                  break;
                              default:
                                  response = new RespError("invalid command");
                          }
                          //response
                          if (!"FETCH".equals(command)){
                              out.writeUTF(gson.toJson(response));
                              out.flush();
                          }
                      }
                    } 
                  }catch (IOException e) {
                      logger.error(e);
                  } catch (Throwable t){
                      logger.error(t);
                  }
                    //new Thread(() -> serveClient(connection)).start();
                } catch (IOException e) {
                    logger.error("fail to connect client socket", e);
                }
            }
        } catch (IOException e) {
            logger.error("fail to create server socket", e);
            System.exit(-1);
        } catch (Throwable e)
        {
            logger.error(e);
        }
    }

    public static void HandleSecure() 
    {
        

//        add local host to server records
       
        
        System.out.println("server waiting for secure connection: port "+serverOptions.getSport()+" host "+serverOptions.getHostname());
        
        
//        start timer thread to exchange server records periodically
        
        startSecureExchangeTimer();
        
        
        

//        open server socket and accept each client
        try{System.setProperty("javax.net.ssl.keyStore","bin/Server.jks");
        
            System.setProperty("javax.net.ssl.trustkeyStore", "bin/Server.jks");
		//Password to access the private key from the keystore file
		    System.setProperty("javax.net.ssl.keyStorePassword","comp90015");

		// Enable debugging to view the handshake and communication which happens between the SSLClient and the SSLServer
		    System.setProperty("javax.net.debug","all");
		    
		    SSLServerSocketFactory sslserversocketfactory = (SSLServerSocketFactory) SSLServerSocketFactory
					.getDefault();
			SSLServerSocket sslserversocket = (SSLServerSocket) sslserversocketfactory.createServerSocket(serverOptions.getSport());
        	
            logger.debug("ip of server:"+sslserversocket.getInetAddress().getHostAddress().toString());
            while(true){
                try { 
                	secureServerRecords.add(new HostInfo(serverOptions.getHostname(), serverOptions.getSport()));
                    SSLSocket connection = (SSLSocket) sslserversocket.accept();
                    
                    try {
//                      check successive access from the same ip
                      Optional<Resp> respOptional = checkAccessIntervalFromSameIp(connection, serverOptions.getConnectionInterval());

//                      handle client command
                      DataInputStream in = new DataInputStream(connection.getInputStream());
                      DataOutputStream out = new DataOutputStream(connection.getOutputStream());

                      Gson gson = new Gson();
                      if (respOptional.isPresent()){//force to close socket
                          out.writeUTF(gson.toJson(respOptional.get()));
                          out.flush();
                          connection.close();
                      }
                      
                      
                      
                      String line=in.readUTF();
                      
                      Resp response = null;
                      if(line==null)
                      {
                      	
                      response = new RespError("missing massage");
                      out.writeUTF(gson.toJson(response));
                      out.flush();
                      }else{
                     
                      
                      
                      //JSONParser parser = new JSONParser();
                      //JSONObject cmd=(JSONObject) parser.parse(line);
                      //System.out.println(cmd.toJSONString());
                      JsonObject reqCmd = gson.fromJson(line, JsonObject.class);//readUTF may cause interrupted
                      JsonElement command = reqCmd.get("command");
                      //JsonObject reqCmd = gson.fromJson(line,JsonObject.class);
                      //JsonObject reqCmd = new JsonParser().parse(line).getAsJsonObject();
                      //String command= reqCmd.get("command").toString();
                      if (command == null){
                          response = new RespError("missing or incorrect type for command");
                          out.writeUTF(gson.toJson(response));
                          out.flush();
                      }else{
                      	//switch each command and do porcess
                          switch (command.getAsString()){
                              case "EXCHANGE":
                                  response = handleExchange(reqCmd,false);
                                  break;
                              case "FETCH":
                                  handleFetch(reqCmd, out);
                                  break;
                              case "PUBLISH":
                                  response = handlePublish(reqCmd,false);
                                  break;
                              case "QUERY":
                                  response = handleQuery(reqCmd,false);
                                  break;
                              case "REMOVE":
                                  response = handleRemove(reqCmd,false);
                                  break;
                              case "SHARE":
                                  response = handleShare(reqCmd,false);
                                  break;
                              default:
                                  response = new RespError("invalid command");
                          }
                          //response
                          if (!"FETCH".equals(command)){
                              out.writeUTF(gson.toJson(response));
                              out.flush();
                          }
                      }
                    } 
                  }catch (IOException e) {
                      logger.error(e);
                  } catch (Throwable t){
                      logger.error(t);
                  }
                } catch (IOException e) {
                    logger.error("fail to connect client socket", e);
                }
            }
        } catch (IOException e) {
            logger.error("fail to create server socket", e);
            System.exit(-1);
        } catch (Throwable e)
        {
            logger.error(e);
        }
    }
    
    
    /**private static void serveClient(Socket connection) {
        logger.debug("serverClient invoked");
//        avoid socket close
        try {
//            check successive access from the same ip
            Optional<Resp> respOptional = checkAccessIntervalFromSameIp(connection, serverOptions.getConnectionInterval());

//            handle client command
            DataInputStream in = new DataInputStream(connection.getInputStream());
            DataOutputStream out = new DataOutputStream(connection.getOutputStream());

            Gson gson = new Gson();
            if (respOptional.isPresent()){//force to close socket
                out.writeUTF(gson.toJson(respOptional.get()));
                out.flush();
                connection.close();
            }
            
            
            
            String line=in.readUTF();
            
            Resp response = null;
            if(line==null)
            {
            	
            response = new RespError("missing massage");
            out.writeUTF(gson.toJson(response));
            out.flush();
            }else{
           
            
            
            //JSONParser parser = new JSONParser();
            //JSONObject cmd=(JSONObject) parser.parse(line);
            //System.out.println(cmd.toJSONString());
            JsonObject reqCmd = gson.fromJson(line, JsonObject.class);//readUTF may cause interrupted
            JsonElement command = reqCmd.get("command");
            //JsonObject reqCmd = gson.fromJson(line,JsonObject.class);
            //JsonObject reqCmd = new JsonParser().parse(line).getAsJsonObject();
            //String command= reqCmd.get("command").toString();
            if (command == null){
                response = new RespError("missing or incorrect type for command");
                out.writeUTF(gson.toJson(response));
                out.flush();
            }else{
            	//switch each command and do porcess
                switch (command.getAsString()){
                    case "EXCHANGE":
                        response = handleExchange(reqCmd);
                        break;
                    case "FETCH":
                        handleFetch(reqCmd, out);
                        break;
                    case "PUBLISH":
                        response = handlePublish(reqCmd);
                        break;
                    case "QUERY":
                        response = handleQuery(reqCmd);
                        break;
                    case "REMOVE":
                        response = handleRemove(reqCmd);
                        break;
                    case "SHARE":
                        response = handleShare(reqCmd);
                        break;
                    default:
                        response = new RespError("invalid command");
                }
                //response
                if (!"FETCH".equals(command)){
                    out.writeUTF(gson.toJson(response));
                    out.flush();
                }
            }
          } 
        }catch (IOException e) {
            logger.error(e);
        } catch (Throwable t){
            logger.error(t);
        }
    }

    /**
     * This function is used for judging whether the fetched resource existing on server.
     * response the fetch info
     * @param reqCmd
     * @param out
     */
    private static void handleFetch(JsonObject reqCmd, DataOutputStream out) {
        File file = null;
        Gson gson = new Gson();
        JsonElement resourceTemplateEle = reqCmd.get("resourceTemplate");

        Resp resp;
        if (resourceTemplateEle == null){
            resp = new RespError("missing resourceTemplate");
        }else{
            Resource qTemplate = gson.fromJson(resourceTemplateEle, Resource.class);
            if (qTemplate.getChannel() == null || "".equals(qTemplate.getChannel())
                    || qTemplate.getUri() == null || !URI.create(qTemplate.getUri()).isAbsolute() || !qTemplate.getUri().startsWith("file://")
            ){
                resp = new RespError("invalid resourceTemplate");
            } else {
                ResourceForFetch rf = null;
                for (Resource res : resourceSet) {
                    if (res.getChannel().equals(qTemplate.getChannel()) && res.getUri().equals(qTemplate.getUri())){
                        rf = gson.fromJson(gson.toJson(res), ResourceForFetch.class);
                    }
                }
                if (rf == null){
                    resp = new RespError("invalid resourceTemplate");
                }else{
                    file = new File(rf.getUri());
                    rf.setResourceSize(file.length());
                    resp = new FetchSuccess(rf);
                }
            }
        }
        try {
            out.writeUTF(gson.toJson(resp));
        } catch (IOException e) {
            logger.error(e);
        }
//        write bytes to client
        if (resp instanceof FetchSuccess){//file exist
            try (FileInputStream fis = new FileInputStream(file); DataOutputStream dos = out){
                byte[] buffer = new byte[1024];
                int len;
                while ((len = fis.read(buffer)) != -1){
                    dos.write(buffer, 0, len);
                }
            }catch (IOException e){
                logger.error(e);
            }
        }
    }

    /**
     * This function is query resource from local or remote
     * @param reqCmd
     * @return
     * @throws IOException
     */
    private static Resp handleQuery(JsonObject reqCmd,boolean flag) throws IOException {
        Gson gson = new Gson();
        JsonElement resourceTemplateEle = reqCmd.get("resourceTemplate");

        Resp resp;
        if (resourceTemplateEle == null){
            resp = new RespError("missing resourceTemplate");
        }else{
            Resource qTemplate = gson.fromJson(resourceTemplateEle, Resource.class);

            Set<Resource> queryResults = queryLocal(qTemplate,flag);
            if (reqCmd.get("relay").getAsBoolean()){
//                query relay:
//                1. the owner and channel information in the original query are both set to "" in the forwarded query
//                2. relay field is set to false
                qTemplate.setOwner("");
                qTemplate.setChannel("");
                queryResults.addAll(queryRemote(new CmdQuery(false, qTemplate),flag));
            }

//            the server will never reveal the owner of a resource in a response
            queryResults.stream().filter(res -> notEmptyString(res.getOwner())).forEach(res -> res.setOwner("*"));

            resp = new QuerySuccess(queryResults,queryResults.size());
        }
        return resp;
    }

    /**
     * query resource from remote server info
     * @param cmdQuery
     * @return
     */
    private static Set<? extends Resource> queryRemote(CmdQuery cmdQuery,boolean flag) {
        Set<Resource> matchedResources = new HashSet<>();

        if(flag==true){
        for (HostInfo h : serverRecords) {
            if (h.getHostname().equals(serverOptions.getHostname()) && h.getPort() == serverOptions.getPort()){ //skip local
                continue;
            }
            Optional<Resp> resp = queryHost(h, cmdQuery,true);
            if (resp.isPresent()){//query normal
                if (resp.get() instanceof QuerySuccess){//force on success in query
                    matchedResources.addAll(((QuerySuccess) resp.get()).getResourceSet());
                }
            }
        }
    }else
       {
    	for (HostInfo h : secureServerRecords) {
            if (h.getHostname().equals(serverOptions.getHostname()) && h.getPort() == serverOptions.getSport()){ //skip local
                continue;
            }
            Optional<Resp> resp = queryHost(h, cmdQuery,false);
            if (resp.isPresent()){//query normal
                if (resp.get() instanceof QuerySuccess){//force on success in query
                    matchedResources.addAll(((QuerySuccess) resp.get()).getResourceSet());
                }
            }
        }
       }
        return matchedResources;
    }

    /**
     *  query host info
     * @param h
     * @param cmdQuery
     * @return
     */
    private static Optional<Resp> queryHost(HostInfo h, CmdQuery cmdQuery,boolean flag) {
        Gson gson = new Gson();
        Optional<Resp> resp = Optional.empty();
        if(flag==true){
        try (Socket connection = new Socket(h.getHostname(), h.getPort())) {
            DataInputStream in = new DataInputStream(connection.getInputStream());
            DataOutputStream out = new DataOutputStream(connection.getOutputStream());

            out.writeUTF(gson.toJson(cmdQuery));
            out.flush();

            while (in.available() > 0) {
                String jsonResult = in.readUTF();
                JsonObject jsonObject = gson.fromJson(jsonResult, JsonObject.class);
                if (RespSuccess.SUCCESS.equals(jsonObject.get(RESPONSE).getAsString())){
                    resp = Optional.of(gson.fromJson(jsonResult, QuerySuccess.class));
                }else{
                    resp = Optional.of(gson.fromJson(jsonObject, RespError.class));
                }
            }
        } catch (IOException e) {
            logger.error("IOException happened when querying host" + h.getHostname() + ":" + h.getPort(), e);
         }
        }
        else
        {
        	try{
                System.setProperty("javax.net.ssl.trustkeyStore", "bin/server.jks");
		//Password to access the private key from the keystore file
		        System.setProperty("javax.net.ssl.keyStorePassword","comp90015");

		// Enable debugging to view the handshake and communication which happens between the SSLClient and the SSLServer
		        System.setProperty("javax.net.debug","all");
		        
		        SSLSocketFactory sslsocketfactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
				SSLSocket sslsocket = (SSLSocket) sslsocketfactory.createSocket(h.getHostname(), h.getPort());
        		
				DataInputStream in = new DataInputStream(sslsocket.getInputStream());
	            DataOutputStream out = new DataOutputStream(sslsocket.getOutputStream());

	            out.writeUTF(gson.toJson(cmdQuery));
	            out.flush();

	            while (in.available() > 0) {
	                String jsonResult = in.readUTF();
	                JsonObject jsonObject = gson.fromJson(jsonResult, JsonObject.class);
	                if (RespSuccess.SUCCESS.equals(jsonObject.get(RESPONSE).getAsString())){
	                    resp = Optional.of(gson.fromJson(jsonResult, QuerySuccess.class));
	                }else{
	                    resp = Optional.of(gson.fromJson(jsonObject, RespError.class));
	                }
        		
        	 }
        	}catch (IOException e) {
                logger.error("IOException happened when querying host" + h.getHostname() + ":" + h.getPort(), e);
            }
        }
        return resp;
    }

    public static boolean notEmptyString(String s){
        return s != null && !"".equals(s.trim());
    }

 

    /**query resource in local erver
     * @param q 
     */
     
    private static Set<Resource> queryLocal(Resource q,boolean flag) {
        boolean channel=true, owner=true, tag=true, uri=true, nameAndDesc=true;
        Set<Resource> matchedResources = new HashSet<>();//prepare an empty set to store matched resources
        
        //check the temp with reource which record in local
        if(flag==true){
        for (Resource r : resourceSet) {
        	if(!q.getChannel().equals("")&&!r.getChannel().equals(q.getChannel()))
        	{
        		channel=false;
        	}else if(q.getChannel().equals("")&&!r.getChannel().equals(""))
        	{
        		channel=false;
        	}
            
        	if(!q.getOwner().equals("")&&!r.getOwner().equals(q.getOwner()))
        	{
        		owner=false;
        	}
            
        	if(!r.getTagList().containsAll(q.getTagList())&&!q.getTagList().equals(""))
        	{
        		tag=false;
        	}
            
        	if(!r.getUri().equals(q.getUri())&&!q.getUri().equals(""))
        	{
        		uri=false;
        	}
            
            nameAndDesc =
                    (r.getName().contains(q.getName())) ||
                    ( r.getDescription().contains(q.getDescription())
                    || q.getName().equals("")||q.getName().equals(""));

            if (channel && owner && tag && uri && nameAndDesc){
            	if(!r.getOwner().equals(""))
            	{
            	      r.setOwner("*");
            	}
                matchedResources.add(r);
            }
         }
        }
        
        else{
        	for (Resource r : SecureresourceSet) {
            	if(!q.getChannel().equals("")&&!r.getChannel().equals(q.getChannel()))
            	{
            		channel=false;
            	}else if(q.getChannel().equals("")&&!r.getChannel().equals(""))
            	{
            		channel=false;
            	}
                
            	if(!q.getOwner().equals("")&&!r.getOwner().equals(q.getOwner()))
            	{
            		owner=false;
            	}
                
            	if(!r.getTagList().containsAll(q.getTagList())&&!q.getTagList().equals(""))
            	{
            		tag=false;
            	}
                
            	if(!r.getUri().equals(q.getUri())&&!q.getUri().equals(""))
            	{
            		uri=false;
            	}
                
                nameAndDesc =
                        (r.getName().contains(q.getName())) ||
                        ( r.getDescription().contains(q.getDescription())
                        || q.getName().equals("")||q.getName().equals(""));

                if (channel && owner && tag && uri && nameAndDesc){
                	if(!r.getOwner().equals(""))
                	{
                	      r.setOwner("*");
                	}
                    matchedResources.add(r);
                }
             }
        	
        }

        return matchedResources;
    }

    /**
     * This function is to remove resource from server
     * @param reqCmd
     * @return
     * @throws IOException
     */
    private static Resp handleRemove(JsonObject reqCmd,boolean flag) throws IOException {
        Gson gson = new Gson();

        JsonElement resource = reqCmd.get("resource");
        if (resource == null){
            return new RespError("missing resource");
        }else{
            Resource r = gson.fromJson(resource, Resource.class);
            if ("*".equals(r.getOwner().trim())){
                return new RespError("invalid resource");
            }
            
            if(flag==true){
            if (!resourceSet.contains(r)) {//If the resource did not exist
                return new RespError("cannot remove resource");
            }

            Resource validResource = r.toValid();//silently remove illegal chars: '\0' and whitespaces
            resourceSet.remove(validResource);//remove existing Resource with the same primary key

            return new RespSuccess();
         }else
         {
        	 if (!SecureresourceSet.contains(r)) {//If the resource did not exist
                 return new RespError("cannot remove resource");
             }

             Resource validResource = r.toValid();//silently remove illegal chars: '\0' and whitespaces
             SecureresourceSet.remove(validResource);//remove existing Resource with the same primary key

             return new RespSuccess(); 
         }
        }
    }

    /**
     * this fuction is handling publish
     * @param reqCmd
     * @return
     */
    private static Resp handlePublish(JsonObject reqCmd,boolean flag) {
        Gson gson = new Gson();
        Resp resp;

        JsonElement resource = reqCmd.get("resource");
        
        if (resource == null){
            resp = new RespError("missing resource");
        }else{
            
            Resource r = gson.fromJson(resource.toString(), Resource.class);
            if (r.getUri() == null || !URI.create(r.getUri()).isAbsolute() || r.getUri().startsWith("file://") //The URI must be present, must be absolute and cannot be a file scheme
                    || "*".equals(r.getOwner().trim())
            ) {
                return new RespError("invalid resource");
            }
            
            if(flag==true){
            if (resourceSet.contains(r)){
            	
            	Resource validResource = r.toValid();//silently remove illegal chars: '\0' and whitespaces
                resourceSet.add(validResource);//overwriting existing Resource with the same primary key
                
                return new RespSuccess();}
            else{
            r.setEzServer(serverOptions.getHostname() + ":" + serverOptions.getPort());
            Resource validResource = r.toValid();//silently remove illegal chars: '\0' and whitespaces
            resourceSet.add(validResource);//overwriting existing Resource with the same primary key
            System.out.println("unsecure"+resourceSet);

            return new RespSuccess();
             }
            }
            else{
            	if (SecureresourceSet.contains(r)){
            		
            		Resource validResource = r.toValid();//silently remove illegal chars: '\0' and whitespaces
                    resourceSet.add(validResource);//overwriting existing Resource with the same primary key
                    
                    return new RespSuccess();}
                else{
                r.setEzServer(serverOptions.getHostname() + ":" + serverOptions.getSport());
                Resource validResource = r.toValid();//silently remove illegal chars: '\0' and whitespaces
                SecureresourceSet.add(validResource);//overwriting existing Resource with the same primary key
                System.out.println("secure"+SecureresourceSet);

                return new RespSuccess();
                 }
            	
            }
            
        }
		return resp;
        
       
    }

    /**
     * This function used to share info
     * @param reqCmd
     * @return
     */
    private static Resp handleShare(JsonObject reqCmd,boolean flag) {
        Gson gson = new Gson();
        Resp resp;

        JsonElement resource = reqCmd.get("resource");
        JsonElement secret = reqCmd.get("secret");

        if (resource == null || secret == null){
            resp = new RespError("missing resource or secret");
        }else{
            if (!serverOptions.getSecret().equals(secret.getAsString())){
                return new RespError("incorrect secret");
            }

            Resource r = gson.fromJson(resource, Resource.class);
            try {
                if (r.getUri() == null || !new URI(r.getUri()).isAbsolute() || !r.getUri().startsWith("file://")
                        
                        || "*".equals(r.getOwner().trim())
                ){
                    return new RespError("invalid resource");
                }
            } catch (URISyntaxException e) {
                return new RespError("invalid resource");
            }
            
            if(flag==true){
            if (resourceSet.contains(r)){
            	
            	Resource validResource = r.toValid();//silently remove illegal chars: '\0' and whitespaces
                resourceSet.add(validResource);//overwriting existing Resource with the same primary key
                
                return new RespSuccess();
            }

            r.setEzServer(serverOptions.getHostname() + ":" + serverOptions.getPort());
            Resource validResource = r.toValid();//silently remove illegal chars: '\0' and whitespaces
            resourceSet.add(validResource);//overwriting existing Resource with the same primary key
            System.out.println(r);

            resp = new RespSuccess();
            }
            
            else{
            	if (SecureresourceSet.contains(r)){
            		Resource validResource = r.toValid();//silently remove illegal chars: '\0' and whitespaces
                    resourceSet.add(validResource);//overwriting existing Resource with the same primary key
                    
                    return new RespSuccess();
                }

                r.setEzServer(serverOptions.getHostname() + ":" + serverOptions.getSport());
                Resource validResource = r.toValid();//silently remove illegal chars: '\0' and whitespaces
                SecureresourceSet.add(validResource);//overwriting existing Resource with the same primary key

                resp = new RespSuccess();
            }
        }
        return resp;
    }

    /**
     * This function is used to tell server about list of servers to exchange 
     * @param reqCmd
     * @return
     * @throws IOException
     */
    private static Resp handleExchange(JsonObject reqCmd,boolean flag) throws IOException {
        Gson gson = new Gson();
        Resp resp;

        JsonElement serverList = reqCmd.get("serverList");
        if (serverList == null){
            resp = new RespError("missing server list");
        }else {
            JsonArray serverArray = serverList.getAsJsonArray();
            if (serverArray.size() == 0){
                resp = new RespError("missing or invalid server list");
            }else{
                for (JsonElement e : serverArray) {
                    HostInfo h = gson.fromJson(e, HostInfo.class);
                    if ("localhost".equals(h.getHostname()) || "127.0.0.1".equals(h.getHostname())){
                        h.setHostname(serverOptions.getHostname());
                    }
                    if(flag==true){
                    if (!serverRecords.contains(h)){
                        serverRecords.add(h);//add non-duplicate server records
                      }
                    }else
                       {
                    	if (!secureServerRecords.contains(h)){
                            secureServerRecords.add(h);//add non-duplicate server records
                       }
                    }
                    	
                }
                resp = new RespSuccess();
            }
        }
        return resp;
    }

    /**
     * 
     * @param clientSocket
     * @param intervalInSeconds
     * @return
     * @throws IOException
     */
    private static Optional<Resp> checkAccessIntervalFromSameIp(Socket clientSocket, int intervalInSeconds) throws IOException {
        String clientIp = clientSocket.getInetAddress().getHostAddress();
        LocalDateTime lastAccess = clientAccessHistory.get(clientIp);
        LocalDateTime currentAccess = LocalDateTime.now();
        clientAccessHistory.put(clientIp, currentAccess);//record the access time
        if (lastAccess != null //if the server has been interviewed
                && lastAccess.plusSeconds(intervalInSeconds).isAfter(currentAccess)) {//server be interviewed more times in 1s
            return Optional.of(new RespError("client socket was closed due to frequent access within " + intervalInSeconds + " seconds"));
        }
        return Optional.empty();
    }

    
    
    /**
     * server contacts a randomly selected server from server records and apply exchange
     */
    private static void startExchangeTimer() {
        long delay = 0L;
        long period = serverOptions.getExchangeInterval() * 1000;
        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (serverRecords.size() > 0){
                    int randomIndex = (int)(Math.random() * serverRecords.size());
                    HostInfo randomSelectedHost = serverRecords.get(randomIndex);

                    if (serverOptions.getHostname().equals(randomSelectedHost.getHostname())
                            && serverOptions.getPort() == randomSelectedHost.getPort()){//if randomly select the present server
                        logger.info(Thread.currentThread().getName() + " - current server selected as target exchange server, ignored...");
                    }else{
                        Optional<Resp> respInfo = exchangeWithHost(randomSelectedHost);
                        respInfo.ifPresent(r -> {
                            if ("error".equals(r.getResponse())){
                                serverRecords.remove(randomIndex);
                            }
                        });
                    }
                }
            }
        }, delay, period);
    }
    
    private static void startSecureExchangeTimer() {
        long delay = 0L;
        long period = serverOptions.getExchangeInterval() * 1000;
        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (secureServerRecords.size() > 0){
                    int randomIndex = (int)(Math.random() * secureServerRecords.size());
                    HostInfo randomSelectedHost = secureServerRecords.get(randomIndex);

                    if (serverOptions.getHostname().equals(randomSelectedHost.getHostname())
                            && serverOptions.getSport() == randomSelectedHost.getPort()){//if randomly select the present server
                        logger.info(Thread.currentThread().getName() + " - current server selected as target exchange server, ignored...");
                    }else{
                        Optional<Resp> respInfo = exchangeWithHost(randomSelectedHost);
                        respInfo.ifPresent(r -> {
                            if ("error".equals(r.getResponse())){
                            	secureServerRecords.remove(randomIndex);
                            }
                        });
                    }
                }
            }
        }, delay, period);
    }

    
    /**
     * 
     * @param host
     * @return
     */
    
    //这里还需要修改
    private static Optional<Resp> exchangeWithHost(HostInfo host) {
        Gson gson = new Gson();
        Optional<Resp> resp = Optional.empty();

        try(Socket s = new Socket(host.getHostname(), host.getPort())) {
            DataInputStream in = new DataInputStream(s.getInputStream());
            DataOutputStream out = new DataOutputStream(s.getOutputStream());

            CmdExchange cmdExchange = new CmdExchange(serverRecords);
            out.writeUTF(gson.toJson(cmdExchange));
            out.flush();

            while (in.available() > 0){
                resp = Optional.of(gson.fromJson(in.readUTF(), Resp.class));
            }
        } catch (IOException e) {
            logger.error(e);
        }

        return resp;
    }

    private static ServerOptions parseOptions(String[] args) {
        /*
            -advertisedhostname <arg>
                advertised hostname
            -connectionintervallimit <arg>
                connection interval limit in seconds
            -exchangeinterval <arg>
                exchange interval in seconds
            -port <arg>
                server port, an integer
            -secret <arg>
                secret
            -debug
                print debug information
        */
        Options options = new Options()
//        options that needs an argument
                .addOption(HOSTNAME, true, "advertised hostname")
                .addOption(INTERVAL_CONN, true, "connection interval limit in seconds")
                .addOption(INTERVAL_EX, true, "exchange interval in seconds")
                .addOption(PORT, true, "server port, an integer")
                .addOption(SPORT,true,"secure server pro, an integer")
                .addOption(SECRET, true, "secret")
//        options that do not need an argument
                .addOption(DEBUG, false, "print debug information");

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = null;
        HelpFormatter formatter = new HelpFormatter();
        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            logger.error("arguments parsing failed");
            formatter.printHelp("commands", options);
            System.exit(-1);
        }
        ServerOptions serverOptions = null;
        try {
            serverOptions = new ServerOptions(
                    cmd.getOptionValue(HOSTNAME, "localhost"),//The default advertised host name will be the operating system supplied hostname.
                    parseInt(cmd.getOptionValue(INTERVAL_CONN, "1")),
                    parseInt(cmd.getOptionValue(INTERVAL_EX, "600")),//The default exchange interval will be 10 minutes (600 seconds).
                    parseInt(cmd.getOptionValue(PORT, "3780")),
                    parseInt(cmd.getOptionValue(SPORT, "3781")),
                    cmd.getOptionValue(SECRET, randomAlphanumeric(26)),//The default secret will be a large random string
                    cmd.hasOption(DEBUG)
            );
        } catch (NumberFormatException e) {
            logger.error("illegal number format");
            formatter.printHelp("commands", options);
        }
        if (serverOptions == null) {
            System.exit(-1);
        }
        return serverOptions;
    }
}
