public class KLine {

	private char type = 'G';
	private String mask = "";
	private String op = "";
	private int ttl = 0;
	private int age = 0;
	private String reason = "";

	KLine() {

	}

	public void setType(char type) {
		this.type = type;
	}

	public char getType() {
		return type;
	}

	public void setMask(String mask) {
		this.mask = mask;
	}

	public String getMask() {
		return mask;
	}

	public void setOp(String op) {
		this.op = op;
	}

	public String getOp() {
		return op;
	}

	public void setTtl(int ttl) {
		this.ttl = ttl;
	}

	public int getTtl() {
		return ttl;
	}

	public void setAge(int age) {
		this.age = age;
	}

	public int getAge() {
		return age;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}

	public String getReason() {
		return reason;
	}
}
