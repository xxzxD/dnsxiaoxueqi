

public class Convert {

    /**
     * @Description: Convert short data to byte[](2 bytes byte array)
     * @param i
     */
    public static byte[] shortToByteArray(short i) {
        byte[] bytes = new byte[2];
        bytes[1] = (byte)(i & 0xff);
        bytes[0] = (byte)((i >> 8) & 0xff);
        return bytes;
    }

    /**
     * @Description: Convert byte[] data to short
     * @param bytes
     */
    public static short byteArrayToShort(byte[] bytes){
        //0xff十六进制 高24位补0：0000 0000 0000 0000 0000 0000 1111 1111
        //&与操作 同为1时才为1 << : 左移运算符，num << 1,相当于num乘以2
        //|按位或 1|0 = 1 , 1|1 = 1 , 0|0 = 0 , 0|1 = 1
        int offset = 0;
        return (short) ((bytes[offset+1] & 0xff) | ((bytes[offset]& 0xff)  << 8));
    }

    /**
     * @Description: Convert int data to byte[]
     * @param i
     */
    public static byte[] intToByteArray(int i) {
        byte[] bytes = new byte[4];
        for (int t = 0; t < 4; t++) {
            bytes[t] = (byte) ((i >> (24 - t * 8)) & 0xff);
        }
        return bytes;
    }

    /**
     * @Description: Convert byte[] data to a hexadecimal string
     * @param bytes
     */
    public static String byteArrayToHexString (byte[] bytes) {
        StringBuilder stringBuffer = new StringBuilder();
        String string;
        for (byte b: bytes) {
            string = Integer.toHexString((b & 0xff)/*Convert byte to int*/);
            if (string.length() < 2) {
                stringBuffer.append(0);
            }
            stringBuffer.append(string.toUpperCase());
        }
        return stringBuffer.toString();
    }

    /**
     * @Description: Convert domain name to byte[]
     * @param domain
     */
    public static byte[] domainToByteArray(String domain) {
        byte[] bytes = new byte[domain.length()+2];
        int offset = 0;
        //Split by ".", www.qq.com
        String[] domainArray = domain.split("\\.");
        for (String d: domainArray) {
            bytes[offset] = (byte) d.length();
            offset++;
            for (char c: d.toCharArray()) {
                bytes[offset] = (byte)c;
                offset++;
            }
        }
        //stop
        bytes[offset] = 0x00;
        return bytes;
    }

    /**
     * @Description: ipv4
     * Decimal conversion to a byte[]
     * @param ip
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

    /**
     * @Description: ipv6
     * Hexadecimal conversion to a byte[]
     * @param ip
     */
    public static byte[] ipv6ToByteArray(String ip) {
        byte[] bytes = new byte[16];
        int count = 0;
        String[] ipv6Array = ip.split(":");
        for (int i=0; i<ipv6Array.length; i++) {
            //Complement with 0, 1->0001
            ipv6Array[i] = String.format("%0" + 4 + "d", Integer.parseInt(ipv6Array[i]));
            //Convert hexadecimal string to byte[]
            int length = ipv6Array[i].length() / 2;
            char[] hexChars = ipv6Array[i].toCharArray();
            for (int t = count; t < length+count; t++) {
                bytes[t] = (byte) (charToByte(hexChars[(t-count) * 2]) << 4 | charToByte(hexChars[((t-count) * 2) + 1]));
            }
            count = count+length;
        }
        return bytes;
    }
    private static byte charToByte(char c) {
        return (byte) "0123456789ABCDEF".indexOf(c);
    }
}
