package net.fj.ctrl;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import io.atlassian.fugue.Pair;

@FunctionalInterface
public interface Controller extends Function<HttpServletRequest, Consumer<HttpServletResponse>> {

	static <A> Controller of(
			Function<? super A, ? extends Consumer<HttpServletResponse>> requestWriter,
			Function<? super HttpServletRequest, ? extends A> requestReader) {
		return req -> resp -> requestReader.andThen(requestWriter).apply(req).accept(resp);
	}

	static <A> BiFunction<Function<A, Consumer<HttpServletResponse>>, Function<HttpServletRequest, A>, Controller> safe(
			Function<? super RuntimeException, ? extends Controller> exceptionWriter) {
		return (rw, rr) -> req -> resp -> {
			try {
				of(rw, rr).apply(req).accept(resp);
			} catch (RuntimeException e) {
				exceptionWriter.apply(e).apply(req).accept(resp);
			}
		};
	}

	static Controller dispatcher(
			Controller notFound,
			List<Pair<Predicate<HttpServletRequest>, Controller>> controllers) {
		return req -> resp -> controllers.stream()
				.filter(p -> p.left().test(req))
				.findFirst()
				.map(Pair::right)
				.orElseGet(() -> notFound)
				.apply(req)
				.accept(resp);
	}

	static BiFunction<Controller, List<Pair<Predicate<HttpServletRequest>, Controller>>, Controller> safeDispatcher(
			Function<? super RuntimeException, ? extends Controller> exceptionWriterw) {
		return (nf, ctrls) -> req -> resp -> {
			try {
				dispatcher(nf, ctrls).apply(req).accept(resp);
			} catch (RuntimeException e) {
				exceptionWriterw.apply(e).apply(req).accept(resp);
			}
		};
	}

}
