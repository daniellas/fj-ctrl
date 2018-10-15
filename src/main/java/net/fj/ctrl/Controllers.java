package net.fj.ctrl;

import java.nio.charset.Charset;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

import javax.servlet.http.HttpServletRequest;

import io.atlassian.fugue.Pair;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Controllers {

	public static Controller dispatcher(
			List<Pair<Predicate<HttpServletRequest>, Controller>> ctrls,
			Function<? super HttpServletRequest, ? extends String> notFoundTranslator,
			BiFunction<? super HttpServletRequest, ? super Exception, ? extends String> errorTranslator) {
		return req -> resp -> {
			try {
				ctrls.stream()
						.filter(p -> p.left().test(req))
						.findFirst()
						.map(Pair::right)
						.orElseGet(() -> Controllers.notFound(notFoundTranslator))
						.apply(req)
						.accept(resp);
			} catch (Exception e) {
				internalError(errorTranslator, e)
						.apply(req)
						.accept(resp);
			}
		};
	}

	public static Controller dispatcher(List<Pair<Predicate<HttpServletRequest>, Controller>> ctrls) {
		return dispatcher(ctrls, Controllers::defaultNotFoundTranslator, Controllers::defaultErrorTranslator);
	}

	private static String defaultNotFoundTranslator(HttpServletRequest req) {
		return "Not found";
	}

	private static String defaultErrorTranslator(HttpServletRequest req, Exception e) {
		return Optional.ofNullable(e.getMessage()).orElse("Error");
	}

	public static Controller textPlain(String body) {
		return req -> resp -> ResponseWriters.header("Content-Type", "text/plain")
				.andThen(ResponseWriters.body(body))
				.accept(resp);
	}

	public static Controller textPlain(Charset charset, String body) {
		return req -> resp -> ResponseWriters.header("Content-Type", "text/plain;charset=" + charset.name())
				.andThen(ResponseWriters.body(body))
				.accept(resp);
	}

	public static Controller notFound(Function<? super HttpServletRequest, ? extends String> f) {
		return req -> resp -> ResponseWriters.notFound()
				.andThen(ResponseWriters.body(f.apply(req)))
				.accept(resp);
	}

	public static Controller notFound() {
		return notFound(r -> "Not found");
	}

	public static Controller badReqest(Function<? super HttpServletRequest, ? extends String> f) {
		return req -> resp -> ResponseWriters.badRequest()
				.andThen(ResponseWriters.body(f.apply(req)))
				.accept(resp);
	}

	public static Controller badReqest(String reason) {
		return badReqest(r -> reason);
	}

	public static Controller internalError(Function<? super HttpServletRequest, ? extends String> f) {
		return req -> resp -> ResponseWriters.internalError()
				.andThen(ResponseWriters.body(f.apply(req)))
				.accept(resp);
	}

	public static Controller internalError(String reason) {
		return internalError(r -> reason);
	}

	public static Controller internalError(BiFunction<? super HttpServletRequest, ? super Exception, ? extends String> f, Exception e) {
		return req -> resp -> ResponseWriters.internalError()
				.andThen(ResponseWriters.body(f.apply(req, e)))
				.accept(resp);
	}

}
