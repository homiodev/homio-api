package org.homio.api.util;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;

@Getter
@RequiredArgsConstructor
public class LogOutputStream extends ByteArrayOutputStream {

  private final Logger logger;
  private final Level level;

  /** Flushes the output stream. */
  public void flush() {
    logger.log(level, toString(StandardCharsets.UTF_8));
    reset();
  }
}
