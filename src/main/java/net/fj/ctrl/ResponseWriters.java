package net.fj.ctrl;

import java.io.IOException;
import java.util.function.Consumer;

import javax.servlet.http.HttpServletResponse;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ResponseWriters {

	public static Consumer<HttpServletResponse> status(int sc) {
		return r -> r.setStatus(sc);
	}

	public static Consumer<HttpServletResponse> internalError() {
		return status(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
	}

	public static Consumer<HttpServletResponse> notFound() {
		return status(HttpServletResponse.SC_NOT_FOUND);
	}

	public static Consumer<HttpServletResponse> badRequest() {
		return status(HttpServletResponse.SC_BAD_REQUEST);
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
