
import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Map;


public class DNSRelayServer {
    public static final int port = 53;

    private static Map<String, String> domainIpMap;
    private static DatagramSocket socket;
    private static int d = 0;
    private static byte[] dnsaddr = new byte[4096];
    private static byte[] dnsfile = new byte[4096];

    static final Object LOCK_OBJ = new Object();

    static Map<String, String> getDomainIpMap() {
        return domainIpMap;
    }

    static DatagramSocket getSocket() {
        return socket;
    }

    public static String getDnsAddr() {
        return new String(dnsaddr);
    }

    public static int getDnsPort() {
        return port;
    }
    
    public static int getd() {		
    	return d+1;		
    }
    
    private static Map<String, String> domainIpMap(String filePath) throws IOException {
        File localTableFile = new File(filePath);
        Map<String, String> domainIpMap = new HashMap<>();
        try {
        BufferedReader br = new BufferedReader(new FileReader(localTableFile));
        String line;
        while ((line = br.readLine()) != null) {
            String[] contentList = line.split(" ");
            if (contentList.length < 2) {
                continue;
            }
            domainIpMap.put(contentList[1], contentList[0]);
            if(d==2) {
            	System.out.println(contentList[1]+" "+contentList[0]);
            }
        }
        br.close();
        System.out.println("OK!\n" + domainIpMap.size() + " names, occupy "  + localTableFile.length() + " bytes memory");
        } catch (IOException e) {
        	System.out.println("Read file error!\nExiting...");
        	System.exit(0);
        }
        return domainIpMap;
    }

    // DD, D=1, DD=2
    public static int DorDD(String D) {
        if(D.length() == 2) {
            return 1;
        }else if(D.length() == 3) {
            return 2;
        }else {
            System.out.println("Input error\nExiting...");
            System.exit(0);
            return -1;
        }
    }

    public static int judgeParaType(String p) {
        if(p.length()<=3) {
            return 0;
        }else if(p.contains(".txt")) {
            return 2;
        }else if(p.indexOf(".")!=-1){
            return 1;
        }else {
            System.out.println("Input error\nExiting...");
            System.exit(0);
    		return -1;
    	}
    }

    public static void main(String[] args) throws IOException {
    	System.out.println("Usage: dnsrelay [-d | -dd] [<dns-server>] [<db-file>]\n");
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
                    default:
                        System.out.println("Input error\nExiting...");
                        System.exit(0);
                    	break;
                }
                switch(judgeParaType(args[1])){
                    case 1:
                        dnsaddr=args[1].getBytes();
                        break;
                    case 2:
                        dnsfile=args[1].getBytes();
                        break;
                    default:
                        System.out.println("Input error\nExiting...");
                        System.exit(0);
                    	break;
                }
                break;
            case 3:
                d=DorDD(args[0]);
                dnsaddr=args[1].getBytes();
                dnsfile=args[2].getBytes();
                break;
            default:
                System.out.println("Input error\nExiting...");
                System.exit(0);
                break;
        }

        // if no filepath/filename entered, use default
        if(dnsfile[0]==0) {
            String dp =  "dnsrelay.txt";
            dnsfile=dp.getBytes();
        }
        if(dnsaddr[0]==0) {
            String da =  "202.106.0.20";
            dnsaddr=da.getBytes();
        }
        
    	System.out.println("Name Server " + new String(dnsaddr));
    	System.out.println("Debug level " + d);
        System.out.print("Bind UDP port "+ port +" ... ");
        
        try {
            socket = new DatagramSocket(getDnsPort());
            System.out.println("OK!");
        } catch (SocketException e) {
        	System.out.println("Bind port error!\nExiting...");
        	System.exit(0);
        }
        
        System.out.println("Try to load table \"" + new String(dnsfile) + "\" ... ");
        
        domainIpMap = domainIpMap(new String(dnsfile));
        
        byte[] data = new byte[1024];
        DatagramPacket packet = new DatagramPacket(data, data.length);

        while (true) {
            try {
                socket.receive(packet);
            } catch (IOException e) {
                e.printStackTrace();
            }
            DNSQuery queryParser = new DNSQuery(packet);
            queryParser.start();
        }
    }
}
