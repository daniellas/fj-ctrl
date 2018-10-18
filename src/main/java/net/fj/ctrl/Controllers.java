package net.fj.ctrl;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import io.atlassian.fugue.Pair;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Controllers {

	public static Controller dispatcher(
			Controller notFound,
			Function<? super Exception, ? extends Controller> error,
			List<Pair<Predicate<HttpServletRequest>, Controller>> ctrls) {
		return req -> resp -> {
			try {
				ctrls.stream()
						.filter(p -> p.left().test(req))
						.findFirst()
						.map(Pair::right)
						.orElseGet(() -> notFound)
						.apply(req)
						.accept(resp);
			} catch (Exception e) {
				error.apply(e).apply(req).accept(resp);
			}
		};
	}

	public static Controller dispatcher(List<Pair<Predicate<HttpServletRequest>, Controller>> ctrls) {
		return dispatcher(
				Controllers.of(
						ResponseWriters.scNotFound(),
						Function.<HttpServletRequest>identity()
								.andThen(RequestReaders::pathInfo)
								.andThen(p -> p + " not found")),
				ex -> Controllers.of(
						ResponseWriters.scInternalError(),
						Function.<HttpServletRequest>identity()
								.andThen(r -> Optional.ofNullable(ex.getMessage()).orElse("Internal server error"))),
				ctrls);
	}

	public static Controller of(Consumer<HttpServletResponse> rw) {
		return req -> resp -> rw.accept(resp);

	}

	public static Controller of(Consumer<HttpServletResponse> rw, Function<? super HttpServletRequest, ? extends String> b) {
		return req -> resp -> rw.andThen(ResponseWriters.body(b.apply(req))).accept(resp);

	}

	public static Controller of(
			Function<? super Exception, ? extends Consumer<HttpServletResponse>> ew,
			Consumer<HttpServletResponse> rw,
			Function<? super HttpServletRequest, ? extends String> b) {
		return req -> resp -> {
			try {
				rw.andThen(ResponseWriters.body(b.apply(req))).accept(resp);
			} catch (Exception e) {
				ew.apply(e).accept(resp);
			}
		};

	}

}
