package org.homio.bundle.api.audio.stream;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.homio.bundle.api.audio.AudioFormat;
import org.homio.bundle.api.audio.AudioStream;
import org.jetbrains.annotations.Nullable;

@Log4j2
public class URLAudioStream extends AudioStream {

    public static final String M3U_EXTENSION = "m3u";
    public static final String PLS_EXTENSION = "pls";
    private static final Pattern PLS_STREAM_PATTERN = Pattern.compile("^File[0-9]=(.+)$");
    private final AudioFormat audioFormat;
    private final InputStream inputStream;
    @Getter
    private String url;

    private @Nullable
    Socket shoutCastSocket;

    public URLAudioStream(String url) throws Exception {
        this.url = url;
        this.audioFormat = new AudioFormat(AudioFormat.CONTAINER_NONE, AudioFormat.CODEC_MP3, false, 16, null, null);
        this.inputStream = createInputStream();
    }

    private InputStream createInputStream() throws Exception {
        final String filename = url.toLowerCase();
        final String extension = StringUtils.defaultString(FilenameUtils.getExtension(filename), "");
        try {
            switch (extension) {
                case M3U_EXTENSION:
                    try (Scanner scanner = new Scanner(new URL(url).openStream(), StandardCharsets.UTF_8.name())) {
                        while (true) {
                            String line = scanner.nextLine();
                            if (!line.isEmpty() && !line.startsWith("#")) {
                                url = line;
                                break;
                            }
                        }
                    } catch (NoSuchElementException e) {
                        // we reached the end of the file, this exception is thus expected
                    }
                    break;
                case PLS_EXTENSION:
                    try (Scanner scanner = new Scanner(new URL(url).openStream(), StandardCharsets.UTF_8.name())) {
                        while (true) {
                            String line = scanner.nextLine();
                            if (!line.isEmpty() && line.startsWith("File")) {
                                final Matcher matcher = PLS_STREAM_PATTERN.matcher(line);
                                if (matcher.find()) {
                                    url = matcher.group(1);
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
            URL streamUrl = new URL(url);
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
                // getInputStream() method is more error-proof than openStream(),
                // because openStream() does openConnection().getInputStream(),
                // which opens a new connection and does not reuse the old one.
                return connection.getInputStream();
            }
        } catch (MalformedURLException e) {
            log.error("URL '{}' is not a valid url: {}", url, e.getMessage(), e);
            throw new MalformedURLException("URL not valid");
        } catch (IOException e) {
            log.error("Cannot set up stream '{}': {}", url, e.getMessage(), e);
            throw new IOException("IO Error");
        }
    }

    @Override
    public AudioFormat getFormat() {
        return audioFormat;
    }

    @Override
    public int read() throws IOException {
        return inputStream.read();
    }

    @Override
    public void close() throws IOException {
        super.close();
        if (shoutCastSocket != null) {
            shoutCastSocket.close();
        }
    }

    @Override
    public String toString() {
        return url;
    }
}
