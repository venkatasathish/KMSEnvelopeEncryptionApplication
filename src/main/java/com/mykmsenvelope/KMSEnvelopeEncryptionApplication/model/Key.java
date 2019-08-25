package com.mykmsenvelope.KMSEnvelopeEncryptionApplication.model;

public class Key {

private String id;
	
	private String keyValue;
	
	private String wrappedKey;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getKeyValue() {
		return keyValue;
	}

	public void setKeyValue(String keyValue) {
		this.keyValue = keyValue;
	}

	public String getWrappedKey() {
		return wrappedKey;
	}

	public void setWrappedKey(String wrappedKey) {
		this.wrappedKey = wrappedKey;
	}
	
	
}
