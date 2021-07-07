
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

    DNSQuery(DatagramPacket packet) {
        data = new byte[packet.getLength()];
        dataLength = packet.getLength();
        address = packet.getAddress();
        port = packet.getPort();
        System.arraycopy(packet.getData(), 0, data, 0, packet.getLength());
    }

    public void LevelDisplay(String qn, short qt, short qc) { //level  1 2 3
    	SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    	System.out.print(" "+c+"  "+df.format(new Date())+ "  Client 127.0.0.1       ");
    	System.out.println(qn+", TYPE "+qt+", CLASS "+qc);
    	c=c+1;
        if(d==3) {
        	System.out.println("byte info is "+Convert.byteArrayToHexString(data));
        }
    }

    public static String getDomain(byte[] bytes, int offset, int stop) {
        StringBuilder stringBuffer = new StringBuilder();
        int length;
        String string;
        byte[] data;
        while (offset < bytes.length && (bytes[offset] & 0xff) != stop){
            length = (bytes[offset] & 0xff);
            offset++;
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
        byte[] dnsQuestionByteArray = dnsQuestion.toByteArray();

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

        receive(responseData, responseData.length);
    }
    public void getIPfromInternet() throws IOException {
        InetAddress dnsAddress = InetAddress.getByName(DNSRelayServer.getDnsAddr());

        DatagramPacket sendPacket = new DatagramPacket(data, dataLength, dnsAddress, DNSRelayServer.getDnsPort());

        DatagramSocket socket = new DatagramSocket();
        socket.send(sendPacket);

        byte[] data = new byte[1024];
        DatagramPacket receivedPacket = new DatagramPacket(data, data.length);
        socket.receive(receivedPacket);

        receive(data, receivedPacket.getLength());
        socket.close();
    }

    @Override
    public void run() {
        int offset = 0;
        byte[] buff2 = new byte[2];
        dnsHeader = new DNSHeader();
        dnsQuestion = new DNSQuestion();
        d=DNSRelayServer.getd();
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

        
        String ip = DNSRelayServer.getDomainIpMap().getOrDefault(dnsQuestion.getQname(), "");
        if(d>=2) {
        	LevelDisplay(dnsQuestion.getQname(),dnsQuestion.getQtype(),dnsQuestion.getQclass());
        }
        
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
        } else {
            try {
                getIPfromInternet();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
