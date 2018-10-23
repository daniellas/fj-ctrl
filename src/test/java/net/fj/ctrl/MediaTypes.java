package net.fj.ctrl;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MediaTypes {
	public static final String TEXT_PLAIN = "text/plain";

	public static final String TEXT_HTML = "text/html";

	public static final String APP_JSON = "application/json";

	public static String withCharset(String mediaType, String charset) {
		return mediaType + ";charset=" + charset;
	}
}
