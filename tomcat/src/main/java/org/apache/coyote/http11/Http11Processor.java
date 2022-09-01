package org.apache.coyote.http11;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import nextstep.jwp.exception.UncheckedServletException;
import org.apache.coyote.Processor;
import org.apache.coyote.http11.request.HttpRequest;
import org.apache.coyote.http11.request.HttpRequestProcessor;
import org.apache.coyote.http11.request.ResourceRequestProcessor;
import org.apache.coyote.http11.request.RootRequestProcessor;
import org.apache.coyote.http11.response.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Http11Processor implements Runnable, Processor {

    private static final Logger log = LoggerFactory.getLogger(Http11Processor.class);

    private final Socket connection;
    private final Map<String, HttpRequestProcessor> requestMappings = new ConcurrentHashMap<>();

    public Http11Processor(final Socket connection) {
        this.connection = connection;
        initRequestMappings();
    }

    private void initRequestMappings() {
        requestMappings.put("root", new RootRequestProcessor());
        requestMappings.put("resource", new ResourceRequestProcessor());
    }

    @Override
    public void run() {
        process(connection);
    }

    @Override
    public void process(final Socket connection) {
        try (final var inputStream = connection.getInputStream();
             final var outputStream = connection.getOutputStream()) {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

            List<String> request = new ArrayList<>();
            while (bufferedReader.ready()) {
                request.add(bufferedReader.readLine());
            }

            HttpRequest httpRequest = new HttpRequest(request);
            HttpRequestProcessor requestProcessor = getMatchedRequestProcessor(httpRequest.getRequestURI());
            HttpResponse httpResponse = requestProcessor.process(httpRequest);
            outputStream.write(httpResponse.getBytes());
            outputStream.flush();

        } catch (IOException | UncheckedServletException e) {
            log.error(e.getMessage(), e);
        }
    }

    private HttpRequestProcessor getMatchedRequestProcessor(String requestURI) {
        if ("/".equals(requestURI)) {
            return requestMappings.get("root");
        }
        return requestMappings.get("resource");
    }
}
