package org.touchhome.bundle.api.state;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.tika.Tika;
import org.apache.tika.parser.txt.CharsetDetector;
import org.springframework.util.MimeTypeUtils;

import java.util.Arrays;
import java.util.Base64;

@Accessors(chain = true)
public class RawType implements State {

    @Getter
    @Setter
    protected String name;
    protected byte[] bytes;
    @Getter
    protected String mimeType;

    public static RawType ofPlainText(String value) {
        return new RawType((value == null ? "" : value).getBytes(), MimeTypeUtils.TEXT_PLAIN_VALUE);
    }

    public RawType(byte[] bytes) {
        this(bytes, new Tika().detect(bytes), null);
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

    @Override
    public String toString() {
        return RawType.detectByteToString(bytes);
    }

    public static String detectByteToString(byte[] bytes) {
        CharsetDetector detector = new CharsetDetector();
        detector.setText(bytes);
        detector.detect();
        return detector.getString(bytes, "UTF-8");
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

    @Override
    public byte[] byteArrayValue() {
        return bytes;
    }

    @Override
    public String toFullString() {
        if (mimeType.startsWith("image/")) {
            return "data:image/jpeg;base64," + Base64.getEncoder().encodeToString(bytes);
        }
        return Base64.getEncoder().encodeToString(bytes);
    }

    @Override
    public RawType toRawType() {
        return this;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(bytes);
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
        return Arrays.equals(bytes, other.bytes);
    }

    public boolean startsWith(String prefix) {
        byte[] prefixBytes = prefix.getBytes();
        if (bytes.length < prefixBytes.length) {
            return false;
        }
        for (int i = 0; i < prefixBytes.length; i++) {
            if (prefixBytes[i] != bytes[i]) {
                return false;
            }
        }
        return true;
    }
}
