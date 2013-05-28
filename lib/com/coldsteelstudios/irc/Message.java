package com.coldsteelstudios.irc;

public class Message implements Comparable<Message> {

	private MessageTarget source;
	private MessageTarget target;

	private String command;

	private String msg = "";

	private String[] args;

	private Priority priority;

	private MessageCode code;

	private MessageType type;

	private String raw = "";

	private long time;

	private boolean fromMe = false;

	private boolean toMe = false;

	Message() {
		time = System.nanoTime();
	}

	/** 
	 * Package private setters...
	 */
	void setArgs(String[] args) {
		this.args = args;
	}
	
	void setCommand(String command) {
		this.command = command;
	}

	void setSource(MessageTarget source) {
		this.source = source;
	}

	void setTarget(MessageTarget target) {
		this.target = target;
	}
	
	void setPriority(Priority priority) {
		this.priority = priority;
	}
	
	void setMessage(String msg) {
		this.msg = msg;
	}
	
	void setCode( MessageCode code ) {
		this.code = code;
	}

	void setType( MessageType type ) {
		this.type = type;
	}

	void setRaw(String raw) {
		this.raw = raw;
	}

	void setFromMe(boolean bool) {
		fromMe = bool;
	}

	void setToMe(boolean bool) {
		toMe = bool;
	}



	public int numArgs() {
		return args.length;
	}

	public String getArg(int n) {
		return args[n];
	}

	public String get(int n) {
		return getArg(n);
	}

	public String[] getArgs() {
		return args;
	}

	public String getCommand() {
		return command;
	}

	public String getMessage() {
		return msg;
	}

	public String getRaw() {
		return raw;
	}

	public MessageTarget getSource() {
		return source;
	}

	public MessageTarget getTarget() {
		return target;
	}

	public Priority getPriority() {
		return priority;
	}

	public MessageType getType() {
		return type;
	}

	public MessageCode getCode() {
		return code;
	}

	public boolean isFromMe() {
		return fromMe;
	}

	/**
	 * The from me one is useful
	 * this probably isn't.
	 */
	public boolean isToMe() {
		return toMe;
	}

	public int compareTo(Message m) {
		return  priority.getValue() - m.priority.getValue();

//		if (val != 0) return val;

//		return (int)(time - m.time);
	}


}
