

public class DNSResource {
	/**
	 * Answer/Authority/Additional
	   0  1  2  3  4  5  6  7  0  1  2  3  4  5  6  7
	  +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
	  |					   ... 						  |
	  |                    NAME                       |
	  |                    ...                        |
	  +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
	  |                    TYPE                       |
	  +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
	  |                    CLASS                      |
	  +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
	  |                    TTL                        |
      |                                               |
	  +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
	  |                    RDLENGTH                   |
	  +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
	  |                    ...                        |
	  |                    RDATA                      |
	  |                    ...                        | 
	  +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
	 */
	
	/* NAME (2字节 采用消息压缩) */
	private short name;
	
	/* TYPE（2字节） */
	private short type;
	
	/* CLASS（2字节） */
	private short class_;
	
	/* TTL（4字节） */
	private int ttl;
	
	/* RDLENGTH（2字节） */
	private short rdlength;
	
	/* RDATA IPv4为4字节*/
	private String rdata;

	public DNSResource(short name, short type, short class_, int ttl, short rdlength, String rdata) {
		this.name = name;
		this.type = type;
		this.class_ = class_;
		this.ttl = ttl;
		this.rdlength = rdlength;
		this.rdata = rdata;
	}
	
    /**
     * 输出包含DNS RR所有信息的字节数组
     */

	byte[] data;
	int offset;
	byte[] byte2;
	byte[] byte4;
	byte[] byte16;
	public void shortToByteArray(short sh){
		byte2 = Convert.shortToByteArray(sh);
		for (int i=0; i<2; i++) {
			data[offset] = byte2[i];
			offset++;
		}
	}
    public byte[] toByteArray() {
		data = new byte[12 + rdlength];
		offset = 0;
		shortToByteArray(name);
		shortToByteArray(type);
		shortToByteArray(class_);
        byte4 = Convert.intToByteArray(ttl);
        for (int i=0; i<4; i++) {
			data[offset] = byte4[i];
			offset++;
        }
		if (rdlength == 4) {
			shortToByteArray(rdlength);
			byte4 = Convert.ipv4ToByteArray(rdata);
			for (int i = 0; i < 4; i++) {
				data[offset] = byte4[i];
				offset++;
			}
		}
		if (rdlength == 16) {
			shortToByteArray(rdlength);
			byte16 = Convert.ipv6ToByteArray(rdata);
			for (byte b : byte16) {
				data[offset] = b;
				offset++;
			}
		}
		return data;
	}
}
