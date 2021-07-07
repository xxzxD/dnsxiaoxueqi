import java.io.UnsupportedEncodingException;

public class Convert {

    /**
     * 将 short 类型数据转为 byte[]
     */
    public static byte[] shortToByteArray(short i) {
        byte[] bytes = new byte[2];
        //由高位到低位
        bytes[1] = (byte)(i & 0xff);
        bytes[0] = (byte)((i >> 8) & 0xff);
        return bytes;
    }

    /**
     * 一维字节数组转 short 值(2 字节)
     */
    public static short byteArrayToShort(byte[] bytes){
        //0xff十六进制 高24位补0：0000 0000 0000 0000 0000 0000 1111 1111
        //&与操作 同为1时才为1 << : 左移运算符，num << 1,相当于num乘以2
        //|按位或 1|0 = 1 , 1|1 = 1 , 0|0 = 0 , 0|1 = 1
        int offset = 0;
        return (short) ((bytes[offset+1] & 0xff) | ((bytes[offset]& 0xff)  << 8));
    }

    /**
     * 将 int 类型数据转为 byte[]
     */
    public static byte[] intToByteArray(int i) {
        byte[] bytes = new byte[4];
        //由高位到低位
        for (int t = 0; t < 4; t++) {
            bytes[t] = (byte) ((i >> (24 - t * 8)) & 0xff);
        }
        return bytes;
    }

    /**
     * byte[] 转化为16进制字符串
     */
    public static String byteArrayToHexString (byte[] bytes) {
        StringBuilder stringBuffer = new StringBuilder();
        String string;
        for (byte b: bytes) {
            string = Integer.toHexString((b & 0xff)/*byte转int*/);
            if (string.length() < 2) {
                stringBuffer.append(0);
            }
            stringBuffer.append(string.toUpperCase());
        }
        return stringBuffer.toString();
    }

    /**
     * 域名转化为字节数组
     */
    public static byte[] domainToByteArray(String domain) {
        byte[] bytes = new byte[domain.length()+2];
        int offset = 0;
        String[] domainArray = domain.split("\\.");
        for (String d: domainArray) {
            bytes[offset++] = (byte) d.length();
            for (char c: d.toCharArray()) {
                bytes[offset] = (byte)c;
                offset++;
            }
        }
        bytes[offset] = 0x00;
        offset++;
        return bytes;
    }

    /**
     * IPv4点分十进制转换为一维字节数组
     */
    public static byte[] ipv4ToByteArray(String ip) {
        byte[] bytes = new byte[4];
        String[] ipv4Array = ip.split("\\.");
        for (int i=0; i<ipv4Array.length; i++) {
            int num = Integer.parseInt(ipv4Array[i]);
            byte t;
            if (num > 127) {
                t = (byte)(num - 256);
            } else {
                t = (byte)num;
            }
            bytes[i] = t;
        }
        return bytes;
    }
}
