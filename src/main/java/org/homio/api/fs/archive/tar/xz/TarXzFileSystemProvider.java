package org.homio.api.fs.archive.tar.xz;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import org.homio.api.fs.archive.tar.AbstractTarFileSystemProvider;
import org.homio.api.fs.archive.tar.AbstractTarFileSystem;

public class TarXzFileSystemProvider extends AbstractTarFileSystemProvider {

    @Override
    protected AbstractTarFileSystem newInstance(
            AbstractTarFileSystemProvider provider, Path path,
            Map<String, ?> env) throws IOException {
        return new TarXzFileSystem(provider, path, env);
    }

    @Override
    public String getScheme() {
        return "tar.xz";
    }

}
