

public class DNSQuestion {
	/**
	 * Question ��ѯ�ֶ�
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
	/* QNAME 8bitΪ��λ��ʾ�Ĳ�ѯ��(�㷺��˵���ǣ�����) */
	private String qname;
	
	/* QTYPE��2�ֽڣ� */
	private short qtype;
	
	/* QCLASS��2�ֽڣ� */
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
     * �������DNS Question������Ϣ���ֽ�����
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
