package org.homio.api.stream.impl;

import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.FilenameUtils;
import org.homio.api.stream.ContentStream;
import org.homio.api.stream.StreamFormat;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.util.ResourceUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Objects.requireNonNullElseGet;

@Log4j2
public class URLContentStream extends UrlResource implements ContentStream {

    public static final String M3U_EXTENSION = "m3u";
    public static final String PLS_EXTENSION = "pls";
    private static final Pattern PLS_STREAM_PATTERN = Pattern.compile("^File[0-9]=(.+)$");
    @Getter
    private final @NotNull StreamFormat streamFormat;

    @Nullable
    private Socket shoutCastSocket;

    public URLContentStream(@NotNull String url) throws MalformedURLException {
        this(new URL(url), (StreamFormat) null);
    }

    public URLContentStream(@NotNull URL url, @NotNull String fileName) {
        super(url);
        this.streamFormat = StreamFormat.evaluateFormat(fileName);
    }

    public URLContentStream(@NotNull URL url, @Nullable StreamFormat streamFormat) {
        super(url);
        this.streamFormat = requireNonNullElseGet(streamFormat, () ->
                StreamFormat.evaluateFormat(getFilename()));
    }

    @Override
    public @NotNull InputStream getInputStream() {
        return createInputStream();
    }

    @Override
    public void close() throws IOException {
        if (shoutCastSocket != null) {
            shoutCastSocket.close();
        }
    }

    @Override
    @SneakyThrows
    public long contentLength() {
        if (ResourceUtils.isFileURL(getURL())) {
            return getFile().length();
        }
        return -1;
    }

    @SneakyThrows
    private InputStream createInputStream() {
        final String filename = getURL().toString().toLowerCase();
        URL streamUrl = getURL();
        final String extension = Objects.toString(FilenameUtils.getExtension(filename), "");
        try {
            switch (extension) {
                case M3U_EXTENSION:
                    try (Scanner scanner = new Scanner(getURL().openStream(), StandardCharsets.UTF_8)) {
                        while (true) {
                            String line = scanner.nextLine();
                            if (!line.isEmpty() && !line.startsWith("#")) {
                                streamUrl = new URL(line);
                                break;
                            }
                        }
                    } catch (NoSuchElementException e) {
                        // we reached the end of the file, this exception is thus expected
                    }
                    break;
                case PLS_EXTENSION:
                    try (Scanner scanner = new Scanner(getURL().openStream(), StandardCharsets.UTF_8)) {
                        while (true) {
                            String line = scanner.nextLine();
                            if (line.startsWith("File")) {
                                final Matcher matcher = PLS_STREAM_PATTERN.matcher(line);
                                if (matcher.find()) {
                                    streamUrl = new URL(matcher.group(1));
                                    break;
                                }
                            }
                        }
                    } catch (NoSuchElementException e) {
                        // we reached the end of the file, this exception is thus expected
                    }
                    break;
                default:
                    break;
            }
            URLConnection connection = streamUrl.openConnection();
            if ("unknown/unknown".equals(connection.getContentType())) {
                // Java does not parse non-standard headers used by SHOUTCast
                int port = streamUrl.getPort() > 0 ? streamUrl.getPort() : 80;
                // Manipulate User-Agent to receive a stream
                Socket socket = new Socket(streamUrl.getHost(), port);
                shoutCastSocket = socket;

                OutputStream os = socket.getOutputStream();
                String userAgent = "WinampMPEG/5.09";
                String req = "GET / HTTP/1.0\r\nuser-agent: " + userAgent
                             + "\r\nIcy-MetaData: 1\r\nConnection: keep-alive\r\n\r\n";
                os.write(req.getBytes());
                return socket.getInputStream();
            } else {
                return super.getInputStream();
            }
        } catch (MalformedURLException e) {
            log.error("URL '{}' is not a valid url: {}", getURL(), e.getMessage(), e);
            throw new MalformedURLException("URL not valid");
        } catch (IOException e) {
            log.error("Cannot set up stream '{}': {}", getURL(), e.getMessage(), e);
            throw new IOException("IO Error");
        }
    }

    @Override
    public @NotNull Resource getResource() {
        return this;
    }
}
