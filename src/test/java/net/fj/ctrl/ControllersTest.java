package net.fj.ctrl;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.Collections;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.junit.Test;

import io.atlassian.fugue.Pair;

public class ControllersTest {

	private static final Predicate<HttpServletRequest> any = r -> true;

	private static final Controller ok = Controllers.of(
			ResponseWriters.scOk()
					.andThen(ResponseWriters.body("OK")));

	private static final Controller error = Controllers.of(
			ResponseWriters.scInternalError()
					.andThen(ResponseWriters.body("Error")));

	private static final Controller badRequest = Controllers.of(
			ResponseWriters.scBadRequest()
					.andThen(ResponseWriters.body("Bad request")));

	private static Function<HttpServletRequest, String> runtimeFailure = req -> {
		throw new RuntimeException("Runtime error");
	};

	private static Function<HttpServletRequest, String> illegalArgFailure = req -> {
		throw new IllegalArgumentException("Illegal argument");
	};

	@Test
	public void shouldNotFindOnEmptyDispatcher() throws Exception {
		Server server = createServer(Controllers.dispatcher(Collections.emptyList()));
		CloseableHttpResponse resp = HttpClients.createDefault().execute(new HttpGet("http://localhost:8080"));

		assertThat(resp.getStatusLine().getStatusCode()).isEqualTo(HttpServletResponse.SC_NOT_FOUND);
		assertThat(EntityUtils.toString(resp.getEntity())).isEqualTo("/ not found");

		server.stop();

	}

	@Test
	public void shouldReturnOk() throws Exception {
		Server server = createServer(Controllers.dispatcher(Arrays.asList(Pair.pair(any, ok))));
		CloseableHttpResponse resp = HttpClients.createDefault().execute(new HttpGet("http://localhost:8080"));

		assertThat(resp.getStatusLine().getStatusCode()).isEqualTo(HttpServletResponse.SC_OK);
		assertThat(EntityUtils.toString(resp.getEntity())).isEqualTo("OK");

		server.stop();

	}

	@Test
	public void shouldReturnInternalError() throws Exception {
		Server server = createServer(Controllers.dispatcher(Arrays.asList(Pair.pair(any, error))));
		CloseableHttpResponse resp = HttpClients.createDefault().execute(new HttpGet("http://localhost:8080"));

		assertThat(resp.getStatusLine().getStatusCode()).isEqualTo(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		assertThat(EntityUtils.toString(resp.getEntity())).isEqualTo("Error");

		server.stop();

	}

	@Test
	public void shouldReturnBadRequest() throws Exception {
		Server server = createServer(Controllers.dispatcher(Arrays.asList(Pair.pair(any, badRequest))));
		CloseableHttpResponse resp = HttpClients.createDefault().execute(new HttpGet("http://localhost:8080"));

		assertThat(resp.getStatusLine().getStatusCode()).isEqualTo(HttpServletResponse.SC_BAD_REQUEST);
		assertThat(EntityUtils.toString(resp.getEntity())).isEqualTo("Bad request");

		server.stop();

	}

	@Test
	public void shouldReturnInternalErrorOnFailure() throws Exception {
		Server server = createServer(
				Controllers.dispatcher(Arrays.asList(Pair.pair(any, Controllers.of(ResponseWriters.scOk(), runtimeFailure)))));
		CloseableHttpResponse resp = HttpClients.createDefault().execute(new HttpGet("http://localhost:8080"));

		assertThat(resp.getStatusLine().getStatusCode()).isEqualTo(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		assertThat(EntityUtils.toString(resp.getEntity())).isEqualTo("Runtime error");

		server.stop();

	}

	@Test
	public void shouldHandleException() throws Exception {
		Server server = createServer(Controllers.dispatcher(
				Arrays.asList(Pair.pair(any, Controllers.of(ControllersTest::errorHandler, ResponseWriters.scOk(), illegalArgFailure)))));
		CloseableHttpResponse resp = HttpClients.createDefault().execute(new HttpGet("http://localhost:8080"));

		assertThat(resp.getStatusLine().getStatusCode()).isEqualTo(HttpServletResponse.SC_BAD_REQUEST);
		assertThat(EntityUtils.toString(resp.getEntity())).isEqualTo("Illegal argument");

		server.stop();
	}

	@Test
	public void shouldHandleExceptionInDispatcher() throws Exception {
		Server server = createServer(
				Controllers.dispatcher(Arrays
						.asList(Pair.pair(any, Controllers.of(ControllersTest::errorHandler, ResponseWriters.scOk(), runtimeFailure)))));
		CloseableHttpResponse resp = HttpClients.createDefault().execute(new HttpGet("http://localhost:8080"));

		assertThat(resp.getStatusLine().getStatusCode()).isEqualTo(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		assertThat(EntityUtils.toString(resp.getEntity())).isEqualTo("java.lang.RuntimeException: Runtime error");

		server.stop();
	}

	private static Consumer<HttpServletResponse> errorHandler(Exception e) {
		if (e instanceof IllegalArgumentException) {
			return ResponseWriters.scBadRequest().andThen(ResponseWriters.body("Illegal argument"));
		}

		throw new RuntimeException(e);
	}

	private static Server createServer(Controller ctrl) {
		return Function.<Integer>identity()
				.andThen(Server::new)
				.andThen(addController(ctrl))
				.andThen(ControllersTest::start)
				.apply(8080);
	}

	private static Function<Server, Server> addController(Controller ctrl) {
		return s -> {
			ServletHandler h = new ServletHandler();

			h.addServletWithMapping(new ServletHolder(Servlets.of(ctrl)), "/*");

			s.setHandler(h);

			return s;
		};
	}

	private static Server start(Server s) {
		try {
			s.start();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		return s;
	}

}
