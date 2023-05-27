package org.homio.api.audio;

import java.io.InputStream;

public abstract class AudioStream extends InputStream {

    public abstract AudioFormat getFormat();
}
