package net.fj.ctrl;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import javax.servlet.http.HttpServletRequest;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RequestReaders {

	public static String pathInfo(HttpServletRequest req) {
		return req.getPathInfo();
	}

	public static Optional<String> header(String name, HttpServletRequest req) {
		return Optional.ofNullable(req.getHeader(name));
	}

	public static List<String> headers(String name, HttpServletRequest req) {
		return StreamSupport
				.stream(Spliterators.spliteratorUnknownSize(enumerationIterator(req.getHeaders(name)), Spliterator.ORDERED), false)
				.collect(Collectors.toList());
	}

	public static Stream<String> bodyStream(HttpServletRequest req) {
		try {
			return req.getReader().lines();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static String body(HttpServletRequest req) {
		return bodyStream(req).collect(Collectors.joining(System.lineSeparator()));
	}

	private static <A> Iterator<A> enumerationIterator(Enumeration<A> e) {
		return new Iterator<A>() {
			@Override
			public boolean hasNext() {
				return e.hasMoreElements();
			}

			@Override
			public A next() {
				return e.nextElement();
			}
		};
	}
}
