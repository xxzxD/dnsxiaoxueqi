
public class DNSHeader {

	private short ID;
	private short rcode;
	private short qdcount;
	private short ancount;
	private short nscount;
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

	byte[] data;
	int offset;
	byte[] byte2;

	/**
	 * @Description: Short converts to byte array
	 * @param sh
	 */
	public void shortToByteArray(short sh){
		byte2 = Convert.shortToByteArray(sh);
		for (int i=0; i<2; i++) {
			data[offset] = byte2[i];
			offset++;
		}
	}

	/**
	 * @Description: Output a byte array containing all the information of the DNS header
	 */
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
