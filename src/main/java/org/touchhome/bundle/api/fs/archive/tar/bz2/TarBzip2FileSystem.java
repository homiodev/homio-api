package org.touchhome.bundle.api.fs.archive.tar.bz2;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorOutputStream;
import org.touchhome.bundle.api.fs.archive.tar.AbstractTarFileSystem;
import org.touchhome.bundle.api.fs.archive.tar.AbstractTarFileSystemProvider;
import org.touchhome.bundle.api.fs.archive.tar.TarUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Map;

class TarBzip2FileSystem extends AbstractTarFileSystem {

    protected TarBzip2FileSystem(AbstractTarFileSystemProvider provider,
                                 Path tfpath, Map<String, ?> env) throws IOException {
        super(provider, tfpath, env);
    }

    @Override
    protected byte[] readFile(Path path) throws IOException {
        return TarUtils.readAllBytes(new BZip2CompressorInputStream(Files
                .newInputStream(path, StandardOpenOption.READ)));
    }

    @Override
    protected void writeFile(byte[] tarBytes, Path path) throws IOException {
        try (BZip2CompressorOutputStream outputStream = new BZip2CompressorOutputStream(
                Files.newOutputStream(path,
                        StandardOpenOption.TRUNCATE_EXISTING,
                        StandardOpenOption.WRITE))) {
            outputStream.write(tarBytes, 0, tarBytes.length);
            outputStream.flush();
            outputStream.finish();
        }
    }
}
