package org.touchhome.bundle.api.util;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.FileUtils;
import org.apache.http.Header;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpMessageConverterExtractor;
import org.springframework.web.client.RestTemplate;
import org.touchhome.bundle.api.EntityContext;
import org.touchhome.bundle.api.exception.ServerException;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Path;
import java.util.Collections;
import java.util.function.Consumer;

import static org.apache.commons.io.FileUtils.ONE_MB_BI;

@Log4j2
@RequiredArgsConstructor
public final class Curl {
    private static final RestTemplate restTemplate = new RestTemplate();

    public static <T> T get(String url, Class<T> responseType, Object... uriVariables) {
        return restTemplate.getForObject(url, responseType, uriVariables);
    }

    public static <T> T post(String url, Object request, Class<T> responseType, Object... uriVariables) {
        return restTemplate.postForObject(url, request, responseType, uriVariables);
    }

    public static void delete(String url, Object... uriVariables) {
        restTemplate.delete(url, uriVariables);
    }

    @SneakyThrows
    public static void download(String url, Path targetPath) {
        FileUtils.copyURLToFile(new URL(url), targetPath.toFile(), 60000, 60000);
    }

    @SneakyThrows
    public static void downloadWithProgress(String urlStr, Path targetPath, String progressKey, EntityContext entityContext) {
        entityContext.ui().progress(progressKey, 1, "Checking file size...");
        URL url = new URL(urlStr);
        double fileSize = getFileSize(url);
        // download without progress if less then 2 megabytes
        if (fileSize / 1000 < 2) {
            download(urlStr, targetPath);
            return;
        }
        int maxMb = (int) (fileSize / ONE_MB_BI.intValue());
        URLConnection connection = url.openConnection();
        connection.setConnectTimeout(60000);
        connection.setReadTimeout(60000);
        InputStream input = connection.getInputStream();
        FileUtils.copyInputStreamToFile(new FilterInputStream(input) {
            int readBytes = 0;
            Consumer<Integer> progressHandler = new Consumer<Integer>() {
                int nextStep = 1;

                @Override
                public void accept(Integer num) {
                    readBytes += num;
                    if (readBytes / ONE_MB_BI.doubleValue() > nextStep) {
                        nextStep++;
                        entityContext.ui().progress(progressKey, (readBytes / fileSize * 100) * 0.9, // max 90%
                                "Downloading " + readBytes / ONE_MB_BI.intValue() + "Mb. of " + maxMb + " Mb.");
                    }
                }
            };

            @Override
            public int read(byte[] b, int off, int len) throws IOException {
                int read = super.read(b, off, len);
                progressHandler.accept(read);
                return read;
            }
        }, targetPath.toFile());
    }

    public static int getFileSize(URL url) {
        URLConnection conn = null;
        try {
            conn = url.openConnection();
            if (conn instanceof HttpURLConnection) {
                ((HttpURLConnection) conn).setRequestMethod("HEAD");
            }
            conn.getInputStream();
            return conn.getContentLength();
        } catch (IOException e) {
            throw new ServerException(e);
        } finally {
            if (conn instanceof HttpURLConnection) {
                ((HttpURLConnection) conn).disconnect();
            }
        }
    }

    @SneakyThrows
    public static <T> T getWithTimeout(String command, Class<T> returnType, int timeoutInSec) {
        CloseableHttpResponse response = createApacheHttpClient(timeoutInSec).execute(new HttpGet(command));
        HttpMessageConverterExtractor<T> responseExtractor = new HttpMessageConverterExtractor<>(returnType, restTemplate.getMessageConverters());
        return responseExtractor.extractData(new ClientHttpResponse() {
            @Override
            public HttpStatus getStatusCode() {
                return null;
            }

            @Override
            public int getRawStatusCode() {
                return response.getStatusLine().getStatusCode();
            }

            @Override
            public String getStatusText() {
                return response.getStatusLine().getReasonPhrase();
            }

            @Override
            @SneakyThrows
            public void close() {
                response.close();
            }

            @Override
            public InputStream getBody() throws IOException {
                return response.getEntity().getContent();
            }

            @Override
            public HttpHeaders getHeaders() {
                MultiValueMap<String, String> headers = new LinkedMultiValueMap<>(response.getAllHeaders().length);
                for (Header header : response.getAllHeaders()) {
                    headers.put(header.getName(), Collections.singletonList(header.getValue()));
                }
                return new HttpHeaders(headers);
            }
        });
    }

    private static CloseableHttpClient createApacheHttpClient(int timeoutInSec) {
        RequestConfig config = RequestConfig.custom()
                .setConnectTimeout(timeoutInSec * 1000)
                .setSocketTimeout(timeoutInSec * 1000).build();
        return HttpClientBuilder.create().setDefaultRequestConfig(config).build();
    }
}
