package org.homio.api.fs.archive.tar.gz;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import org.homio.api.fs.archive.tar.AbstractTarFileSystem;
import org.homio.api.fs.archive.tar.AbstractTarFileSystemProvider;
import org.homio.api.fs.archive.tar.TarUtils;

class TarGzipFileSystem extends AbstractTarFileSystem {

    TarGzipFileSystem(AbstractTarFileSystemProvider provider, Path tfpath,
        Map<String, ?> env) throws IOException {
        super(provider, tfpath, env);
    }

    @Override
    protected byte[] readFile(Path path) throws IOException {
        return TarUtils.readAllBytes(new GZIPInputStream(Files.newInputStream(
            path, StandardOpenOption.READ)));
    }

    @Override
    protected void writeFile(byte[] tarBytes, Path path) throws IOException {
        try (GZIPOutputStream os = new GZIPOutputStream(Files.newOutputStream(
            path, StandardOpenOption.TRUNCATE_EXISTING,
            StandardOpenOption.WRITE))) {
            os.write(tarBytes, 0, tarBytes.length);
            os.flush();
            os.finish();
        }
    }

}
