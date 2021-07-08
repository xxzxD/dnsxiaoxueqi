

public class DNSResource {

	//2 bytes, Message compression, 0xc00c
	private final short name;
	private final short type;
	private final short class_;
	//4 bytes
	private final int ttl;
	private final short rdlength;
	//ipv4 is 4 bytes and ipv6 is 16 bytes
	private final String rdata;

	public DNSResource(short name, short type, short class_, int ttl, short rdlength, String rdata) {
		this.name = name;
		this.type = type;
		this.class_ = class_;
		this.ttl = ttl;
		this.rdlength = rdlength;
		this.rdata = rdata;
	}

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

	/**
	 * @Description: Outputs a byte array containing all the information of the DNS Resource Record
	 */
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
        //ipv4
		if (rdlength == 4) {
			shortToByteArray(rdlength);
			byte4 = Convert.ipv4ToByteArray(rdata);
			for (int i = 0; i < 4; i++) {
				data[offset] = byte4[i];
				offset++;
			}
		}
		//ipv6
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
