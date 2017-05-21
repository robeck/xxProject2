package clients;

import java.io.DataInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;

import org.apache.commons.cli.CommandLine;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


/**
 * 
 * @author mengyuz2@student.unimelb.edu.au
 * @author haozhic1@student.unimelb.edu.au
 * @author feifanl1@student.unimelb.edu.au
 * @author xiangl14@student.unimelb.edu.au
 *
 */
public class Download {
	private static int count = 1;

	public static void DownloadFile(DataInputStream input,CommandLine commandLine) throws InterruptedException{
		try {
			
			//read data
			Thread.sleep(1000);
			String result = "";
//			while(input.available()>0){
				String resultPart = input.readUTF();
				result += resultPart;
//				System.out.println("test"+result);
//				Thread.sleep(2000);
//			}

			

			String[] dataReceived = result.split("}");

			for(int i = 0; i<dataReceived.length;i++){
				dataReceived[i] = dataReceived[i] + "}";
//				System.out.println(dataReceived[i]);		
			}

			JSONParser parser = new JSONParser();

		

			//files input
			if( dataReceived.length >= 2) {
				JSONObject resource =(JSONObject) parser.parse(dataReceived[1]);
				String fileName;
				if(! resource.get("name").equals("")){
				    fileName = "client_files/"+resource.get("name");
				}else{
					fileName = "client_files/" + String.valueOf(count);
					count ++;
				}
				// Create a RandomAccessFile to read and write the output file.
				RandomAccessFile downloadingFile = new RandomAccessFile(fileName, "rw");
				long fileSizeRemaining = (Long) resource.get("resultSize");
				int chunkSize = setChunkSize(fileSizeRemaining);						
							// Represents the receiving buffer
				byte[] receiveBuffer = new byte[chunkSize];			
							// Variable used to read if there are remaining size left to read.
				int num;
//				System.out.println("Downloading "+fileName+" of size "+fileSizeRemaining);
				while((num=input.read(receiveBuffer))>0){
								// Write the received bytes into the RandomAccessFile
					downloadingFile.write(Arrays.copyOf(receiveBuffer, num));
								// Reduce the file size left to read..
					fileSizeRemaining = fileSizeRemaining-num;		
								// Set the chunkSize again
					chunkSize = setChunkSize(fileSizeRemaining);
					receiveBuffer = new byte[chunkSize];
								// If you're done then break
					if(fileSizeRemaining==0){
						Thread.sleep(2000);
						resultPart = input.readUTF();
						result += resultPart;
						break;
					}
				}
//				System.out.println("File received!");
				downloadingFile.close();
				
			
			//debug option
			
		}if(commandLine.hasOption("debug")){
				System.out.println("RECEIVED: "+result);
			}
		
		} catch (FileNotFoundException e) {
            System.out.println("please try again.");
            System.exit(1);
		} catch (IOException e) {
            System.out.println("connection fail!");
            e.printStackTrace();
//            System.exit(1);
		} catch (ParseException e) {
            System.out.println("please try again.");
            System.exit(1);
		}
	}
	
		
	
        public static int setChunkSize(long fileSizeRemaining){
        	// Determine the chunkSize
        	int chunkSize=1024*1024;
        		
        	// If the file size remaining is less than the chunk size
        	// then set the chunk size to be equal to the file size.
        	if(fileSizeRemaining<chunkSize){
      			chunkSize=(int) fileSizeRemaining;
        	}
        		
        	return chunkSize;
        }
}
