package org.touchhome.bundle.api.state;

import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.MimeTypeUtils;
import org.touchhome.bundle.api.util.TouchHomeUtils;
import org.touchhome.common.util.CommonUtils;
import org.touchhome.common.util.Curl;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Base64;

@Accessors(chain = true)
public class RawType extends State {

    @Getter
    @Setter
    protected String name;
    protected byte[] bytes;
    @Getter
    protected String mimeType;
    private Path relatedFile;

    public RawType(byte[] bytes) {
        this(bytes, MimeTypeUtils.TEXT_PLAIN_VALUE, null);
    }

    public RawType(Path file) {
        this(file, MimeTypeUtils.TEXT_PLAIN_VALUE);
    }

    public RawType(Path file, String mimeType) {
        this(new byte[0], mimeType, null);
        this.relatedFile = file;
    }

    public RawType(byte[] bytes, String mimeType) {
        this(bytes, mimeType, null);
    }

    public RawType(byte[] bytes, String mimeType, String name) {
        if (mimeType.isEmpty()) {
            throw new IllegalArgumentException("mimeType argument must not be blank");
        }
        this.bytes = bytes;
        this.mimeType = mimeType;
        this.name = name;
    }

    public RawType(Curl.RawResponse rawResponse) {
        this.bytes = rawResponse.getBytes();
        this.mimeType = rawResponse.getMimeType();
        this.name = rawResponse.getName();
    }

    public static RawType ofPlainText(String value) {
        return new RawType((value == null ? "" : value).getBytes(), MimeTypeUtils.TEXT_PLAIN_VALUE);
    }

    public static RawType valueOf(String value) {
        int idx, idx2;
        if (value.isEmpty()) {
            throw new IllegalArgumentException("Argument must not be blank");
        } else if (!value.startsWith("data:") || ((idx = value.indexOf(",")) < 0)) {
            throw new IllegalArgumentException("Invalid data URI syntax for argument " + value);
        } else if ((idx2 = value.indexOf(";")) <= 5) {
            throw new IllegalArgumentException("Missing MIME type in argument " + value);
        }
        return new RawType(Base64.getDecoder().decode(value.substring(idx + 1)), value.substring(5, idx2));
    }

    public Path toPath() {
        if (relatedFile == null || !Files.isReadable(relatedFile)) {
            String fileName = name;
            if (fileName == null) {
                fileName = String.valueOf(Arrays.hashCode(bytes));
            }
            relatedFile = TouchHomeUtils.writeToFile(CommonUtils.getTmpPath().resolve(fileName), bytes, false);
        }
        return relatedFile;
    }

    @Override
    public float floatValue() {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public int intValue() {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public boolean boolValue() {
        throw new RuntimeException("Not implemented");
    }

    @SneakyThrows
    @Override
    public byte[] byteArrayValue() {
        if (bytes == null && relatedFile != null) {
            bytes = Files.readAllBytes(relatedFile);
        }
        return bytes;
    }

    @Override
    public String stringValue() {
        if (mimeType.startsWith("image/")) {
            return "data:image/jpeg;base64," + Base64.getEncoder().encodeToString(byteArrayValue());
        }
        return toString();
    }

    @Override
    public String toString() {
        return new String(byteArrayValue());
    }

    @Override
    public RawType toRawType() {
        return this;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(byteArrayValue());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        RawType other = (RawType) obj;
        if (!mimeType.equals(other.mimeType)) {
            return false;
        }
        return Arrays.equals(byteArrayValue(), other.byteArrayValue());
    }

    public boolean startsWith(String prefix) {
        byte[] prefixBytes = prefix.getBytes();
        if (byteArrayValue().length < prefixBytes.length) {
            return false;
        }
        for (int i = 0; i < prefixBytes.length; i++) {
            if (prefixBytes[i] != byteArrayValue()[i]) {
                return false;
            }
        }
        return true;
    }

    public boolean isImage() {
        return StringUtils.defaultString(mimeType, "").startsWith("image/");
    }

    public boolean isVideo() {
        return StringUtils.defaultString(mimeType, "").startsWith("video/");
    }
}
