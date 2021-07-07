
import java.io.IOException;
import java.net.*;


public class DNSQuery extends Thread {
    private final byte[] data;
    private final int dataLength;
    private final InetAddress address;
    private final int port;

    DNSQuery(DatagramPacket packet) {
        data = new byte[packet.getLength()];
        dataLength = packet.getLength();
        address = packet.getAddress();
        port = packet.getPort();
        System.arraycopy(packet.getData(), 0, data, 0, packet.getLength());
    }

    /**
     * 从字节数组中提取出域名
     */
    public static String getDomain(byte[] bytes, int offset, int stop) {
        StringBuilder stringBuffer = new StringBuilder();
        int length;
        String string;
        byte[] data;
        while (offset < bytes.length && (bytes[offset] & 0xff) != stop){
            length = (bytes[offset] & 0xff);
            offset++;
            //一维字节数组转化为Ascii对应的字符串
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

    public void receive(byte[] data, int length){
        DatagramPacket responsePacket = new DatagramPacket(data, length, address, port);
        synchronized (DNSRelayServer.LOCK_OBJ) {
            System.out.println(this.getName() + " 获得socket，响应" + dnsQuestion.getQname());
            try {
                DNSRelayServer.getSocket().send(responsePacket);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    public void getIPfromLocal(String ip, short rcode){

        DNSHeader dnsHeaderResponse = new DNSHeader(dnsHeader.getID(), rcode, dnsHeader.getQdcount(), (short) 1, (short) 1, (short) 0);
        byte[] dnsHeaderByteArray = dnsHeaderResponse.toByteArray();

        // Questions
        byte[] dnsQuestionByteArray = dnsQuestion.toByteArray();

        // Answers
        //0xc00c DNS协议消息压缩技术
        short length;
        if(ip.contains(".")){
            length = 4;
        } else {
            length = 16;
        }
        DNSResource answer = new DNSResource((short) 0xc00c, dnsQuestion.getQtype(), dnsQuestion.getQclass(), 3600*24, length, ip);
        byte[] answerByteArray = answer.toByteArray();

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

        System.out.println(this.getName() + " 响应数据：" +Convert.byteArrayToHexString(answerByteArray));

        // 回复响应数据包
        receive(responseData, responseData.length);
    }
    public void getIPfromInternet() throws IOException {
        System.out.println(this.getName() + " 请求因特网DNS服务器");

        InetAddress dnsAddress = InetAddress.getByName(DNSRelayServer.getDnsAddr());

        DatagramPacket sendPacket = new DatagramPacket(data, dataLength, dnsAddress, DNSRelayServer.getDnsPort());

        DatagramSocket socket = new DatagramSocket();
        socket.send(sendPacket);

        byte[] data = new byte[1024];
        DatagramPacket receivedPacket = new DatagramPacket(data, data.length);
        socket.receive(receivedPacket);

        // 回复响应数据包
        receive(data, receivedPacket.getLength());
        socket.close();
    }

    @Override
    public void run() {
        int offset = 0;
        byte[] buff2 = new byte[2];
        dnsHeader = new DNSHeader();
        dnsQuestion = new DNSQuestion();
        // 处理请求，返回结果

        /*for (int i = 0; i < 2; i++) {
            buff2[i] = data[i + offset];
        }*/
        // 读取DNS协议头
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

        // 获取查询的域名
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

        // 查询本地域名-IP映射
        String ip = DNSRelayServer.getDomainIpMap().getOrDefault(dnsQuestion.getQname(), "");

        System.out.println(this.getName() + " Local search results domain:" + dnsQuestion.getQname() + " QTYPE:" + dnsQuestion.getQtype() + " ip:" + ip);

        // 在本地域名-IP映射文件中找到结果且查询类型为A(Host Address)，构造回答的数据包
        //&& dnsQuestion.getQtype() == 1
        short rcode;
        if (dnsQuestion.getQtype() == 1 && ip.contains(".")){
            if("0.0.0.0".equals(ip)){
                rcode = 3;
            } else {
                rcode = 0;
            }
            getIPfromLocal(ip, rcode);

        } else if (dnsQuestion.getQtype() == 28 && ip.contains(":")){
            if("0:0:0:0:0:0:0:0".equals(ip)){
                rcode = 3;
            } else {
                rcode = 0;
            }
            getIPfromLocal(ip, rcode);
            // Header
                // rcode为3（名字差错），只从一个授权名字服务器上返回，它表示在查询中指定的域名不存在
        } else {
            try {
                getIPfromInternet();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
