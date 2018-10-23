package net.fj.ctrl;

import java.util.List;
import java.util.function.BiFunction;
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

	public interface RequestReader<A> extends Function<HttpServletRequest, A> {

	}

	public interface ResponseWriter<A> extends Function<A, Consumer<HttpServletResponse>> {

	}

	public interface ErrorWriter extends Function<RuntimeException, Controller> {

	}

	public static <A> Controller of(Function<? super A, ? extends Consumer<HttpServletResponse>> rw, Function<? super HttpServletRequest, ? extends A> rr) {
		return req -> resp -> rr.andThen(rw).apply(req).accept(resp);
	}

	public static <A> BiFunction<ResponseWriter<A>, RequestReader<A>, Controller> of(ErrorWriter ew) {
		return (rw, rr) -> req -> resp -> {
			try {
				of(rw, rr).apply(req).accept(resp);
			} catch (RuntimeException e) {
				ew.apply(e).apply(req).accept(resp);
			}
		};
	}

	public static Controller dispatcher(
			Controller notFound,
			Function<? super RuntimeException, ? extends Controller> error,
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
			} catch (RuntimeException e) {
				error.apply(e).apply(req).accept(resp);
			}
		};
	}

}
