package net.fj.ctrl;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DispatcherServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	private final Controller ctrl;

	@Override
	protected void service(HttpServletRequest req, HttpServletResponse resp) {
		ctrl.apply(req).accept(resp);
	}
}
