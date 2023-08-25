package org.homio.api.fs.archive.tar.gz;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import org.homio.api.fs.archive.tar.AbstractTarFileSystem;
import org.homio.api.fs.archive.tar.AbstractTarFileSystemProvider;

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
