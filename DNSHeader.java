
public class DNSHeader {
	/**
	 * DNS Header
	    0  1  2  3  4  5  6  7  0  1  2  3  4  5  6  7
	  +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
	  |                      ID                       |
	  +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
	  |QR|  opcode   |AA|TC|RD|RA|   Z    |   RCODE   |
	  +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
	  |                    QDCOUNT                    |
	  +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
	  |                    ANCOUNT                    |
	  +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
	  |                    NSCOUNT                    |
	  +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
	  |                    ARCOUNT                    |
	  +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
	 */
	
	/* 会话标识（2字节）*/
	private short ID;

	/* RCODE（2字节）*/
	private short rcode;
	
	/* QDCOUNT（2字节）*/
	private short qdcount;
	
	/* ANCOUNT（2字节）*/
	private short ancount;
	
	/* NSCOUNT（2字节）*/
	private short nscount;
	
	/* ARCOUNT（2字节）*/
	private short arcount;

	public DNSHeader() {}

	public DNSHeader(short ID, short rcode, short qdcount, short ancount, short nscount, short arcount) {
		this.ID = ID;
		this.rcode = rcode;
		this.qdcount = qdcount;
		this.ancount = ancount;
		this.nscount = nscount;
		this.arcount = arcount;
	}

	public void setID(short ID) {
		this.ID = ID;
	}

	public void setRcode(short rcode) {
		this.rcode = rcode;
	}

	public void setQdcount(short qdcount) {
		this.qdcount = qdcount;
	}

	public void setAncount(short ancount) {
		this.ancount = ancount;
	}

	public void setNscount(short nscount) {
		this.nscount = nscount;
	}

	public void setArcount(short arcount) {
		this.arcount = arcount;
	}

	public short getID() {
		return ID;
	}

	public short getQdcount() {
		return qdcount;
	}
    /**
     * 输出包含DNS协议头所有信息的字节数组
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
		data = new byte[12];
		offset = 0;
		shortToByteArray(ID);
		shortToByteArray(rcode);
		shortToByteArray(qdcount);
		shortToByteArray(ancount);
		shortToByteArray(nscount);
		shortToByteArray(arcount);
        return data;
    }
}
