package org.apache.coyote.http11.request;

import org.apache.coyote.http11.response.HttpResponse;
import org.apache.coyote.http11.response.spec.HttpStatus;

public class HttpRequestHandler {

    private final ResourceLocator resourceLocator;

    public HttpRequestHandler(ResourceLocator resourceLocator) {
        this.resourceLocator = resourceLocator;
    }

    public HttpResponse process(HttpRequest request) {
        String path = request.getPathString();
        try {
            Resource resource = resourceLocator.locate(path);
            return new HttpResponse(
                    HttpStatus.OK,
                    resource.getMimeType(),
                    resource.getData());
        } catch (IllegalArgumentException e) {
            Resource resource = resourceLocator.locate("/404.html");
            return new HttpResponse(
                    HttpStatus.NOT_FOUND,
                    resource.getMimeType(),
                    resource.getData()
            );
        }
    }
}
