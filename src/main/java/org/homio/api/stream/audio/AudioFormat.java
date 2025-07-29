package org.homio.api.stream.audio;

import java.util.Objects;
import java.util.Set;
import lombok.Getter;
import org.homio.api.stream.StreamFormat;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.util.MimeType;

@Getter
public class AudioFormat implements StreamFormat {

  public static final String CONTAINER_NONE = "NONE";
  public static final String CONTAINER_WAVE = "WAVE";
  public static final String CONTAINER_OGG = "OGG";
  public static final String CODEC_PCM_SIGNED = "PCM_SIGNED";
  // generic pcm signed format (no container) without any further constraints
  public static final AudioFormat PCM_SIGNED =
      new AudioFormat(
          AudioFormat.CONTAINER_NONE, AudioFormat.CODEC_PCM_SIGNED, null, null, null, null);
  // generic wav format without any further constraints
  public static final AudioFormat WAV =
      new AudioFormat(
          AudioFormat.CONTAINER_WAVE, AudioFormat.CODEC_PCM_SIGNED, null, null, null, null);
  public static final String CODEC_PCM_UNSIGNED = "PCM_UNSIGNED";
  public static final String CODEC_PCM_ALAW = "ALAW";
  public static final String CODEC_PCM_ULAW = "ULAW";
  public static final String CODEC_MP3 = "MP3";

  public static final AudioFormat MP3 =
      new AudioFormat(
          AudioFormat.CONTAINER_NONE, AudioFormat.CODEC_MP3, null, null, null, null, null);
  public static final AudioFormat MP3_URL =
      new AudioFormat(AudioFormat.CONTAINER_NONE, AudioFormat.CODEC_MP3, false, 16, null, null);
  public static final String CODEC_VORBIS = "VORBIS";
  // generic OGG format without any further constraints
  public static final AudioFormat OGG =
      new AudioFormat(AudioFormat.CONTAINER_OGG, AudioFormat.CODEC_VORBIS, null, null, null, null);

  public static final String CODEC_AAC = "AAC";
  // generic AAC format without any further constraints
  public static final AudioFormat AAC =
      new AudioFormat(AudioFormat.CONTAINER_NONE, AudioFormat.CODEC_AAC, null, null, null, null);

  private final @Nullable String codec;
  private final @Nullable String container;
  private final @Nullable Boolean bigEndian;
  private final @Nullable Integer bitDepth;
  private final @Nullable Integer bitRate;
  private final @Nullable Long frequency;
  private final @Nullable Integer channels;

  public AudioFormat(
      @Nullable String container,
      @Nullable String codec,
      @Nullable Boolean bigEndian,
      @Nullable Integer bitDepth,
      @Nullable Integer bitRate,
      @Nullable Long frequency) {
    this(container, codec, bigEndian, bitDepth, bitRate, frequency, 1);
  }

  public AudioFormat(
      @Nullable String container,
      @Nullable String codec,
      @Nullable Boolean bigEndian,
      @Nullable Integer bitDepth,
      @Nullable Integer bitRate,
      @Nullable Long frequency,
      @Nullable Integer channels) {
    this.container = container;
    this.codec = codec;
    this.bigEndian = bigEndian;
    this.bitDepth = bitDepth;
    this.bitRate = bitRate;
    this.frequency = frequency;
    this.channels = channels;
  }

  /**
   * Determines the best match between a list of audio formats supported by a source and a sink.
   *
   * @param inputs the supported audio formats of an audio source
   * @param outputs the supported audio formats of an audio sink
   * @return the best matching format or null, if source and sink are incompatible
   */
  public static @Nullable AudioFormat getBestMatch(
      Set<AudioFormat> inputs, Set<AudioFormat> outputs) {
    AudioFormat preferredFormat = getPreferredFormat(inputs);
    if (preferredFormat != null) {
      for (AudioFormat output : outputs) {
        if (output.isCompatible(preferredFormat)) {
          return preferredFormat;
        } else {
          for (AudioFormat input : inputs) {
            if (output.isCompatible(input)) {
              return input;
            }
          }
        }
      }
    }
    return null;
  }

  /**
   * Gets the first concrete AudioFormat in the passed set or a preferred one based on 16bit, 16KHz,
   * big endian default
   *
   * @param audioFormats The AudioFormats from which to choose
   * @return The preferred AudioFormat or null if none could be determined. A passed concrete format
   *     is preferred adding default values to an abstract AudioFormat in the passed set.
   */
  public static @Nullable AudioFormat getPreferredFormat(Set<AudioFormat> audioFormats) {
    // Return the first concrete AudioFormat found
    for (AudioFormat currentAudioFormat : audioFormats) {
      // Check if currentAudioFormat is abstract
      if (null == currentAudioFormat.getCodec()) {
        continue;
      }
      if (null == currentAudioFormat.getContainer()) {
        continue;
      }
      if (null == currentAudioFormat.getBigEndian()) {
        continue;
      }
      if (null == currentAudioFormat.getBitDepth()) {
        continue;
      }
      if (null == currentAudioFormat.getBitRate()) {
        continue;
      }
      if (null == currentAudioFormat.getFrequency()) {
        continue;
      }

      // Prefer WAVE container
      if (!CONTAINER_WAVE.equals(currentAudioFormat.getContainer())) {
        continue;
      }

      // As currentAudioFormat is concrete, use it
      return currentAudioFormat;
    }

    // There's no concrete AudioFormat so we must create one
    for (AudioFormat currentAudioFormat : audioFormats) {
      // Define AudioFormat to return
      AudioFormat format = currentAudioFormat;

      // Not all Codecs and containers can be supported
      if (null == format.getCodec()) {
        continue;
      }
      if (null == format.getContainer()) {
        continue;
      }

      // Prefer WAVE container or raw SIGNED PCM encoded audio
      if (!CONTAINER_WAVE.equals(format.getContainer())
          && !(CONTAINER_NONE.equals(format.getContainer())
              && CODEC_PCM_SIGNED.equals(format.getCodec()))) {
        continue;
      }

      Integer channel = format.getChannels() == null ? Integer.valueOf(1) : format.getChannels();

      // If required set BigEndian, BitDepth, BitRate, and Frequency to default values
      if (null == format.getBigEndian()) {
        format =
            new AudioFormat(
                format.getContainer(),
                format.getCodec(),
                Boolean.TRUE,
                format.getBitDepth(),
                format.getBitRate(),
                format.getFrequency(),
                channel);
      }
      if (null == format.getBitDepth()
          || null == format.getBitRate()
          || null == format.getFrequency()) {
        // Define default values
        int defaultBitDepth = 16;
        long defaultFrequency = 16384;

        // Obtain current values
        Integer bitRate = format.getBitRate();
        Long frequency = format.getFrequency();
        Integer bitDepth = format.getBitDepth();

        // These values must be interdependent (bitRate = bitDepth * frequency)
        if (null == bitRate) {
          if (null == bitDepth) {
            bitDepth = defaultBitDepth;
          }
          if (null == frequency) {
            frequency = defaultFrequency;
          }
          bitRate = bitDepth * frequency.intValue();
        } else if (null == bitDepth) {
          if (null == frequency) {
            frequency = defaultFrequency;
          }
          bitDepth = bitRate / frequency.intValue();
        } else if (null == frequency) {
          frequency = bitRate.longValue() / bitDepth.longValue();
        }

        format =
            new AudioFormat(
                format.getContainer(),
                format.getCodec(),
                format.getBigEndian(),
                bitDepth,
                bitRate,
                frequency,
                channel);
      }

      // Return preferred AudioFormat
      return format;
    }

    // Return null indicating failure
    return null;
  }

  /**
   * Determines if the passed AudioFormat is compatible with this AudioFormat.
   *
   * <p>This AudioFormat is compatible with the passed AudioFormat if both have the same value for
   * all non-null members of this instance.
   */
  public boolean isCompatible(@Nullable AudioFormat audioFormat) {
    if (audioFormat == null) {
      return false;
    }
    if ((null != getContainer()) && (!getContainer().equals(audioFormat.getContainer()))) {
      return false;
    }
    if ((null != getCodec()) && (!getCodec().equals(audioFormat.getCodec()))) {
      return false;
    }
    if ((null != getBigEndian()) && (!getBigEndian().equals(audioFormat.getBigEndian()))) {
      return false;
    }
    if ((null != getBitDepth()) && (!getBitDepth().equals(audioFormat.getBitDepth()))) {
      return false;
    }
    if ((null != getBitRate()) && (!getBitRate().equals(audioFormat.getBitRate()))) {
      return false;
    }
    return (null == getFrequency()) || (getFrequency().equals(audioFormat.getFrequency()));
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    if (obj instanceof AudioFormat format) {
      return Objects.equals(getCodec(), format.getCodec())
          && //
          Objects.equals(getContainer(), format.getContainer())
          && //
          Objects.equals(getBigEndian(), format.getBigEndian())
          && //
          Objects.equals(getBitDepth(), format.getBitDepth())
          && //
          Objects.equals(getBitRate(), format.getBitRate())
          && //
          Objects.equals(getFrequency(), format.getFrequency())
          && //
          Objects.equals(getChannels(), format.getChannels());
    }
    return super.equals(obj);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((bigEndian == null) ? 0 : bigEndian.hashCode());
    result = prime * result + ((bitDepth == null) ? 0 : bitDepth.hashCode());
    result = prime * result + ((bitRate == null) ? 0 : bitRate.hashCode());
    result = prime * result + ((codec == null) ? 0 : codec.hashCode());
    result = prime * result + ((container == null) ? 0 : container.hashCode());
    result = prime * result + ((frequency == null) ? 0 : frequency.hashCode());
    result = prime * result + ((channels == null) ? 0 : channels.hashCode());
    return result;
  }

  @Override
  public String toString() {
    return "AudioFormat ["
        + (codec != null ? "codec=" + codec + ", " : "")
        + (container != null ? "container=" + container + ", " : "")
        + (bigEndian != null ? "bigEndian=" + bigEndian + ", " : "")
        + (bitDepth != null ? "bitDepth=" + bitDepth + ", " : "")
        + (bitRate != null ? "bitRate=" + bitRate + ", " : "")
        + (frequency != null ? "frequency=" + frequency + ", " : "")
        + (channels != null ? "channels=" + channels : "")
        + "]";
  }

  @Override
  public @NotNull MimeType getMimeType() {
    if (AudioFormat.CODEC_MP3.equals(codec)) {
      return new MimeType("audio", "mpeg");
    } else if (AudioFormat.CONTAINER_WAVE.equals(container)) {
      return new MimeType("audio", "wav");
    } else if (AudioFormat.CONTAINER_OGG.equals(container)) {
      return new MimeType("audio", "ogg");
    }
    throw new IllegalStateException("Unable to determine mime type");
  }
}
