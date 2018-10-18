package net.fj.ctrl;

import java.io.IOException;
import java.util.function.Consumer;

import javax.servlet.http.HttpServletResponse;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ResponseWriters {

	public static Consumer<HttpServletResponse> sc(int sc) {
		return r -> r.setStatus(sc);
	}

	public static Consumer<HttpServletResponse> scOk() {
		return sc(HttpServletResponse.SC_OK);
	}
	
	public static Consumer<HttpServletResponse> scInternalError() {
		return sc(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
	}

	public static Consumer<HttpServletResponse> scNotFound() {
		return sc(HttpServletResponse.SC_NOT_FOUND);
	}

	public static Consumer<HttpServletResponse> scBadRequest() {
		return sc(HttpServletResponse.SC_BAD_REQUEST);
	}

	public static Consumer<HttpServletResponse> header(String name, String value) {
		return r -> r.setHeader(name, value);
	}

	public static Consumer<HttpServletResponse> body(String body) {
		return r -> {
			try {
				r.getWriter().write(body);
				r.getWriter().flush();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		};
	}

}
