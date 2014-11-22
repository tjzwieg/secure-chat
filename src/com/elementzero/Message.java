package com.elementzero;

public class Message {

	private Long id;
	private Object message;
	private Long toId;
	private Long fromId;
	
	@SuppressWarnings("unused")
	private Message() {}
	
	public Message(Long mid, Object message, Long toId, Long fromId) {
		this.id = mid;
		this.message = message;
		this.toId = toId;
		this.fromId = fromId;
	}
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Object getMessage() {
		return message;
	}

	public void setMessage(Object message) {
		this.message = message;
	}

	public Long getToId() {
		return toId;
	}

	public void setToId(Long toId) {
		this.toId = toId;
	}

	public Long getFromId() {
		return fromId;
	}

	public void setFromId(Long fromId) {
		this.fromId = fromId;
	}

	
	
	
}
