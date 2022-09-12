package org.apache.catalina.connector;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import nextstep.jwp.controller.LoginController;
import nextstep.jwp.controller.RegisterController;
import nextstep.jwp.controller.RootController;
import nextstep.jwp.controller.StaticResourceController;
import org.apache.catalina.servlets.RequestMappings;
import org.apache.coyote.WebConfig;
import org.apache.coyote.http11.Http11Processor;
import org.apache.coyote.http11.ResourceLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Connector implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(Connector.class);

    private static final int DEFAULT_PORT = 8080;
    private static final int DEFAULT_ACCEPT_COUNT = 100;
    private static final int DEFAULT_MAX_THREAD = 250;

    private final ServerSocket serverSocket;
    private ExecutorService executorService;
    private boolean stopped;

    public Connector() {
        this(DEFAULT_PORT, DEFAULT_ACCEPT_COUNT, DEFAULT_MAX_THREAD);
    }

    public Connector(final int port, final int acceptCount, final int maxThread) {
        this.serverSocket = createServerSocket(port, acceptCount);
        this.stopped = false;
        this.executorService = Executors.newFixedThreadPool(maxThread);
    }

    private ServerSocket createServerSocket(final int port, final int acceptCount) {
        try {
            final int checkedPort = checkPort(port);
            final int checkedAcceptCount = checkAcceptCount(acceptCount);
            return new ServerSocket(checkedPort, checkedAcceptCount);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void start() {
        var thread = new Thread(this);
        thread.setDaemon(true);
        thread.start();
        stopped = false;
    }

    @Override
    public void run() {
        while (!stopped) {
            connect();
        }
    }

    private void connect() {
        try {
            process(serverSocket.accept());
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }

    private void process(final Socket connection) {
        if (connection == null) {
            return;
        }
        log.info("connect host: {}, port: {}", connection.getInetAddress(), connection.getPort());
        ResourceLocator resourceLocator = new ResourceLocator("/static");
        RequestMappings requestMappings = new RequestMappings(
                List.of(new LoginController(resourceLocator), new RootController(resourceLocator),
                        new StaticResourceController(resourceLocator), new RegisterController(resourceLocator)));
        WebConfig webConfig = new WebConfig(resourceLocator, requestMappings);
        var processor = new Http11Processor(connection, webConfig);
        executorService.submit(processor);
    }

    public void stop() {
        stopped = true;
        try {
            serverSocket.close();
            executorService.shutdown();
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }

    private int checkPort(final int port) {
        final var MIN_PORT = 1;
        final var MAX_PORT = 65535;

        if (port < MIN_PORT || MAX_PORT < port) {
            return DEFAULT_PORT;
        }
        return port;
    }

    private int checkAcceptCount(final int acceptCount) {
        return Math.max(acceptCount, DEFAULT_ACCEPT_COUNT);
    }
}
