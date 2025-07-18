package org.homio.api.service;

import java.io.File;
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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.FileUtils;
import org.homio.api.Context;
import org.homio.api.exception.ServerException;
import org.homio.api.util.CommonUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Interface for entities who have an ability to convert text to sound
 */
@Getter
@Log4j2
public abstract class TextToSpeechEntityService<E extends EntityService<?>> extends EntityService.ServiceInstance<E> {

    protected final @NotNull Path cacheFolder;
    protected final @Nullable Integer maxQuota;
    protected final @NotNull Map<String, List<String>> cacheVoices = new ConcurrentHashMap<>();
    protected long lastTimeCleanOldCache = 0;

    /**
     * @param folderName - cache folder name
     * @param maxQuota   if null - no quota
     */
    public TextToSpeechEntityService(Context context, E entity, String folderName, @Nullable Integer maxQuota, @NotNull String name) {
        super(context, entity, true, name);
        this.cacheFolder = CommonUtils.createDirectoriesIfNotExists(CommonUtils.getAudioPath().resolve(folderName));
        this.maxQuota = maxQuota;
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

    @SneakyThrows
    public void cleanOldCache() {
        boolean requireToCheckCache = TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis() - lastTimeCleanOldCache) > 1;
        if (requireToCheckCache) {
            lastTimeCleanOldCache = System.currentTimeMillis();
            cleanOldCharacterFiles();
            cleanOldAudioFiles();
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
        if (text == null || text.length() < 3) {
            throw new IllegalArgumentException("Unable to make sound from too short text");
        }
        cleanOldCache();
        String fileNameInCache = getUniqueFilenameForText(text);
        Path audioFileInCache = cacheFolder.resolve(fileNameInCache + ".mp3");
        // check if in cache
        if (save && Files.exists(audioFileInCache) && Files.size(audioFileInCache) > 0) {
            log.debug("Audio file {} was found in cache.", audioFileInCache.getFileName());
            return audioFileInCache;
        }

        if (maxQuota != null && getSynthesizedCharacters() > maxQuota) {
            throw new ServerException("ERROR.TTS_QUOTA", maxQuota);
        }

        // if not in cache, get audio data and put to cache
        byte[] audio = synthesizeSpeech(text);
        if (save && audio != null) {
            return saveAudioAndTextToFile(text, audioFileInCache, audio);
        }
        return null;
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

    protected String getCharactersFilePrefix(Calendar cal) {
        return cal.get(Calendar.MONTH) + "_" + cal.get(Calendar.YEAR);
    }

    protected abstract byte[] synthesizeSpeech(String text);

    protected void cleanOldCharacterFiles() {
        String currentCharactersFileName = getCharacters().getFileName().toString();
        for (File file : FileUtils.listFiles(cacheFolder.toFile(), new String[]{".txt"}, true)) {
            if (!file.getName().equals(currentCharactersFileName)) {
                log.info("Delete old character file <{}>", file.getName());
                file.delete();
            }
        }
    }

    @SneakyThrows
    private void cleanOldAudioFiles() {
        for (File file : FileUtils.listFiles(cacheFolder.toFile(), new String[]{".mp3"}, true)) {
            BasicFileAttributes attributes = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
            if (TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis() - attributes.lastAccessTime().toMillis()) > 31) {
                log.info("Delete old audio file <{}>", file.getName());
                file.delete();
            }
        }
    }

    private Path saveAudioAndTextToFile(String text, Path cacheFile, byte[] audio) {
        log.debug("Caching audio file {}", cacheFile.getFileName());
        CommonUtils.writeToFile(getCharacters(), text, true);
        return CommonUtils.writeToFile(cacheFile, audio, false);
    }

    @Override
    public void destroy(boolean forRestart, @Nullable Exception ex) {
        cleanOldCache();
    }

    @Override
    public void testService() {
        synthesizeSpeech("Hello world", false);
    }
}
