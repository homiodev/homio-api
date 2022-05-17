package org.touchhome.bundle.api.service;

import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.AbstractFileFilter;
import org.touchhome.bundle.api.util.TouchHomeUtils;
import org.touchhome.common.util.CommonUtils;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.apache.commons.io.filefilter.TrueFileFilter.TRUE;

/**
 * Interface for entities who have ability to convert text to sound
 */
@Log4j2
public abstract class TextToSpeechEntityService {

    @Getter
    private final Path cacheFolder;
    public long lastTimeCleanOldCache = 0;

    @SneakyThrows
    public TextToSpeechEntityService(String folderName) {
        cacheFolder = CommonUtils.createDirectoriesIfNotExists(TouchHomeUtils.getAudioPath().resolve(folderName));
        cleanOldCache();
    }

    @SneakyThrows
    public Path getCharacters() {
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        Path file = cacheFolder.resolve(getCharactersFilePrefix(cal) + "_characters.txt");
        if (!Files.exists(file)) {
            Files.createFile(file);
        }
        return file;
    }

    protected String getCharactersFilePrefix(Calendar cal) {
        return cal.get(Calendar.MONTH) + "_" + cal.get(Calendar.YEAR);
    }

    protected abstract byte[] synthesizeSpeech(String text);

    @SneakyThrows
    public void cleanOldCache() {
        boolean requireToCheckCache = TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis() - lastTimeCleanOldCache) > 1;
        if (requireToCheckCache) {
            lastTimeCleanOldCache = System.currentTimeMillis();
            cleanOldCharacterFiles();
            cleanOldAudioFiles();
        }
    }

    private void cleanOldAudioFiles() {
        for (File oldFile : FileUtils.listFiles(cacheFolder.toFile(), new AbstractFileFilter() {
            @Override
            public boolean accept(File file) {
                try {
                    BasicFileAttributes attributes = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
                    return TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis() - attributes.lastAccessTime().toMillis()) > 31;
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }, TRUE)) {
            oldFile.delete();
        }
    }

    protected void cleanOldCharacterFiles() {
        String currentCharactersFileName = getCharacters().getFileName().toString();
        for (File oldFile : FileUtils.listFiles(cacheFolder.toFile(), new AbstractFileFilter() {
            @Override
            public boolean accept(File file) {
                return !file.getName().equals(currentCharactersFileName);
            }
        }, TRUE)) {
            oldFile.delete();
        }
    }

    public abstract List<String> getVoices(String languageCode);

    public abstract String getUniqueFilenameForText();

    @SneakyThrows
    public int getSynthesizedCharacters() {
        return (int) Files.size(getCharacters());
    }

    @SneakyThrows
    public Path synthesizeSpeech(String text, boolean save) {
        cleanOldCache();
        String fileNameInCache = getUniqueFilenameForText(text);
        Path audioFileInCache = cacheFolder.resolve(fileNameInCache + ".mp3");
        // check if in cache
        if (save && Files.exists(audioFileInCache)) {
            log.debug("Audio file {} was found in cache.", audioFileInCache.getFileName());
            return audioFileInCache;
        }

        // if not in cache, get audio data and put to cache
        byte[] audio = synthesizeSpeech(text);
        if (save && audio != null) {
            return saveAudioAndTextToFile(text, audioFileInCache, audio);
        }
        return null;
    }

    private Path saveAudioAndTextToFile(String text, Path cacheFile, byte[] audio) {
        log.debug("Caching audio file {}", cacheFile.getFileName());
        TouchHomeUtils.writeToFile(cacheFile, audio, false);
        return TouchHomeUtils.writeToFile(getCharacters(), text, true);
    }

    public String getUniqueFilenameForText(String text) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] bytesOfMessage = text.getBytes(StandardCharsets.UTF_8);
            String fileNameHash = String.format("%032x", new BigInteger(1, md.digest(bytesOfMessage)));
            return getUniqueFilenameForText() + "_" + fileNameHash;
        } catch (NoSuchAlgorithmException e) {
            // should not happen
            log.error("Could not create MD5 hash for '{}'", text, e);
            return null;
        }
    }

    public void destroy() {
        cleanOldCache();
    }
}
