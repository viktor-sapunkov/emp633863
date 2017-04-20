package com.infobip.urlshortener;

import com.infobip.urlshortener.controller.AccountController;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.filter.DelegatingFilterProxy;
import org.springframework.web.servlet.DispatcherServlet;

import javax.servlet.DispatcherType;
import java.io.IOException;
import java.util.EnumSet;

import static org.springframework.security.web.context.AbstractSecurityWebApplicationInitializer.DEFAULT_FILTER_NAME;

/**
 * an HTTP service that serves to shorten URLs, with the following functionalities:
 * - Registration Web address (API)
 *
 * - Redirect client in accordance with the shortened URL
 * - Usage Statistics (API)
 */
public class URLShortenerApplication {
    public static final char[] ALPHANUMERIC = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".toCharArray();

    private static final Logger LOGGER = LoggerFactory.getLogger(URLShortenerApplication.class);

    private static final int DEFAULT_PORT = 8088;

    private static final String CONTEXT_PATH = "/";
    private static final String CONFIG_LOCATION_PACKAGE = "com.infobip.urlshortener.config";
    private static final String MAPPING_URL = "/";
    private static final String DEFAULT_PROFILE = "dev";
    private static final String WEBAPP_DIRECTORY = "webapp";

    public static void main(String[] args) throws Exception {
        new URLShortenerApplication().startJetty(getPortFromArgs(args));
    }

    private static int getPortFromArgs(String... args) {
        int value = DEFAULT_PORT;
        if (args.length > 0) {
            try {
                value = Integer.valueOf(args[0]);
            } catch (NumberFormatException ignored) {
            }

            if (value < 1 || value > 65535) {
                throw new IllegalArgumentException(String.format(
                        "Port %d does not look sane (out of bounds [1..65535])",
                        value
                ));
            }
        }
        return value;
    }

    private void startJetty(int port) throws Exception {
        LOGGER.debug("Starting server at port {}", port);
        server = new Server(port);

        setServerHandlerAsServletContextHandler();
        addRuntimeShutdownHook(server);

        server.start();
        LOGGER.info("Server started at port {}", port);

        server.join();
    }

    private void setServerHandlerAsServletContextHandler() throws IOException {
        ServletContextHandler contextHandler = new ServletContextHandler();

        // this one went tricky..
        contextHandler.addFilter(new FilterHolder(
                new DelegatingFilterProxy(DEFAULT_FILTER_NAME)), "/*", EnumSet.allOf(DispatcherType.class)
        );

        contextHandler.setErrorHandler(null);

        contextHandler.setResourceBase(String.valueOf(new ClassPathResource(WEBAPP_DIRECTORY).getURI()));
        contextHandler.setContextPath(CONTEXT_PATH);

        contextHandler.setClassLoader(Thread.currentThread().getContextClassLoader());

        WebApplicationContext webAppContext = getWebApplicationContext();
        DispatcherServlet dispatcherServlet = new DispatcherServlet(webAppContext);
        ServletHolder springServletHolder = new ServletHolder("mvc-dispatcher", dispatcherServlet);
        contextHandler.addServlet(springServletHolder, MAPPING_URL);
        contextHandler.addEventListener(new ContextLoaderListener(webAppContext));

        server.setHandler(contextHandler);
    }

    private WebApplicationContext getWebApplicationContext() {
        AnnotationConfigWebApplicationContext context = new AnnotationConfigWebApplicationContext();
        context.register(AccountController.class);
        context.setConfigLocation(CONFIG_LOCATION_PACKAGE);
        context.getEnvironment().setDefaultProfiles(DEFAULT_PROFILE);
        return context;
    }

    private void addRuntimeShutdownHook(final Server server) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (server.isStarted()) {
                server.setStopAtShutdown(true);

                try {
                    server.stop();
                } catch (Exception e) {
                    System.out.println("Error while stopping jetty server: " + e.getMessage());
                    LOGGER.error("Error while stopping jetty server: " + e.getMessage(), e);
                }
            }
        }));
    }

    private Server server;
}
