package org.apache.coyote.http11.request;

import java.util.Map;
import org.apache.coyote.http11.request.spec.HttpHeaders;
import org.apache.coyote.http11.request.spec.StartLine;

public class HttpRequest {

    private final StartLine startLine;
    private final HttpHeaders headers;

    public HttpRequest(StartLine startLine, HttpHeaders headers) {
        this.startLine = startLine;
        this.headers = headers;
    }

    public String getPathString() {
        return startLine.getPath();
    }

    public Map<String, String> getParams() {
        return startLine.getParams();
    }

    public void setPath(String pathName) {
        startLine.setPath(pathName);
    }
}
