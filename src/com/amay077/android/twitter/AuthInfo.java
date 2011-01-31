package com.amay077.android.twitter;

public class AuthInfo {
	public String consumerToken;
	public String consumerSecret;
	public String userId;

	public AuthInfo(String text) {
		String[] buf = text.split("／");

		if (buf.length >= 3) {
			consumerToken = buf[0];
			consumerSecret = buf[1];
			userId = buf[2];
		}
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(consumerToken);
		builder.append("／");
		builder.append(consumerSecret);
		builder.append("／");
		builder.append(userId);
		return builder.toString();
	}

}

