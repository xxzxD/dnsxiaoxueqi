import java.util.Arrays;

public class Convert {

    /**
     * �� short ��������תΪ byte[]
     */
    public static byte[] shortToByteArray(short i) {
        byte[] bytes = new byte[2];
        //�ɸ�λ����λ
        bytes[1] = (byte)(i & 0xff);
        bytes[0] = (byte)((i >> 8) & 0xff);
        return bytes;
    }

    /**
     * һά�ֽ�����ת short ֵ(2 �ֽ�)
     */
    public static short byteArrayToShort(byte[] bytes){
        //0xffʮ������ ��24λ��0��0000 0000 0000 0000 0000 0000 1111 1111
        //&����� ͬΪ1ʱ��Ϊ1 << : �����������num << 1,�൱��num����2
        //|��λ�� 1|0 = 1 , 1|1 = 1 , 0|0 = 0 , 0|1 = 1
        int offset = 0;
        return (short) ((bytes[offset+1] & 0xff) | ((bytes[offset]& 0xff)  << 8));
    }

    /**
     * �� int ��������תΪ byte[]
     */
    public static byte[] intToByteArray(int i) {
        byte[] bytes = new byte[4];
        //�ɸ�λ����λ
        for (int t = 0; t < 4; t++) {
            bytes[t] = (byte) ((i >> (24 - t * 8)) & 0xff);
        }
        return bytes;
    }

    /**
     * byte[] ת��Ϊ16�����ַ���
     */
    public static String byteArrayToHexString (byte[] bytes) {
        StringBuilder stringBuffer = new StringBuilder();
        String string;
        for (byte b: bytes) {
            string = Integer.toHexString((b & 0xff)/*byteתint*/);
            if (string.length() < 2) {
                stringBuffer.append(0);
            }
            stringBuffer.append(string.toUpperCase());
        }
        return stringBuffer.toString();
    }

    public static byte[] hexStringToBytes(String hexString)
    {
        hexString = hexString.toUpperCase();
        int length = hexString.length() / 2;
        char[] hexChars = hexString.toCharArray();
        byte[] bytes = new byte[length];
        for (int i = 0; i < length; i++) {
            bytes[i] = (byte) (charToByte(hexChars[i * 2]) << 4 | charToByte(hexChars[(i * 2) + 1]));
        }
        return bytes;
    }

    private static byte charToByte(char c) {
        return (byte) "0123456789ABCDEF".indexOf(c);
    }

    /**
     * ����ת��Ϊ�ֽ�����
     */
    public static byte[] domainToByteArray(String domain) {
        byte[] bytes = new byte[domain.length()+2];
        int offset = 0;
        String[] domainArray = domain.split("\\.");
        for (String d: domainArray) {
            bytes[offset] = (byte) d.length();
            offset++;
            for (char c: d.toCharArray()) {
                bytes[offset] = (byte)c;
                offset++;
            }
        }
        bytes[offset] = 0x00;
        return bytes;
    }

    /**
     * IPv4���ʮ����ת��Ϊһά�ֽ�����
     */
    public static byte[] ipv4ToByteArray(String ip) {
        byte[] bytes = new byte[4];
        String[] ipv4Array = ip.split("\\.");
        for (int i=0; i<ipv4Array.length; i++) {
            int num = Integer.parseInt(ipv4Array[i]);
            bytes[i] = (byte) (num & 0xff);
        }
        return bytes;
    }
    public static byte[] ipv6ToByteArray(String ip) {
        byte[] bytes = new byte[16];
        int count = 0;
        String[] ipv6Array = ip.split(":");
        for (int i=0; i<ipv6Array.length; i++) {
            ipv6Array[i] = String.format("%0" + 4 + "d", Integer.parseInt(ipv6Array[i]));
            int length = ipv6Array[i].length() / 2;
            char[] hexChars = ipv6Array[i].toCharArray();
            for (int t = count; t < length+count; t++) {
                bytes[t] = (byte) (charToByte(hexChars[(t-count) * 2]) << 4 | charToByte(hexChars[((t-count) * 2) + 1]));
            }
            count = count+length;
        }
        return bytes;
    }
}
