package io.github.fhv5.finsight.config;

import org.jspecify.annotations.NonNull;
import org.springframework.web.accept.ApiVersionParser;

public class CustomApiVersionParser implements ApiVersionParser<String> {

    @Override
    public String parseVersion(@NonNull String version) {

        if("api-docs".equals(version) || "index.html".equals(version)
                || "swagger-ui-bundle.js".equals(version)
                || "swagger-ui.css".equals(version)
                || "index.css".equals(version)
                || "swagger-ui-standalone-preset.js".equals(version)
                || "favicon-32x32.png".equals(version)
                || "favicon-16x16.png".equals(version)
                || "swagger-initializer.js".equals(version))
            return null;

        if (version.startsWith("v") || version.startsWith("V")) {
            version = version.substring(1);
        }

        return version;
    }
}