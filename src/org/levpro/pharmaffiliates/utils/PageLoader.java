package org.levpro.pharmaffiliates.utils;

import java.net.HttpURLConnection;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

public class PageLoader {
    public static String load(String url, String method) throws IOException {
        URL address = new URL(url);

        HttpURLConnection connection = (HttpURLConnection) address.openConnection();

        connection.setRequestMethod(method);

        connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");

        int status = connection.getResponseCode();

        if (status != HttpURLConnection.HTTP_OK) {
            throw new IOException(String.format("Server response with error, code: %s", status));
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

        StringBuilder output = new StringBuilder();
        String line;

        while ((line = reader.readLine()) != null) {
            output.append(line);
        }

        reader.close();

        return output.toString();
    }
}
