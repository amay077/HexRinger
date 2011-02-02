package com.amay077.android.twitter;

public class AuthInfo {
	static private String SEP = ",";

	public String consumerToken = null;
	public String consumerSecret = null;
	public String userId = null;

	private AuthInfo() {
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();

		if (isEmpty()) {
			return "";
		}

		builder.append(consumerToken);
		builder.append(SEP);
		builder.append(consumerSecret);
		builder.append(SEP);
		builder.append(userId);
		return builder.toString();
	}

	public boolean isEmpty() {
		return !(consumerToken != null && consumerSecret != null && userId != null);
	}

	static public AuthInfo fromString(String text) {
		AuthInfo info = new AuthInfo();
		String[] buf = text.split(SEP);

		if (buf.length >= 3) {
			info.consumerToken = buf[0];
			info.consumerSecret = buf[1];
			info.userId = buf[2];
		}

		return info;
	}

	static public AuthInfo makeEmpty() {
		return new AuthInfo();
	}

}

