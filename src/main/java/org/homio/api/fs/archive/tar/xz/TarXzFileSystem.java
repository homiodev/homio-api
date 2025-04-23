package org.homio.api.fs.archive.tar.xz;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Map;
import org.apache.commons.compress.compressors.xz.XZCompressorInputStream;
import org.apache.commons.compress.compressors.xz.XZCompressorOutputStream;
import org.homio.api.fs.archive.tar.AbstractTarFileSystem;
import org.homio.api.fs.archive.tar.AbstractTarFileSystemProvider;
import org.homio.api.fs.archive.tar.TarUtils;

class TarXzFileSystem extends AbstractTarFileSystem {

  protected TarXzFileSystem(AbstractTarFileSystemProvider provider, Path tfpath, Map<String, ?> env)
      throws IOException {
    super(provider, tfpath, env);
  }

  @Override
  protected byte[] readFile(Path path) throws IOException {
    return TarUtils.readAllBytes(
        new XZCompressorInputStream(Files.newInputStream(path, StandardOpenOption.READ)));
  }

  @Override
  protected void writeFile(byte[] tarBytes, Path path) throws IOException {
    try (XZCompressorOutputStream outputStream =
        new XZCompressorOutputStream(
            Files.newOutputStream(
                path, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE))) {
      outputStream.write(tarBytes, 0, tarBytes.length);
      outputStream.flush();
      outputStream.finish();
    }
  }
}
