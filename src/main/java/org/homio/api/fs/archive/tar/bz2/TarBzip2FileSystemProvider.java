package org.homio.api.fs.archive.tar.bz2;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import org.homio.api.fs.archive.tar.AbstractTarFileSystemProvider;
import org.homio.api.fs.archive.tar.AbstractTarFileSystem;

public class TarBzip2FileSystemProvider extends AbstractTarFileSystemProvider {

	@Override
	protected AbstractTarFileSystem newInstance(
			AbstractTarFileSystemProvider provider, Path path,
			Map<String, ?> env) throws IOException {
		return new TarBzip2FileSystem(provider, path, env);
	}

	@Override
	public String getScheme() {
		return "tar.bz2";
	}

}