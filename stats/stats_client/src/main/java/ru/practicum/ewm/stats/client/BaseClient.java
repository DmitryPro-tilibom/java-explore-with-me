package ru.practicum.ewm.stats.client;

import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

public abstract class BaseClient {
    public final RestTemplate rest;

    public BaseClient() {
        this.rest = new RestTemplate();
        HttpClient httpClient = HttpClientBuilder.create().build();
        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory(httpClient);
        rest.setRequestFactory(requestFactory);
    }

    public ResponseEntity<Object> post(String path, Object body) {
        try {
            ResponseEntity<Object> response = rest.postForEntity(path, body, Object.class);
            return buildResponse(response);
        } catch (HttpStatusCodeException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsByteArray());
        }
    }

    public ResponseEntity<Object> get(String path) {
        try {
            ResponseEntity<Object> response = rest.exchange(path, HttpMethod.GET, null, Object.class);
            return buildResponse(response);
        } catch (HttpStatusCodeException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsByteArray());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    private ResponseEntity<Object> buildResponse(ResponseEntity<Object> response) {
        ResponseEntity.BodyBuilder responseBuilder = ResponseEntity.status(response.getStatusCode());
        if (response.hasBody()) {
            return responseBuilder.body(response.getBody());
        }
        return responseBuilder.build();
    }
}