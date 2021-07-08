
import java.io.IOException;
import java.net.*;
import java.util.Date;
import java.text.SimpleDateFormat;

public class DNSQuery extends Thread {
    private final byte[] data;
    private final int dataLength;
    private final InetAddress address;
    private final int port;
    private int d=0;
    private static int c=0;

    /**
     * @Description: Get data from packet
     * @param packet
     */
    DNSQuery(DatagramPacket packet) {
        data = new byte[packet.getLength()];
        dataLength = packet.getLength();
        address = packet.getAddress();
        port = packet.getPort();
        //将packet.getData()数组中 从0位到第packet.getLength()位之间的数值从data数组的第0位开始copy到data数组中
        System.arraycopy(packet.getData(), 0, data, 0, packet.getLength());
    }

    /**
     * @Description: Display according to the level
     * @param qn
     * @param qt
     * @param qc
     */
    public void LevelDisplay(String qn, short qt, short qc) {
        //level  1 2
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        System.out.print(" "+c+"  "+df.format(new Date())+ "  Client 127.0.0.1       ");
        System.out.println(qn+", TYPE "+qt+", CLASS "+qc);
        c=c+1;
        if(d==3) {
            //-dd display hexadecimal data
            System.out.println("byte info is "+Convert.byteArrayToHexString(data));
        }
    }

    /**
     * @Description: Get domain name from byte[]
     * @param bytes
     * @param offset
     * @param stop
     */
    public static String getDomain(byte[] bytes, int offset, int stop) {
        StringBuilder stringBuffer = new StringBuilder();
        int length;
        String string;
        byte[] data;
        //Don't stop until 0x00
        while (offset < bytes.length && (bytes[offset] & 0xff) != stop){
            length = (bytes[offset] & 0xff);
            offset++;
            //Convert byte[] to String
            data = new byte[length];
            System.arraycopy(bytes, offset, data, 0, length);
            string = new String(data);
            stringBuffer.append(string);
            offset += length;
            if(offset < bytes.length && (bytes[offset] & 0xff) != stop) {
                stringBuffer.append(".");
            }
        }
        return stringBuffer.toString();
    }

    DNSHeader dnsHeader = new DNSHeader();
    DNSQuestion dnsQuestion = new DNSQuestion();

    /**
     * @Description: Response packet
     * @param data
     * @param length
     */
    public void response(byte[] data, int length){
        DatagramPacket receivePacket = new DatagramPacket(data, length, address, port);
        try {
            DNSRelayServer.getSocket().send(receivePacket);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * @Description: Get data from local
     * @param ip
     * @param rcode
     */
    public void getDatafromLocal(String ip, short rcode){

        //Make DNSheader
        DNSHeader dnsHeaderResponse = new DNSHeader(dnsHeader.getID(), rcode, dnsHeader.getQdcount(), (short) 1, (short) 1, (short) 0);
        byte[] dnsHeaderByteArray = dnsHeaderResponse.toByteArray();

        //Make DNSQuestions
        byte[] dnsQuestionByteArray = dnsQuestion.toByteArray();

        //Make DNSRR
        short length;
        //ipv4
        if(ip.contains(".")){
            length = 4;
        } else {
            //ipv6
            length = 16;
        }
        DNSResource answer = new DNSResource((short) 0xc00c, dnsQuestion.getQtype(), dnsQuestion.getQclass(), 3600*24, length, ip);
        byte[] answerByteArray = answer.toByteArray();

        //Get response Data
        byte[] responseData = new byte[dnsHeaderByteArray.length + dnsQuestionByteArray.length + answerByteArray.length];
        int offset = 0;

        for (byte b : dnsHeaderByteArray) {
            responseData[offset] = b;
            offset++;
        }
        for (byte b : dnsQuestionByteArray) {
            responseData[offset] = b;
            offset++;
        }

        if ((!"0.0.0.0".equals(ip))&&(!"0:0:0:0:0:0:0:0".equals(ip))) {
            for (byte b : answerByteArray) {
                responseData[offset] = b;
                offset++;
            }
        }

        response(responseData, responseData.length);
    }

    /**
     * @Description: Get data from Internet
     * @throws IOException
     */
    public void getDatafromInternet() throws IOException {

        InetAddress dnsAddress = InetAddress.getByName(DNSRelayServer.getDnsAddr());
        //Make packet to send
        DatagramPacket sendPacket = new DatagramPacket(data, dataLength, dnsAddress, DNSRelayServer.getDnsPort());

        DatagramSocket socket = new DatagramSocket();
        socket.send(sendPacket);

        byte[] data = new byte[1024];
        DatagramPacket receivedPacket = new DatagramPacket(data, data.length);
        //Receive packet
        socket.receive(receivedPacket);

        response(data, receivedPacket.getLength());
        socket.close();
    }


    @Override
    /**
     * @Description: Process the query
     */
    public void run() {
        int offset = 0;
        byte[] buff2 = new byte[2];
        dnsHeader = new DNSHeader();
        dnsQuestion = new DNSQuestion();
        d=DNSRelayServer.getd();

        // Get DNSHeader
        for (int i=0; i<6; i++){
            System.arraycopy(data, offset, buff2, 0, 2);
            offset += 2;
            switch (i){
                case 0:
                    dnsHeader.setID(Convert.byteArrayToShort(buff2));
                    break;
                case 1:
                    dnsHeader.setRcode(Convert.byteArrayToShort(buff2));
                    break;
                case 2:
                    dnsHeader.setQdcount(Convert.byteArrayToShort(buff2));
                    break;
                case 3:
                    dnsHeader.setAncount(Convert.byteArrayToShort(buff2));
                    break;
                case 4:
                    dnsHeader.setNscount(Convert.byteArrayToShort(buff2));
                    break;
                default:
                    dnsHeader.setArcount(Convert.byteArrayToShort(buff2));
                    break;
            }
        }

        // Get DNSQuestion
        String domainName;
        domainName = getDomain(data, offset, 0x00);
        dnsQuestion.setQname(domainName);
        offset += domainName.length() + 2;

        for (int i=0; i<2; i++){
            System.arraycopy(data, offset, buff2, 0, 2);
            offset += 2;
            if (i == 0) {
                dnsQuestion.setQtype(Convert.byteArrayToShort(buff2));
            } else {
                dnsQuestion.setQclass(Convert.byteArrayToShort(buff2));
            }
        }

        // Query local domain name by IP map, return "" if cannot find
        // getOrDefault() 方法获取指定 key 对应对 value，如果找不到 key ，则返回设置的默认值。
        String ipv4 = "";
        String ipv6 = "";
        String ip = DNSRelayServer.getDomainIp().getOrDefault(dnsQuestion.getQname(), "");
        //Both have ipv4 and ipv6
        if(ip.contains(",")){
            String[] ipList = ip.split(",");
            ipv4 = ipList[0];
            ipv6 = ipList[1];
        } else if(!"".equals(ip)){
            //ipv4
            if(ip.contains(".")){
                ipv4 = ip;
            } else if (ip.contains(":")){
                //ipv6
                ipv6 = ip;
            }
        }

        //-d
        if(d>=2) {
            LevelDisplay(dnsQuestion.getQname(),dnsQuestion.getQtype(),dnsQuestion.getQclass());
        }

        //Construct the packet of receive
        short rcode;
        //ipv4 and in local
        if (dnsQuestion.getQtype() == 1 && !"".equals(ipv4)){
            //Domain name does not exist, recode = 3
            if("0.0.0.0".equals(ipv4)){
                rcode = 3;
            } else {
                rcode = 0;
            }
            getDatafromLocal(ipv4, rcode);
        //ipv and in local
        } else if (dnsQuestion.getQtype() == 28 && !"".equals(ipv6)){
            if("0:0:0:0:0:0:0:0".equals(ipv6)){
                rcode = 3;
            } else {
                rcode = 0;
            }
            getDatafromLocal(ipv6, rcode);
        //Cannot find in local, query through Internet DNS server
        } else {
            try {
                getDatafromInternet();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
