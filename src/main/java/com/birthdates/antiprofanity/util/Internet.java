package com.birthdates.antiprofanity.util;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Internet implementation
 */
public class Internet {

    /**
     * Send a GET request to a URL
     *
     * @param urlString Target URL
     * @return A {@link String} response from the URL
     */
    public static String sendRequest(String urlString) {
        return sendRequest(urlString, "GET");
    }

    /**
     * Send a method request to a URL
     *
     * @param urlString Target URL
     * @param method    Target method (GET, POST, OPTIONS, PUT, DELETE, e.t.c)
     * @return A {@link String} response from the URL
     */
    public static String sendRequest(String urlString, String method) {
        return sendRequest(urlString, method, null);
    }

    /**
     * Send a method request to a URL with certain headers
     *
     * @param urlString Target URL
     * @param method    Target method (GET, POST, OPTIONS, PUT, DELETE, e.t.c)
     * @param headers   A {@link Map} of headers
     * @return A {@link String} response from the URL
     */
    public static String sendRequest(String urlString, String method, Map<String, String> headers) {
        return sendRequest(urlString, method, headers, null);
    }

    /**
     * Send a method request to a URL with certain headers & body
     *
     * @param urlString Target URL
     * @param method    Target method (GET, POST, OPTIONS, PUT, DELETE, e.t.c)
     * @param headers   A {@link Map} of headers
     * @param body      The body of the request
     * @return A {@link String} response from the URL
     */
    public static String sendRequest(String urlString, String method, Map<String, String> headers, String body) {
        return new String(sendRequest(urlString, method, headers, body, null), StandardCharsets.UTF_8);
    }

    /**
     * Send a method request to a URL with certain headers, body, & content type
     *
     * @param urlString   Target URL
     * @param method      Target method (GET, POST, OPTIONS, PUT, DELETE, e.t.c)
     * @param headers     A {@link Map} of headers
     * @param body        The body of the request
     * @param contentType Content type (application/json, e.t.c)
     * @return A {@link String} response from the URL
     */
    public static byte[] sendRequest(String urlString, String method, Map<String, String> headers, String body, String contentType) {
        byte[] response;
        try {
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod(method);
            if (headers != null) {
                headers.forEach(connection::setRequestProperty);
            }
            if (contentType != null) {
                if (headers == null || !headers.containsKey("Content-Type"))
                    connection.setRequestProperty("Content-Type", contentType);
            }
            if (body != null) {
                connection.setDoOutput(true);
                OutputStream stream = connection.getOutputStream();
                stream.write(body.getBytes());
                stream.flush();
                stream.close();
            }
            response = connection.getInputStream().readAllBytes();
            connection.getInputStream().close();
            connection.disconnect();
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
        return response;
    }
}
