
import java.io.IOException;
import java.io.UnsupportedEncodingException;
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
     * ���ֽ���������ȡ������
     */
    public static String extractDomain(byte[] bytes, int offset, int stop) throws UnsupportedEncodingException {
        StringBuilder stringBuffer = new StringBuilder();
        int length;
        String asciiStr;
        byte[] data;
        while (offset < bytes.length && (bytes[offset] & 0xff) != stop){
            length = (bytes[offset] & 0xff);
            offset++;
            //һά�ֽ�����ת��ΪAscii��Ӧ���ַ���
            data = new byte[length];
            System.arraycopy(bytes, offset, data, 0, length);
            asciiStr = new String(data, "ISO8859-1");
            stringBuffer.append(asciiStr);
            offset += length;
            if(offset < bytes.length && (bytes[offset] & 0xff) != stop) {
                stringBuffer.append(".");
            }
        }
        return stringBuffer.toString();
    }

    @Override
    public void run() {
        int offset = 0;
        byte[] buff2 = new byte[2];
        DNSHeader dnsHeader = new DNSHeader();
        DNSQuestion dnsQuestion = new DNSQuestion();
        // �������󣬷��ؽ��

        /*for (int i = 0; i < 2; i++) {
            buff2[i] = data[i + offset];
        }*/
        // ��ȡDNSЭ��ͷ
        for(int i=0; i<6; i++){
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

        // ��ȡ��ѯ������

        // qdcountͨ��Ϊ1
        String domainName = null;
        try {
            domainName = extractDomain(data, offset, 0x00);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        dnsQuestion.setQname(domainName);
        assert domainName != null;
        offset += domainName.length() + 2;

        for(int i=0; i<2; i++){
            System.arraycopy(data, offset, buff2, 0, 2);
            offset += 2;
            if (i == 0) {
                dnsQuestion.setQtype(Convert.byteArrayToShort(buff2));
            } else {
                dnsQuestion.setQclass(Convert.byteArrayToShort(buff2));
            }
        }


        // ��ѯ��������-IPӳ��
        String ip = DNSRelayServer.getDomainIpMap().getOrDefault(dnsQuestion.getQname(), "");
        
        
        System.out.println(this.getName() + " Local search results domain:" + dnsQuestion.getQname() + " QTYPE:" + dnsQuestion.getQtype() + " ip:" + ip);

        // �ڱ�������-IPӳ���ļ����ҵ�����Ҳ�ѯ����ΪA(Host Address)������ش�����ݰ�
        if (!"".equals(ip) && dnsQuestion.getQtype() == 1) {
            // Header
            short rcode;
            
            // rcodeΪ3�����ֲ����ֻ��һ����Ȩ���ַ������Ϸ��أ�����ʾ�ڲ�ѯ��ָ��������������
            if ("0.0.0.0".equals(ip)) {
                rcode = (short) 0x8583;
            } else {// rcodeΪ0��û�в��
                rcode = (short) 0x8580;
            }
            DNSHeader dnsHeaderResponse = new DNSHeader(dnsHeader.getID(), rcode, dnsHeader.getQdcount(), (short) 1, (short) 1, (short) 0);
            byte[] dnsHeaderByteArray = dnsHeaderResponse.toByteArray();

            // Questions
            byte[] dnsQuestionByteArray = dnsQuestion.toByteArray();

            // Answers
            DNSResource answer = new DNSResource((short) 0xc00c, dnsQuestion.getQtype(), dnsQuestion.getQclass(), 3600*24, (short) 4, ip);
            byte[] answerByteArray = answer.toByteArray();

            // Authoritative nameservers��ֻ��ģ���˰���ʽ��nameserverʵ��ָ���˲�ѯ������
            DNSResource nameserver = new DNSResource((short) 0xc00c, (short) 6, dnsQuestion.getQclass(), 3600*24, (short) 0 , null);
            byte[] nameserverByteArray = nameserver.toByteArray();

            byte[] responseData = new byte[dnsHeaderByteArray.length + dnsQuestionByteArray.length + answerByteArray.length + nameserverByteArray.length];
            int responseOffset = 0;

            for (byte b : dnsHeaderByteArray) {
                responseData[responseOffset++] = b;
            }
            for (byte b : dnsQuestionByteArray) {
                responseData[responseOffset++] = b;
            }
            if (!"0.0.0.0".equals(ip)) {
                for (byte b : answerByteArray) {
                    responseData[responseOffset++] = b;
                }
            }
            for (byte b : nameserverByteArray) {
                responseData[responseOffset++] = b;
            }

            System.out.println(this.getName() + " ��Ӧ���ݣ�" +Convert.byteArrayToHexString(answerByteArray));

            // �ظ���Ӧ���ݰ�
            DatagramPacket responsePacket = new DatagramPacket(responseData, responseData.length, address, port);
            synchronized (DNSRelayServer.LOCK_OBJ) {
                    System.out.println(this.getName() + "���socket����Ӧ" + dnsQuestion.getQname() + ":" + ip);
                try {
                    DNSRelayServer.getSocket().send(responsePacket);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else { // ����δ������������������DNS������
            System.out.println(this.getName() + " ����������DNS������");

            InetAddress dnsServerAddress = null;
            try {
                dnsServerAddress = InetAddress.getByName(DNSRelayServer.getDnsAddr());
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
            DatagramPacket internetSendPacket = new DatagramPacket(data, dataLength, dnsServerAddress, DNSRelayServer.getDnsPort());
            DatagramSocket internetSocket = null;
            try {
                internetSocket = new DatagramSocket();
            } catch (SocketException e) {
                e.printStackTrace();
            }
            try {
                assert internetSocket != null;
                internetSocket.send(internetSendPacket);
            } catch (IOException e) {
                e.printStackTrace();
            }
            byte[] receivedData = new byte[1024];
                DatagramPacket internetReceivedPacket = new DatagramPacket(receivedData, receivedData.length);
            try {
                internetSocket.receive(internetReceivedPacket);
            } catch (IOException e) {
                e.printStackTrace();
            }

            // �ظ���Ӧ���ݰ�
            DatagramPacket responsePacket = new DatagramPacket(receivedData, internetReceivedPacket.getLength(), address, port);
            internetSocket.close();
            synchronized (DNSRelayServer.LOCK_OBJ) {
                    System.out.println(this.getName() + " ���socket����Ӧ" + dnsQuestion.getQname());
                try {
                    DNSRelayServer.getSocket().send(responsePacket);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}