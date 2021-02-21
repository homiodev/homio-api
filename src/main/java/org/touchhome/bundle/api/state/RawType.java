package org.touchhome.bundle.api.state;

import java.util.Arrays;
import java.util.Base64;

public class RawType implements State {

    public static final String DEFAULT_MIME_TYPE = "application/octet-stream";

    protected byte[] bytes;
    protected String mimeType;

    public RawType(byte[] bytes, String mimeType) {
        if (mimeType.isEmpty()) {
            throw new IllegalArgumentException("mimeType argument must not be blank");
        }
        this.bytes = bytes;
        this.mimeType = mimeType;
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

    public byte[] getBytes() {
        return bytes;
    }

    public String getMimeType() {
        return mimeType;
    }

    @Override
    public String toString() {
        return String.format("raw type (%s): %d bytes", mimeType, bytes.length);
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
        return String.format("data:%s;base64,%s", mimeType, Base64.getEncoder().encodeToString(bytes));
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
        if (!Arrays.equals(bytes, other.bytes)) {
            return false;
        }
        return true;
    }
}
