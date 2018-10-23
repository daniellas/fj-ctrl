package net.fj.ctrl;

import static org.assertj.core.api.Assertions.assertThat;

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

import net.fj.ctrl.Controllers.ResponseWriter;

public class ControllersTest {

	@Test
	public void shouldHandleViaMethodRefCtrl() throws Exception {
		Server server = Jetty.createServer(ControllersTest::helloCtrl);
		CloseableHttpResponse resp = HttpClients.createDefault().execute(new HttpGet("http://localhost:8080"));

		assertThat(resp.getStatusLine().getStatusCode()).isEqualTo(HttpServletResponse.SC_OK);
		assertThat(EntityUtils.toString(resp.getEntity())).isEqualTo("Hello!");

		server.stop();
	}

	@Test
	public void shoudHandleViaFunctions() throws Exception {
		Function<String, Consumer<HttpServletResponse>> writer = ControllersTest::writeTextPlain;
		Server server = Jetty.createServer(Controllers.of(writer.compose(ControllersTest::echo), RequestReaders::pathInfo));
		CloseableHttpResponse resp = HttpClients.createDefault().execute(new HttpGet("http://localhost:8080/index.html"));

		assertThat(resp.getStatusLine().getStatusCode()).isEqualTo(HttpServletResponse.SC_OK);
		assertThat(EntityUtils.toString(resp.getEntity())).isEqualTo("Echo /index.html");

		server.stop();
	}

	private static Consumer<HttpServletResponse> helloCtrl(HttpServletRequest req) {
		return ResponseWriters.status(HttpServletResponse.SC_OK)
				.andThen(ResponseWriters.body("Hello!"));
	}

	private static String echo(String v) {
		return "Echo " + v;
	}

	private static Consumer<HttpServletResponse> writeTextPlain(String body) {
		return ResponseWriters.status(HttpServletResponse.SC_OK)
				.andThen(ResponseWriters.header("Content-Type", "text/plain"))
				.andThen(ResponseWriters.body(body));
	}

}
