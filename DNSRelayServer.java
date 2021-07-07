package com.kngxscn.dnsrelay;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class DNSRelayServer {
    private static Map<String, String> domainIpMap;
    private static DatagramSocket socket;

    // identify -d -dd or null
    private static int d = 0;
    private static byte[] dnsaddr = new byte[4096];
    private static byte[] dnsfile = new byte[4096];
    //
    
    static final Object lockObj = new Object();

    static Map<String, String> getDomainIpMap() {
        return domainIpMap;
    }

    static DatagramSocket getSocket() {
        return socket;
    }
    
    public static String getDnsAddr() {
    	return new String(dnsaddr);
    }
    
    public static int getd() {
    	return d+1;
    }
    

    private static Map<String, String> generateDomainIpMap(String filePath) {
        // 读取本地域名-IP映射文件的内容
        File localTableFile = new File(filePath);
        Map<String, String> domainIpMap = new HashMap<String, String>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(localTableFile));
            String line = null;
            while ((line = br.readLine()) != null) {
                String[] contentList = line.split(" ");
                if (contentList.length < 2) {
                    continue;
                }
                domainIpMap.put(contentList[1], contentList[0]);
            }
            br.close();
            System.out.println("OK!\n" + domainIpMap.size() + " names, occupy "  + localTableFile.length() + " bytes memory");
        } catch (IOException e) {
        	System.out.println("Read file error!");
        }
        return domainIpMap;
    }

    
    //
    
    // 判断D还是DD, D=1, DD=2
    public static int DorDD(String D) {
    	if(D.length() == 2) {
    		return 1;
    	}else if(D.length() == 3) {
    		return 2;
    	}else {
    		return 0;
    	}
    }
    
    public static int judgeParaType(String p) {
    	if(p.length()<=3) {
    		return 0;
    	}else if(p.indexOf(".txt")!=-1) {
    		return 2;
    	}else if(p.indexOf(".")!=-1){
    		return 1;
    	}else {
    		return -1;
    	}
    }
    
    //
    
    
    public static void main(String[] args ) {
    	
    	//
    	switch(args.length){
    		case 0:
    			break;
    		case 1:
    			switch(judgeParaType(args[0])){
    				case 0:
    					d = DorDD(args[0]);
    					break;
    				case 1:
    					dnsaddr=args[0].getBytes();
    					break;
    				case 2:
    					dnsfile=args[0].getBytes();
    					break;
    			}
    			break;
    		case 2:
    			switch(judgeParaType(args[0])){
					case 0:
						d = DorDD(args[0]);
						break;
					case 1:
						dnsaddr=args[0].getBytes();
						break;
    			}
    			switch(judgeParaType(args[1])){
					case 1:
						dnsaddr=args[1].getBytes();
						break;
					case 2:
						dnsfile=args[1].getBytes();
						break;
    			}
    			break;
    		case 3:
    			d=DorDD(args[0]);
    			dnsaddr=args[1].getBytes();
    			dnsfile=args[2].getBytes();
    			break;
    		default:
    			System.out.println("输入错误");
    			break;
    	}
    	
    	// if no filepath entered, use default path
    	if(dnsfile[0]==0) {
    		String dp =  "dnsrelay.txt";
    		dnsfile=dp.getBytes();
    	}
    	
    	if(dnsaddr[0]==0) {
    		String da =  "202.106.0.20";
    		dnsaddr=da.getBytes();
    	}
    	
    	
    	System.out.println("Usage: dnsrelay [-d | -dd] [<dns-server>] [<db-file>]");
    	System.out.println("Name Server " + new String(dnsaddr));
    	System.out.println("Debug level " + d);
        System.out.print("Bind UDP port 53 ... ");
        try {
            socket = new DatagramSocket(53);
            System.out.println("OK!");
        } catch (SocketException e) {
        	System.out.println("Bind port error!");
        }
        
        System.out.print("Try to load table " + new String(dnsfile) + " ... ");
        domainIpMap = generateDomainIpMap(new String(dnsfile)); // change
        
        
        
        byte[] data = new byte[1024];
        DatagramPacket packet = new DatagramPacket(data, data.length);

        ExecutorService servicePool = Executors.newFixedThreadPool(10);  // 容纳10个线程的线程池
        while (true) {
            try {
                socket.receive(packet);
            } catch (IOException e) {
                e.printStackTrace();
            }
            servicePool.execute(new QueryParser(packet));
        }
    }
}
