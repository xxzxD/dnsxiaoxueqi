
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
    // identify -d -dd or null
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
    private static Map<String, String> domainIpMap(String filePath) throws IOException {
        // 读取本地域名-IP映射文件的内容
        File localTableFile = new File(filePath);
        Map<String, String> domainIpMap = new HashMap<>();
        BufferedReader br = new BufferedReader(new FileReader(localTableFile));
        String line;
        while ((line = br.readLine()) != null) {
            String[] contentList = line.split(" ");
            if (contentList.length < 2) {
                continue;
            }
            domainIpMap.put(contentList[1], contentList[0]);
        }
        br.close();
        return domainIpMap;
    }

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
        }else if(p.contains(".txt")) {
            return 2;
        }else {
            return 1;
        }
    }

    public static void main(String[] args ) throws IOException {

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
            String dp =  "C:\\Users\\yao\\Desktop\\dnsrelay-master\\dnsrelay-master\\dnsrelay.txt";
            dnsfile=dp.getBytes();
        }

        if(dnsaddr[0]==0) {
            String da =  "202.106.0.20";
            dnsaddr=da.getBytes();
        }
        // change
        domainIpMap = domainIpMap(new String(dnsfile));
        System.out.println("本地域名-IP映射文件读取完成。一共" + domainIpMap.size() + "条记录");
        try {
            socket = new DatagramSocket(getDnsPort());
        } catch (SocketException e) {
            e.printStackTrace();
        }
        byte[] data = new byte[1024];
        DatagramPacket packet = new DatagramPacket(data, data.length);

        int num = 0;
        while (true) {
            try {
                socket.receive(packet);
            } catch (IOException e) {
                e.printStackTrace();
            }
            DNSQuery queryParser = new DNSQuery(packet);
            System.out.println(queryParser.getName()+"开始");
            queryParser.start();
        }
    }
}