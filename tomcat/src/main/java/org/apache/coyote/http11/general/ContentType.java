package org.apache.coyote.http11.general;

import java.util.Arrays;

public enum ContentType {

    HTML("text/html"),
    CSS("text/css"),
    JS("text/javascript"),
    SVG("image/svg+xml"),
    APPLICATION_FORM_URLENCODED("application/x-www-form-urlencoded"),
    APPLICATION_JSON("application/json");

    private final String value;

    ContentType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static ContentType of(String type) {
        return Arrays.stream(values())
                .filter(contentType -> contentType.value.equalsIgnoreCase(type))
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException("지원하지 않는 mime type = " + type));
    }
}
