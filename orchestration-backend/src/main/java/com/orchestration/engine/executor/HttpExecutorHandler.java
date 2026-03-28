package com.orchestration.engine.executor;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.flowable.engine.delegate.DelegateExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Built-in executor: makes HTTP calls.
 * Config JSON: {"url": "...", "method": "GET|POST|PUT|DELETE", "headers": {...}, "body": "..."}
 */
@Component
public class HttpExecutorHandler implements ExecutorHandler {

    private static final Logger log = LoggerFactory.getLogger(HttpExecutorHandler.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    @Override
    public String getType() {
        return "HTTP";
    }

    @Override
    public Object execute(DelegateExecution execution, String config) throws Exception {
        JsonNode configNode = objectMapper.readTree(config);
        String url = configNode.path("url").asText();
        String method = configNode.path("method").asText("GET");
        String body = configNode.has("body") ? configNode.get("body").toString() : "";

        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(30));

        // Add headers
        if (configNode.has("headers")) {
            configNode.get("headers").fields().forEachRemaining(entry ->
                    requestBuilder.header(entry.getKey(), entry.getValue().asText()));
        }

        HttpRequest request = switch (method.toUpperCase()) {
            case "POST" -> requestBuilder.POST(HttpRequest.BodyPublishers.ofString(body)).build();
            case "PUT" -> requestBuilder.PUT(HttpRequest.BodyPublishers.ofString(body)).build();
            case "DELETE" -> requestBuilder.DELETE().build();
            default -> requestBuilder.GET().build();
        };

        log.info("[HttpExecutor] {} {}", method, url);
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() >= 400) {
            throw new RuntimeException("HTTP request failed with status " + response.statusCode()
                    + ": " + response.body());
        }

        return response.body();
    }
}
