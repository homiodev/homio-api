package org.homio.api.fs.archive.tar;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.FileChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.*;
import java.nio.file.DirectoryStream.Filter;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.FileAttributeView;
import java.nio.file.spi.FileSystemProvider;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;

public abstract class AbstractTarFileSystemProvider extends FileSystemProvider {

    protected final Map<Path, AbstractTarFileSystem> filesystems = new HashMap<>();

    public AbstractTarFileSystemProvider() {
    }

    // Checks that the given file is a UnixPath
    static final TarPath toTarPath(Path path) {
        if (path == null) {
            throw new NullPointerException();
        }
        if (!(path instanceof TarPath)) {
            throw new ProviderMismatchException();
        }
        return (TarPath) path;
    }

    protected Path uriToPath(URI uri) {
        String scheme = uri.getScheme();
        if (scheme == null || !scheme.equalsIgnoreCase(getScheme())) {
            throw new IllegalArgumentException("URI scheme is not '"
                    + getScheme() + "'");
        }
        try {
            String spec = uri.getRawSchemeSpecificPart();
            int sep = spec.indexOf("!/");
            if (sep != -1) {
                spec = spec.substring(0, sep);
            }
            return Paths.get(new URI(spec)).toAbsolutePath();
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
    }

    protected abstract AbstractTarFileSystem newInstance(
            AbstractTarFileSystemProvider provider, Path path,
            Map<String, ?> env) throws IOException;

    protected boolean ensureFile(Path path) {
        try {
            BasicFileAttributes attrs = Files.readAttributes(path, BasicFileAttributes.class);
            if (!attrs.isRegularFile()) {
                throw new UnsupportedOperationException();
            }
            if (!path.toString().endsWith(getScheme())) {
                throw new UnsupportedOperationException();
            }
            return true;
        } catch (IOException ioe) {
            return false;
        }
    }

    @Override
    public FileSystem newFileSystem(URI uri, Map<String, ?> env)
            throws IOException {
        Path path = uriToPath(uri);
        synchronized (filesystems) {
            Path realPath = null;
            if (ensureFile(path)) {
                realPath = path.toRealPath();
                if (filesystems.containsKey(realPath)) {
                    throw new FileSystemAlreadyExistsException();
                }
            }
            AbstractTarFileSystem tarfs = null;
            tarfs = newInstance(this, path, env);
            filesystems.put(realPath, tarfs);
            return tarfs;
        }
    }

    @Override
    public FileSystem newFileSystem(Path path, Map<String, ?> env)
            throws IOException {
        if (path.getFileSystem() != FileSystems.getDefault()) {
            throw new UnsupportedOperationException();
        }
        ensureFile(path);
        return newInstance(this, path, env);
    }

    @Override
    public Path getPath(URI uri) {

        String spec = uri.getSchemeSpecificPart();
        int sep = spec.indexOf("!/");
        if (sep == -1) {
            throw new IllegalArgumentException(
                    "URI: "
                            + uri
                            + " does not contain path info ex. tar:file:/c:/foo.tar!/BAR");
        }
        return getFileSystem(uri).getPath(spec.substring(sep + 1));
    }

    @Override
    public FileSystem getFileSystem(URI uri) {
        synchronized (filesystems) {
            AbstractTarFileSystem tarfs = null;
            try {
                tarfs = filesystems.get(uriToPath(uri).toRealPath());
            } catch (IOException x) {
                // ignore the ioe from toRealPath(), return FSNFE
            }
            if (tarfs == null) {
                throw new FileSystemNotFoundException();
            }
            return tarfs;
        }
    }

    @Override
    public void checkAccess(Path path, AccessMode... modes) throws IOException {
        AbstractTarFileSystemProvider.toTarPath(path).checkAccess(modes);
    }

    @Override
    public void copy(Path src, Path target, CopyOption... options)
            throws IOException {
        AbstractTarFileSystemProvider.toTarPath(src).copy(
                AbstractTarFileSystemProvider.toTarPath(target), options);
    }

    @Override
    public void createDirectory(Path path, FileAttribute<?>... attrs)
            throws IOException {
        AbstractTarFileSystemProvider.toTarPath(path).createDirectory(attrs);
    }

    @Override
    public final void delete(Path path) throws IOException {
        AbstractTarFileSystemProvider.toTarPath(path).delete();
    }

    @Override
    public <V extends FileAttributeView> V getFileAttributeView(Path path,
                                                                Class<V> type, LinkOption... options) {
        return TarFileAttributeView.get(
                AbstractTarFileSystemProvider.toTarPath(path), type);
    }

    @Override
    public FileStore getFileStore(Path path) throws IOException {
        return AbstractTarFileSystemProvider.toTarPath(path).getFileStore();
    }

    @Override
    public boolean isHidden(Path path) {
        return AbstractTarFileSystemProvider.toTarPath(path).isHidden();
    }

    @Override
    public boolean isSameFile(Path path, Path other) throws IOException {
        return AbstractTarFileSystemProvider.toTarPath(path).isSameFile(other);
    }

    @Override
    public void move(Path src, Path target, CopyOption... options)
            throws IOException {
        AbstractTarFileSystemProvider.toTarPath(src).move(
                AbstractTarFileSystemProvider.toTarPath(target), options);
    }

    @Override
    public AsynchronousFileChannel newAsynchronousFileChannel(Path path,
                                                              Set<? extends OpenOption> options, ExecutorService exec,
                                                              FileAttribute<?>... attrs) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public SeekableByteChannel newByteChannel(Path path,
                                              Set<? extends OpenOption> options, FileAttribute<?>... attrs)
            throws IOException {
        return AbstractTarFileSystemProvider.toTarPath(path).newByteChannel(
                options, attrs);
    }

    @Override
    public DirectoryStream<Path> newDirectoryStream(Path path,
                                                    Filter<? super Path> filter) throws IOException {
        return AbstractTarFileSystemProvider.toTarPath(path)
                .newDirectoryStream(filter);
    }

    @Override
    public FileChannel newFileChannel(Path path,
                                      Set<? extends OpenOption> options, FileAttribute<?>... attrs)
            throws IOException {
        return AbstractTarFileSystemProvider.toTarPath(path).newFileChannel(
                options, attrs);
    }

    @Override
    public InputStream newInputStream(Path path, OpenOption... options)
            throws IOException {
        return AbstractTarFileSystemProvider.toTarPath(path).newInputStream(
                options);
    }

    @Override
    public OutputStream newOutputStream(Path path, OpenOption... options)
            throws IOException {
        return AbstractTarFileSystemProvider.toTarPath(path).newOutputStream(
                options);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <A extends BasicFileAttributes> A readAttributes(Path path,
                                                            Class<A> type, LinkOption... options) throws IOException {
        if (type == BasicFileAttributes.class
                || type == TarFileAttributes.class) {
            return (A) AbstractTarFileSystemProvider.toTarPath(path)
                    .getAttributes();
        }
        return null;
    }

    @Override
    public Map<String, Object> readAttributes(Path path, String attribute,
                                              LinkOption... options) throws IOException {
        return AbstractTarFileSystemProvider.toTarPath(path).readAttributes(
                attribute, options);
    }

    @Override
    public Path readSymbolicLink(Path link) throws IOException {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public void setAttribute(Path path, String attribute, Object value,
                             LinkOption... options) throws IOException {
        AbstractTarFileSystemProvider.toTarPath(path).setAttribute(attribute,
                value, options);
    }

    void removeFileSystem(Path tfpath, AbstractTarFileSystem tfs)
            throws IOException {
        synchronized (filesystems) {
            Path realPath = tfpath.toRealPath();
            if (filesystems.get(realPath) == tfs) {
                filesystems.remove(realPath);
            }
        }
    }
}
