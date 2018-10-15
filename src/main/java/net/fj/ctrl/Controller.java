package net.fj.ctrl;

import java.util.function.Consumer;
import java.util.function.Function;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@FunctionalInterface
public interface Controller extends Function<HttpServletRequest, Consumer<HttpServletResponse>> {

}
