package net.fj.ctrl;

import java.util.function.Function;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Jetty {

	public static Server createServer(Controller ctrl) {
		return Function.<Integer>identity()
				.andThen(Server::new)
				.andThen(addController(ctrl))
				.andThen(Jetty::start)
				.apply(8080);
	}

	public static Function<Server, Server> addController(Controller ctrl) {
		return s -> {
			ServletHandler h = new ServletHandler();

			h.addServletWithMapping(new ServletHolder(Servlets.of(ctrl)), "/*");

			s.setHandler(h);

			return s;
		};
	}

	public static Server start(Server s) {
		try {
			s.start();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		return s;
	}

}
