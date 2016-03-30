package org.apache.http.examples.server;

//import java.util.concurrent.TimeUnit;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.concurrent.TimeUnit;

import org.apache.http.ConnectionClosedException;
import org.apache.http.ExceptionLogger;
import org.apache.http.Header;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.HttpStatus;
import org.apache.http.config.SocketConfig;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.bootstrap.HttpServer;
import org.apache.http.impl.bootstrap.ServerBootstrap;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpProcessor;
import org.apache.http.protocol.HttpProcessorBuilder;
import org.apache.http.protocol.HttpRequestHandler;
import org.apache.http.protocol.ResponseConnControl;
import org.apache.http.protocol.ResponseContent;
import org.apache.http.protocol.ResponseDate;
import org.apache.http.protocol.UriHttpRequestHandlerMapper;

public class TestHttpServer {

	public static volatile int num = 0;

	public static void main(String[] args) throws Exception {

		final HttpServer server = getServer();
		server.start();

		Thread.sleep(60000);
		server.shutdown(1, TimeUnit.SECONDS);
		System.out.println("Server1 shut down");

		// Keep the JVM alaive for 1 minute more
		Thread.sleep(60000);
		System.out.println("Server2 start");
		final HttpServer server2 = getServer();
		server2.start();
		Thread.sleep(60000);
		server2.shutdown(1, TimeUnit.SECONDS);
		System.out.println("Server2 shut down");
		System.out.println("End main thread!");
	}

	static HttpServer getServer() {
		HttpRequestHandler requestHandlerOK = new OkResponse();

		UriHttpRequestHandlerMapper handle_map = new UriHttpRequestHandlerMapper();
		handle_map.register("*", requestHandlerOK);

		HttpProcessor httpProcessor = HttpProcessorBuilder.create()
				.add(new ResponseDate()).add(new ResponseContent())
				.add(new ResponseConnControl()).build();

		SocketConfig socketConfig = SocketConfig.custom().setSoTimeout(180000)
				.setTcpNoDelay(true).build();

		System.out.println("Default socket config is:" + socketConfig);
		return ServerBootstrap.bootstrap()
				// .setConnectionFactory(connectionFactory)
				.setListenerPort(9090).setHttpProcessor(httpProcessor)
				// .setSocketConfig(socketConfig)
				.setExceptionLogger(new StdErrorExceptionLogger())
				.setHandlerMapper(handle_map).create();
	}

	static class OkResponse implements HttpRequestHandler {
		public void handle(HttpRequest request, HttpResponse response,
				HttpContext context) throws HttpException, IOException {

			response.setStatusCode(HttpStatus.SC_OK);
			response.setEntity(new StringEntity("some important message"
					+ (num++), ContentType.TEXT_PLAIN));
			System.out.println("Response is " + response);
		}
	}

	static class StdErrorExceptionLogger implements ExceptionLogger {

		@Override
		public void log(final Exception ex) {
			if (ex instanceof SocketTimeoutException) {
				System.err.println("Connection timed out");
			} else if (ex instanceof ConnectionClosedException) {
				System.err.println(ex.getMessage());
			} else {
				ex.printStackTrace();
			}
		}

	}

}
