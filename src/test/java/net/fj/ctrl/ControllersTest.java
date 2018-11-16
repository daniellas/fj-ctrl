package net.fj.ctrl;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.eclipse.jetty.server.Server;
import org.junit.Test;

public class ControllersTest {

	@Test
	public void shouldHandleViaMethodRef() throws Exception {
		Server server = Jetty.createServer(ControllersTest::helloCtrl);
		CloseableHttpResponse resp = HttpClients.createDefault().execute(new HttpGet("http://localhost:8080"));

		assertThat(resp.getStatusLine().getStatusCode()).isEqualTo(HttpServletResponse.SC_OK);
		assertThat(EntityUtils.toString(resp.getEntity())).isEqualTo("Hello!");

		server.stop();
	}

	@Test
	public void shoudHandleViaFunctions() throws Exception {
		Server server = Jetty.createServer(Controller.of(plainTextWriter.compose(ControllersTest::echo), RequestReaders::pathInfo));
		CloseableHttpResponse resp = HttpClients.createDefault().execute(new HttpGet("http://localhost:8080/index.html"));

		assertThat(resp.getStatusLine().getStatusCode()).isEqualTo(HttpServletResponse.SC_OK);
		assertThat(EntityUtils.toString(resp.getEntity())).isEqualTo("Echo /index.html");

		server.stop();
	}

	@Test
	public void shoudCatchError() throws Exception {
		Server server = Jetty.createServer(
				Controller.<String>safe(errorWriter).apply(plainTextWriter.compose(ControllersTest::echo), RequestReaders::pathInfo));
		CloseableHttpResponse resp = HttpClients.createDefault().execute(new HttpGet("http://localhost:8080/error"));

		assertThat(resp.getStatusLine().getStatusCode()).isEqualTo(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		assertThat(EntityUtils.toString(resp.getEntity())).isEqualTo("Illegal argument /error");

		server.stop();
	}

	private static Consumer<HttpServletResponse> helloCtrl(HttpServletRequest req) {
		return ResponseWriters.status(HttpServletResponse.SC_OK)
				.andThen(ResponseWriters.body("Hello!"));
	}

	private static String echo(String v) {
		if ("/error".equals(v)) {
			throw new IllegalArgumentException("Illegal argument " + v);
		}

		return "Echo " + v;
	}

	private final Function<String, Consumer<HttpServletResponse>> plainTextWriter = b -> ResponseWriters.status(HttpServletResponse.SC_OK)
			.andThen(ResponseWriters.header(Headers.CONTENT_TYPE, MediaTypes.TEXT_PLAIN))
			.andThen(ResponseWriters.body(b));

	private static Function<RuntimeException, Controller> errorWriter = e -> Controller.of(
			resp -> ResponseWriters.status(HttpServletResponse.SC_INTERNAL_SERVER_ERROR)
					.andThen(ResponseWriters.header(Headers.CONTENT_TYPE, MediaTypes.TEXT_PLAIN))
					.andThen(ResponseWriters.body(Optional.ofNullable(e.getMessage()).orElse("Error"))),
			req -> null);

}
