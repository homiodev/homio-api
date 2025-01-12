package org.homio.api.fs.archive.tar.gz;

import org.homio.api.fs.archive.tar.AbstractTarFileSystem;
import org.homio.api.fs.archive.tar.AbstractTarFileSystemProvider;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

public class TarGzipFileSystemProvider extends AbstractTarFileSystemProvider {

  @Override
  public String getScheme() {
    return "tar.gz";
  }

  @Override
  protected AbstractTarFileSystem newInstance(
    AbstractTarFileSystemProvider provider, Path path,
    Map<String, ?> env) throws IOException {
    return new TarGzipFileSystem(provider, path, env);
  }

}
