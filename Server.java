import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.text.DecimalFormat;
import java.util.concurrent.TimeUnit;
import java.nio.ByteBuffer;
import java.nio.*;

class UDPServer {
	public static void main(String args[]) throws Exception {
		DatagramSocket serverSocket = new DatagramSocket(9876);
		byte[] receiveData = new byte[512];
		byte[] sendData = new byte[512];
		int j=0;
		int port;
		boolean sofReceived=false;
		// boolean lengthReceived=false;
		

		System.out.println("Waiting for incoming connections");

		while(sofReceived==false) {
			DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);  
			serverSocket.receive(receivePacket);
			String sentence = new String( receivePacket.getData(),0,receivePacket.getLength(),"UTF-8");

			if (sentence.equals("Start of Frame")==true) {
				sofReceived=true;
				System.out.println("Start of Frame received");
				InetAddress IPAddress = receivePacket.getAddress();
				port = receivePacket.getPort();
				sendData = "SOF received".getBytes();
				DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
				serverSocket.send(sendPacket);

			}

		}
		while (true) {
			System.out.println("Waiting for message...");
			List<Integer> list=new LinkedList<>();
			List<Integer> covert=new LinkedList<>();
		// ricezione lunghezza
			receiveData=new byte[512];
			DatagramPacket receiveLength=new DatagramPacket(receiveData,receiveData.length);
			serverSocket.receive(receiveLength);
			ByteBuffer buffer=ByteBuffer.wrap(receiveData);
			buffer.order(ByteOrder.LITTLE_ENDIAN);
			int length=buffer.getInt();
			System.out.println("length= "+ length);

    // ricezione timing
			receiveData=new byte[512];
			DatagramPacket receiveTiming = new DatagramPacket(receiveData, receiveData.length);
			serverSocket.receive(receiveTiming);
			buffer=ByteBuffer.wrap(receiveData);
			buffer.order(ByteOrder.LITTLE_ENDIAN);
			double interval= buffer.getDouble();
			System.out.println("INTERVAL=" +interval);
            int zeros=0;
		// pacchetto di fine comunicazione o fornisco lunghezza messaggio?
			while(j<length) {
				byte[] single_bit=new byte[8];
				DatagramPacket receivePacket = new DatagramPacket(single_bit, single_bit.length);
				long init=System.nanoTime();
				serverSocket.receive(receivePacket);
				long end=System.nanoTime();
				long difference=end-init;
				double seconds = (double)difference / 1000000000;
				double diff=seconds-interval/2;
				single_bit =receivePacket.getData();
				if(j>=8 && j%8==0)
					System.out.println();

				buffer=ByteBuffer.wrap(single_bit);
				buffer.order(ByteOrder.LITTLE_ENDIAN);
				int current=buffer.getInt();
				list.add(current);
				System.out.println("RECEIVED: "+ current + " "+ new DecimalFormat("#.##########").format(seconds));
				if(diff>interval/2 && diff < interval) {
						if(diff>(interval/2+interval/20))
							diff=interval;
						else
							diff=interval/2;
					}
						
				if (diff>0){
					zeros=(int)(diff/interval);
					for (int z=0; z<zeros;z++)
						covert.add(0);
					covert.add(1);
					System.out.print("DIFF= "+diff);
				System.out.println(" zeros= "+ zeros);
				}
				j++;
			}
			String decoded=decode(list);
			String decodedCovert=decode(covert);
			System.out.println("DECODED: "+ decoded);
			System.out.println(covert.toString());
			System.out.println("Bit covert ricevuti= "+covert.size());
			System.out.println("COVERT: "+ decodedCovert);
			j=0;
		}

	}

	public static String decode(List<Integer> list) {
		String value="";
		for (int i: list)
			value+=i;
		String result= int2str(value);
		return result;

	}
	public static String int2str( String s ) { 
		String[] ss=new String[s.length()/8];
		for (int i=0; i<ss.length;i++) {
			ss[i]=s.substring(i*8,i*8+8);
		}
		StringBuilder sb = new StringBuilder();
		for ( int i = 0; i < ss.length; i++ ) { 
			sb.append( (char)Integer.parseInt( ss[i], 2 ) );                                                                                                                                                        
		}   
		return sb.toString();
	}
} 