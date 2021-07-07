

public class DNSQuestion {
	/**
	 * Question 查询字段
		0  1  2  3  4  5  6  7  0  1  2  3  4  5  6  7
	  +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
	  |                     ...                       |
	  |                    QNAME                      |
	  |                     ...                       |
	  +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
	  |                    QTYPE                      |
	  +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
	  |                    QCLASS                     |
	  +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
	 */
	public DNSQuestion() {}
	/* QNAME 8bit为单位表示的查询名(广泛的说就是：域名) */
	private String qname;
	
	/* QTYPE（2字节） */
	private short qtype;
	
	/* QCLASS（2字节） */
	private short qclass;
	

	public void setQname(String qname) {
		this.qname = qname;
	}

	public void setQtype(short qtype) {
		this.qtype = qtype;
	}

	public void setQclass(short qclass) {
		this.qclass = qclass;
	}

	public String getQname() {
		return qname;
	}

	public short getQtype() {
		return qtype;
	}

	public short getQclass() {
		return qclass;
	}

    /**
     * 输出包含DNS Question所有信息的字节数组
     */

	byte[] data;
	int offset;
	byte[] byte2;
	public void shortToByteArray(short sh){
		byte2 = Convert.shortToByteArray(sh);
		for (int i=0; i<2; i++) {
			data[offset] = byte2[i];
			offset++;
		}
	}
	public byte[] toByteArray() {
		data = new byte[qname.length() + 2 + 4];
		offset = 0;
        byte[] domainByteArray = Convert.domainToByteArray(qname);
		for (byte i : domainByteArray) {
			data[offset] = i;
			offset++;
		}
		shortToByteArray(qtype);
		shortToByteArray(qclass);
        return data;
    }
}
