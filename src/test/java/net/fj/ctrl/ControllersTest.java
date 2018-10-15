package net.fj.ctrl;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.function.Function;
import java.util.function.Predicate;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.junit.Test;

import io.atlassian.fugue.Pair;

public class ControllersTest {

	private static final Predicate<HttpServletRequest> any = r -> true;

	@Test
	public void shouldNotFindOnEmptyDispatcher() throws Exception {
		Server server = createServer(Controllers.dispatcher(Collections.emptyList()));
		CloseableHttpResponse resp = HttpClients.createDefault().execute(new HttpGet("http://localhost:8080"));

		assertThat(resp.getStatusLine().getStatusCode()).isEqualTo(HttpServletResponse.SC_NOT_FOUND);

		server.stop();

	}

	@Test
	public void shouldReturnOk() throws Exception {
		Server server = createServer(Controllers.dispatcher(Arrays.asList(
				Pair.pair(any, Controllers.textPlain("Response")))));
		CloseableHttpResponse resp = HttpClients.createDefault().execute(new HttpGet("http://localhost:8080"));

		assertThat(resp.getStatusLine().getStatusCode()).isEqualTo(HttpServletResponse.SC_OK);

		server.stop();

	}

	@Test
	public void shouldReturnInternalError() throws Exception {
		Server server = createServer(Controllers.dispatcher(Arrays.asList(
				Pair.pair(any, Controllers.internalError("Error")))));
		CloseableHttpResponse resp = HttpClients.createDefault().execute(new HttpGet("http://localhost:8080"));

		assertThat(resp.getStatusLine().getStatusCode()).isEqualTo(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

		server.stop();

	}

	@Test
	public void shouldReturnBadRequest() throws Exception {
		Server server = createServer(Controllers.dispatcher(Arrays.asList(
				Pair.pair(any, Controllers.badReqest("Bad request")))));
		CloseableHttpResponse resp = HttpClients.createDefault().execute(new HttpGet("http://localhost:8080"));

		assertThat(resp.getStatusLine().getStatusCode()).isEqualTo(HttpServletResponse.SC_BAD_REQUEST);

		server.stop();

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

			h.addServletWithMapping(new ServletHolder(new HttpServlet() {
				private static final long serialVersionUID = 1L;

				@Override
				protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
					ctrl.apply(req).accept(resp);
				}
			}), "/*");

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
