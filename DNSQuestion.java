

public class DNSQuestion {

	public DNSQuestion() {}
	private String qname;
	private short qtype;
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

	byte[] data;
	int offset;
	byte[] byte2;
	public void shortToByteArray(short sh){
		byte2 = Convert.shortToByteArray(sh);
		//2 bytes
		for (int i=0; i<2; i++) {
			data[offset] = byte2[i];
			offset++;
		}
	}

	/**
	 * @Description: Outputs a byte array containing all the information of the DNS Question
	 */
	public byte[] toByteArray() {
		data = new byte[qname.length() + 6];
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
