package org.homio.api.stream.impl;

import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.NotImplementedException;
import org.homio.api.stream.ContentStream;
import org.homio.api.stream.StreamFormat;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNullElseGet;

@Log4j2
public class URLContentStream extends UrlResource implements ContentStream {
  public static final String PLS_EXTENSION = "pls";
  private static final Pattern PLS_STREAM_PATTERN = Pattern.compile("^File[0-9]=(.+)$");
  @Getter
  private final @NotNull StreamFormat streamFormat;
  String M3U_EXTENSION = "m3u";
  String M3U8_EXTENSION = "m3u8";
  @Nullable
  private Socket shoutCastSocket;
  private String content;

  public URLContentStream(@NotNull String url) throws MalformedURLException {
    this(new URL(url), (StreamFormat) null);
  }

  public URLContentStream(@NotNull URL url) {
    this(url, (StreamFormat) null);
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
  public long contentLength() throws IOException {
    if (isM3U8Content()) {
      return getM3U8InputStream().available();
    }
    return super.contentLength();
  }

  @Override
  public @NotNull InputStream getInputStream() {
    return createInputStream();
  }

  @SneakyThrows
  protected @NotNull URL createRelativeUrl(@NotNull String line) {
    return getURL().toURI().resolve(line).toURL();
    // String url = getURL().toString();
    // return url.substring(0, url.lastIndexOf("/")) + "/" + line;
  }

  @Override
  public void close() throws IOException {
    if (shoutCastSocket != null) {
      shoutCastSocket.close();
    }
  }

  @SneakyThrows
  private InputStream createInputStream() {
    if (isM3U8Content()) {
      return getM3U8InputStream();
    }
    URL streamUrl = getURL();
    try {
      if (Objects.equals(getFileExtension(), PLS_EXTENSION)) {
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

  @SneakyThrows
  private @NotNull InputStream getM3U8InputStream() {
    if (content == null) {
      try (BufferedReader reader = new BufferedReader(new InputStreamReader(getURL().openStream(), StandardCharsets.UTF_8))) {
        this.content = reader.lines().map(line -> {
          if (!line.isEmpty() && !line.startsWith("#")) {
            ContentStream contentStream;
            contentStream = new URLContentStream(createRelativeUrl(line)) {
              @Override
              protected @NotNull String createStreamUrl(@NotNull ContentStream contentStream) {
                return URLContentStream.this.createStreamUrl(contentStream);
              }
            };
            return createStreamUrl(contentStream);
          }
          return line;
        }).collect(Collectors.joining("\n"));
      }
    }
    return new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
  }

  @NotNull
  protected String createStreamUrl(@NotNull ContentStream contentStream) {
    throw new NotImplementedException();
  }

  private boolean isM3U8Content() {
    String fileExtension = getFileExtension();
    return fileExtension.equals(M3U8_EXTENSION) || fileExtension.equals(M3U_EXTENSION);
  }

  private @NotNull String getFileExtension() {
    String filename = getResource().getFilename();
    return Objects.toString(FilenameUtils.getExtension(filename), "");
  }
}
