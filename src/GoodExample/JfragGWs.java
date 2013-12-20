package GoodExample;


import java.io.*;
import java.net.*;
import java.security.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import sun.misc.BASE64Encoder;

/**
 * Title:        Sample Server
 * Description:  This utility will accept input from a socket, posting back to the socket before closing the link.
 * It is intended as a template for coders to base servers on. Please report bugs to brad at kieser.net
 * Copyright:    Copyright (c) 2002
 * Company:      Kieser.net
 * @author B. Kieser
 * @version 1.0
 */

public class JfragGWs {
  public static int NumberOfClientsCreates;
  public static ArrayList<doComms> list = new ArrayList<doComms>();
  private static int port=1337, maxConnections=0;
  public static void SendToAll(String Message,int whoami) throws IOException{
	  for (int i = 0; i < list.size(); i++){
	     if (i != whoami){
		  list.get(i).sendMessage(Message);  }
		  
	  }
	}
  public static void main(String[] args) {
	  System.out.println("This shits running on "+ port);
	  int i=0;
    try{
      ServerSocket listener = new ServerSocket(port);
      Socket server;
      
      while((i++ < maxConnections) || (maxConnections == 0)){
        doComms connection;

        server = listener.accept();
        doComms conn_c= new doComms(server);
        Thread t = new Thread(conn_c);
        t.start();
      }
    } catch (IOException ioe) {
      System.out.println("IOException on socket listen: " + ioe);
      ioe.printStackTrace();
    }
  }

}
class threadtwo implements Runnable {

	@Override
	public void run() {
		// TODO Auto-generated method stub
		
	}}
class doComms implements Runnable {
	private int whoami = JfragGWs.NumberOfClientsCreates;
    
	private int MASK_SIZE = 4;
	private int SINGLE_FRAME_UNMASKED = 0x81;
    private Socket server;
    PrintStream out = null;
    private String line,input;
    doComms(Socket server) {
      this.server=server;
      
    }
    public void sendMessage2(byte[] msg) throws IOException {
        System.out.println("Sending to client");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        BufferedOutputStream os = new BufferedOutputStream(server.getOutputStream());
        baos.write(SINGLE_FRAME_UNMASKED);
        
        if (msg.length < 126){
        baos.write(msg.length);}
       
        
        if (msg.length > 125 && msg.length < 256){
        baos.write(126);
        baos.write(0);
        baos.write(msg.length);	
}
        if (msg.length > 255){
        baos.write(126);
        baos.write(msg.length/256);
        baos.write(msg.length%256);
        }
        baos.write(msg);
        
       
        baos.flush();
        baos.close();
        //convertAndPrint(baos.toByteArray());
        os.write(baos.toByteArray(), 0, baos.size());
        os.flush();
        }
    private void convertAndPrint(byte[] bytes) {
    StringBuilder sb = new StringBuilder();
    for (byte b : bytes) {
        sb.append(String.format("%02X ", b));
    }
    System.out.println(sb.toString());
    }
    private boolean handshake() throws IOException {
        PrintWriter out = new PrintWriter(server.getOutputStream());
        BufferedReader in = new BufferedReader(new InputStreamReader(server.getInputStream()));

        HashMap<String, String> keys = new HashMap<>();
        String str;
        //Reading client handshake
        while (!(str = in.readLine()).equals("")) {
            String[] s = str.split(": ");
            System.out.println();
            System.out.println(str);
            if (s.length == 2) {
            keys.put(s[0], s[1]);
            }
        }
        //Do what you want with the keys here, we will just use "Sec-WebSocket-Key"

        String hash;
        try {
            hash = new BASE64Encoder().encode(MessageDigest.getInstance("SHA-1").digest((keys.get("Sec-WebSocket-Key") + "258EAFA5-E914-47DA-95CA-C5AB0DC85B11").getBytes()));
        } catch (NoSuchAlgorithmException ex) {
            ex.printStackTrace();
            return false;
        }

        //Write handshake response
        out.write("HTTP/1.1 101 Switching Protocols\r\n"
            + "Upgrade: websocket\r\n"
            + "Connection: Upgrade\r\n"
            + "Sec-WebSocket-Accept: " + hash + "\r\n"
             + "Origin: http://face2fame.com\r\n"
            + "\r\n");
           
        out.flush();

        return true;
        }
 public void sendMessage(String Message) throws IOException{
	  out = new PrintStream(server.getOutputStream());
	//	out.println(Message);
	  sendMessage2(Message.getBytes());
	
 }
 @SuppressWarnings("unused")
public String reiceveMessage() throws IOException {
	    String EasyBytes = null;
	    byte[] buf = readBytes(2); // our initial header
	   
	    convertAndPrint(buf);
	    //System.exit(0);
	    EasyBytes = (String.format("%02X ", buf[1]));
	    int payloadadder = 0;
	    if (EasyBytes.contains("FE")){ // Indicates extended message
	    	byte[] buf2 = readBytes(1);
	    	int a = (buf2[0] & 0xff) +1; // if byte is zero there is one extra fragment so add 1!
	    	System.out.println("Hex First Digit" + a);
	    	payloadadder = 2; // account for original header size
	    	byte[] adder = null;
	    	//String MagnificentString = "";
	    	for (int x = 0; x < a; x++){
	    		if(x==0){
	    		adder = readBytes(1);
	    		//MagnificentString += String.format("%02X ", adder[0]);
	    		payloadadder += ((adder[0] & 0xFF) - 0x80);}
	    		if(x>=1){
	    		//payloadadder = 	(buf[1] & 0xFF) + (adder[0] & 0xFF);
	    			payloadadder = (Integer.parseInt((String.format("%02X", buf2[0]) + String.format("%02X", adder[0])), 16)) - 126;
	    		}
	    		//else{
	    	    	//payloadadder = (Integer.parseInt((String.format("%02X", buf2[0]) + String.format("%02X", adder[0])), 16));
	    	    	//System.out.println("PROBLEM" + String.format("%02X", buf2[0]) + String.format("%02X", adder[0]));
	    	    	
	    	  // }
	    	   
	    	    
	    	}
	    	System.out.println("Overflow in byte/s " + payloadadder);
	    	//System.out.println("Our Hex String " + MagnificentString);
	    	//System.exit(0);
	    }
	    //convertAndPrint(buf);
	    //dont use this byte[] buf2 = readBytes(4);
	    
	    System.out.println("Headers:");
	   
	    //convertAndPrint(buf2);// Check out the byte sizes
	    int opcode = buf[0] & 0x0F;
	    if (1==2) {
	        //Client want to close connection!
	        //System.out.println("Client closed!");
	        //server.close();
	        //System.exit(0);
	        return null;
	    } else {
	    	int payloadSize = 0;
	    	if (payloadadder <= 0){
	     payloadSize = getSizeOfPayload(buf[1]);}
	    	else {
	    		payloadSize = getSizeOfPayload(buf[1]) + payloadadder;
	    	}
	    //	if (extendedsize>=126){   
	    	//payloadSize = extendedsize;}
	        System.out.println("Payloadsize: " + payloadSize);
	        buf = readBytes(MASK_SIZE + payloadSize);
	        System.out.println("Payload:");
	        convertAndPrint(buf);
	        buf = unMask(Arrays.copyOfRange(buf, 0, 4), Arrays.copyOfRange(buf, 4, buf.length));
	        
	        String message = new String(buf);
	        
	        return message;
	    }
	    }
 private int getSizeOfPayload(byte b) {
	    //Must subtract 0x80 from masked frames
	    	
	    int a = b & 0xff;
	    //System.out.println("PAYLOAD SIZE INT" + a);
	    return ((b & 0xFF) - 0x80);
	    }

	    private byte[] unMask(byte[] mask, byte[] data) {
	    for (int i = 0; i < data.length; i++) {
	        data[i] = (byte) (data[i] ^ mask[i % mask.length]);
	    }
	    return data;
	    }
	    private boolean convertAndPrintHeader(byte[] bytes) {
	       StringBuilder sb = new StringBuilder();
	       String CaryOverDetection = new String();
	       // We must test byte 2 specifically for this. In the next step we add length bytes perhaps?
	       //for(int i = 0; i < bytes.length; i++) {
	    	   //}
	        for (byte b : bytes) {
	        	CaryOverDetection = (String.format("%02X ", b));
	        	if (CaryOverDetection.contains("FE")){
	        	
	        		return false;
	        	}
	            sb.append(String.format("%02X ", b));
	        }
	        System.out.println(sb.toString());
			return true;
			
	        }
 private byte[] readBytes(int numOfBytes) throws IOException {
 byte[] b = new byte[numOfBytes];
 server.getInputStream().read(b);
 return b;
 }
    public void run () {
    	JfragGWs.NumberOfClientsCreates ++;
    	try {
			if (handshake()){System.out.println("This is a web socket Server");}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}    	
    	JfragGWs.list.add(this);
      input="";

      try {
        DataInputStream in = new DataInputStream (server.getInputStream());
      //  while((line = in.readLine()) != null && !line.equals(".")) {
        	
        	// If post handshake
        	//
       // }
        boolean meh = true;
        String Message = null;
while(meh){
	Message = reiceveMessage();
	System.out.println("Recieved from client: " + Message);
	JfragGWs.SendToAll(Message,whoami); // that are not me
	
}
       

        server.close();
      } catch (IOException ioe) {
        System.out.println("IOException on socket listen: " + ioe);
        ioe.printStackTrace();
      }
    }
}
