
import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Map;


public class DNSRelayServer {
    private static final int PORT = 53;
    private static Map<String, String> domainIp;
    private static DatagramSocket socket;
    // identify -d -dd or null
    private static int d = 0;
    private static byte[] dnsaddr = new byte[4096];
    private static byte[] dnsfile = new byte[4096];

    static Map<String, String> getDomainIp() {
        return domainIp;
    }

    static DatagramSocket getSocket() {
        return socket;
    }

    public static String getDnsAddr() {
        return new String(dnsaddr);
    }

    public static int getDnsPort() {
        return PORT;
    }
    
    public static int getd() {		
    	return d+1;		
    }

    /**
     * @Description: Get HashMap of address and ip in the file
     * @param path
     */
    private static void domainIpMap(String path) {
        File txt = new File(path);
        domainIp = new HashMap<>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(txt));
            String line;
            while ((line = br.readLine()) != null) {
                String[] list = line.split(" ");
                if (list.length < 2) {
                    continue;
                }
                //Inserts the specified key/value pair into the HashMap
                //hashmap.put£¨K key£¬V value£©
                domainIp.put(list[1], list[0]);
            if(d==2) {
                System.out.println(list[1]+" ip:"+list[0]);
            }
        }
        br.close();
        System.out.println("OK!\n" + domainIp.size() + " names, occupy "  + txt.length() + " bytes memory");
        } catch (IOException e) {
        	System.out.println("Read file error!\nExiting...");
        	System.exit(0);
        }
    }

    /**
     * @Description: Determine Debugging Level, D=1, DD=2
     * @param D
     */
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

    /**
     * @Description: Determine the type of input.
     * @param type
     */
    public static int judgeParaType(String type) {
        if(type.length()<=3) {
            return 0;
        }else if(type.contains(".txt")) {
            return 2;
        }else if(type.contains(".")){
            return 1;
        }else {
            System.out.println("Input error\nExiting...");
            System.exit(0);
    		return -1;
    	}
    }

    /**
     * @Description: Main function
     * @param args
     */
    public static void main(String[] args) {
    	System.out.println("Usage: dnsrelay [-d | -dd] [<dns-server>] [<db-file>]\n");
    	//Determine the input
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
                    default:
                        System.out.println("Input error\nExiting...");
                        System.exit(0);
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
            String dp =  "C:\\Users\\yao\\Desktop\\dnsrelay-master\\dnsrelay-master\\dnsrelay.txt";
            dnsfile=dp.getBytes();
        }
        if(dnsaddr[0]==0) {
            String da =  "202.106.0.20";
            dnsaddr=da.getBytes();
        }
        
    	System.out.println("Name Server " + new String(dnsaddr));
    	System.out.println("Debug level " + d);
        System.out.print("Bind UDP port "+ PORT +" ... ");
        
        try {
            //Create an instance of DatagramSocket and bind the object to
            // the native default IP address 127.0.0.1 and the specified port 53
            socket = new DatagramSocket(getDnsPort());
            System.out.println("OK!");
        } catch (SocketException e) {
            //Port 53 occupied
        	System.out.println("Bind port error!\nExiting...");
        	System.exit(0);
        }

        //Read the file
        System.out.println("Try to load table \"" + new String(dnsfile) + "\" ... ");
        domainIpMap(new String(dnsfile));

        //Creates a DatagramPacket object that receives data from the DatagramSocket
        byte[] data = new byte[1024];
        DatagramPacket packet = new DatagramPacket(data, data.length);

        while (true) {
            try {
                //Receive data
                socket.receive(packet);
            } catch (IOException e) {
                e.printStackTrace();
            }
            DNSQuery query = new DNSQuery(packet);
            query.start();
        }
    }
}