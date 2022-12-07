package org.touchhome.bundle.api.state;

import lombok.Getter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class HSBType extends State {

    // constants for the constituents
    public static final String KEY_HUE = "h";
    public static final String KEY_SATURATION = "s";
    public static final String KEY_BRIGHTNESS = "b";
    // constants for colors
    public static final HSBType BLACK = new HSBType("0,0,0");
    public static final HSBType WHITE = new HSBType("0,0,100");
    public static final HSBType RED = new HSBType("0,100,100");
    public static final HSBType GREEN = new HSBType("120,100,100");
    public static final HSBType BLUE = new HSBType("240,100,100");
    private static final long serialVersionUID = 322902950356613226L;
    // 1931 CIE XYZ to sRGB (D65 reference white)
    private static final float[][] XY2RGB = {{3.2406f, -1.5372f, -0.4986f}, {-0.9689f, 1.8758f, 0.0415f},
            {0.0557f, -0.2040f, 1.0570f}};

    // sRGB to 1931 CIE XYZ (D65 reference white)
    private static final float[][] RGB2XY = {{0.4124f, 0.3576f, 0.1805f}, {0.2126f, 0.7152f, 0.0722f},
            {0.0193f, 0.1192f, 0.9505f}};

    protected BigDecimal hue;
    protected BigDecimal saturation;

    @Getter
    private final BigDecimal value;

    public HSBType() {
        this("0,0,0");
    }

    /**
     * Constructs a HSBType instance with the given values
     *
     * @param h the hue value in the range from 0 <= h < 360
     * @param s the saturation as a percent value
     * @param b the brightness as a percent value
     */
    public HSBType(DecimalType h, DecimalType s, DecimalType b) {
        this.hue = h.toBigDecimal();
        this.saturation = s.toBigDecimal();
        this.value = b.toBigDecimal();
        validateValue(this.hue, this.saturation, this.value);
    }

    /**
     * Constructs a HSBType instance from a given string.
     * The string has to be in comma-separated format with exactly three segments, which correspond to the hue,
     * saturation and brightness values.
     * where the hue value in the range from 0 <= h < 360 and the saturation and brightness are percent values.
     *
     * @param value a stringified HSBType value in the format "hue,saturation,brightness"
     */
    public HSBType(String value) {
        List<String> constituents = Arrays.stream(value.split(",")).map(in -> in.trim()).collect(Collectors.toList());
        if (constituents.size() == 3) {
            this.hue = new BigDecimal(constituents.get(0));
            this.saturation = new BigDecimal(constituents.get(1));
            this.value = new BigDecimal(constituents.get(2));
            validateValue(this.hue, this.saturation, this.value);
        } else {
            throw new IllegalArgumentException(value + " is not a valid HSBType syntax");
        }
    }

    public static HSBType valueOf(String value) {
        return new HSBType(value);
    }

    /**
     * Create HSB from RGB
     *
     * @param r red 0-255
     * @param g green 0-255
     * @param b blue 0-255
     */
    public static HSBType fromRGB(int r, int g, int b) {
        float tmpHue, tmpSaturation, tmpBrightness;
        int max = (r > g) ? r : g;
        if (b > max) {
            max = b;
        }
        int min = (r < g) ? r : g;
        if (b < min) {
            min = b;
        }
        tmpBrightness = max / 2.55f;
        tmpSaturation = (max != 0 ? ((float) (max - min)) / ((float) max) : 0) * 100;
        if (tmpSaturation == 0) {
            tmpHue = 0;
        } else {
            float red = ((float) (max - r)) / ((float) (max - min));
            float green = ((float) (max - g)) / ((float) (max - min));
            float blue = ((float) (max - b)) / ((float) (max - min));
            if (r == max) {
                tmpHue = blue - green;
            } else if (g == max) {
                tmpHue = 2.0f + red - blue;
            } else {
                tmpHue = 4.0f + green - red;
            }
            tmpHue = tmpHue / 6.0f * 360;
            if (tmpHue < 0) {
                tmpHue = tmpHue + 360.0f;
            }
        }

        return new HSBType(new DecimalType((int) tmpHue), new DecimalType((int) tmpSaturation),
                new DecimalType((int) tmpBrightness));
    }

    /**
     * Returns a HSBType object representing the provided xy color values in CIE XY color model.
     * Conversion from CIE XY color model to sRGB using D65 reference white
     * Returned color is set to full brightness
     *
     * @param x, y color information 0.0 - 1.0
     * @return new HSBType object representing the given CIE XY color, full brightness
     */
    public static HSBType fromXY(float x, float y) {
        float tmpY = 1.0f;
        float tmpX = (tmpY / y) * x;
        float tmpZ = (tmpY / y) * (1.0f - x - y);

        float r = tmpX * XY2RGB[0][0] + tmpY * XY2RGB[0][1] + tmpZ * XY2RGB[0][2];
        float g = tmpX * XY2RGB[1][0] + tmpY * XY2RGB[1][1] + tmpZ * XY2RGB[1][2];
        float b = tmpX * XY2RGB[2][0] + tmpY * XY2RGB[2][1] + tmpZ * XY2RGB[2][2];

        float max = r > g ? r : g;
        if (b > max) {
            max = b;
        }

        r = gammaCompress(r / max);
        g = gammaCompress(g / max);
        b = gammaCompress(b / max);

        return HSBType.fromRGB((int) (r * 255.0f + 0.5f), (int) (g * 255.0f + 0.5f), (int) (b * 255.0f + 0.5f));
    }

    // Gamma compression (sRGB) for a single component, in the 0.0 - 1.0 range
    private static float gammaCompress(float c) {
        if (c < 0.0f) {
            c = 0.0f;
        } else if (c > 1.0f) {
            c = 1.0f;
        }

        return c <= 0.0031308f ? 12.92f * c : (1.0f + 0.055f) * (float) Math.pow(c, 1.0f / 2.4f) - 0.055f;
    }

    // Gamma decompression (sRGB) for a single component, in the 0.0 - 1.0 range
    private static float gammaDecompress(float c) {
        if (c < 0.0f) {
            c = 0.0f;
        } else if (c > 1.0f) {
            c = 1.0f;
        }

        return c <= 0.04045f ? c / 12.92f : (float) Math.pow((c + 0.055f) / (1.0f + 0.055f), 2.4f);
    }

   /* @Override
    public SortedMap<String, PrimitiveType> getConstituents() {
        TreeMap<String, PrimitiveType> map = new TreeMap<>();
        map.put(KEY_HUE, getHue());
        map.put(KEY_SATURATION, getSaturation());
        map.put(KEY_BRIGHTNESS, getBrightness());
        return map;
    }*/

    @Override
    public String stringValue() {
        return value.toPlainString();
    }

    private void validateValue(BigDecimal hue, BigDecimal saturation, BigDecimal value) {
        if (BigDecimal.ZERO.compareTo(hue) > 0 || BigDecimal.valueOf(360).compareTo(hue) <= 0) {
            throw new IllegalArgumentException("Hue must be between 0 and 360");
        }
        if (BigDecimal.ZERO.compareTo(saturation) > 0 || BigDecimal.valueOf(100).compareTo(saturation) < 0) {
            throw new IllegalArgumentException("Saturation must be between 0 and 100");
        }
        if (BigDecimal.ZERO.compareTo(value) > 0 || BigDecimal.valueOf(100).compareTo(value) < 0) {
            throw new IllegalArgumentException("Brightness must be between 0 and 100");
        }
    }

    public DecimalType getHue() {
        return new DecimalType(hue);
    }

    public DecimalType getSaturation() {
        return new DecimalType(saturation);
    }

    public DecimalType getBrightness() {
        return new DecimalType(value);
    }

    public DecimalType getRed() {
        return toRGB()[0];
    }

    public DecimalType getGreen() {
        return toRGB()[1];
    }

    public DecimalType getBlue() {
        return toRGB()[2];
    }

    /**
     * Returns the RGB value representing the color in the default sRGB
     * color model.
     * (Bits 24-31 are alpha, 16-23 are red, 8-15 are green, 0-7 are blue).
     *
     * @return the RGB value of the color in the default sRGB color model
     */
    public int getRGB() {
        DecimalType[] rgb = toRGB();
        return ((0xFF) << 24) | ((convertPercentToByte(rgb[0]) & 0xFF) << 16)
                | ((convertPercentToByte(rgb[1]) & 0xFF) << 8) | ((convertPercentToByte(rgb[2]) & 0xFF) << 0);
    }

    @Override
    public String toString() {
        return toFullString();
    }

    public String toFullString() {
        return getHue() + "," + getSaturation() + "," + getBrightness();
    }

    @Override
    public int hashCode() {
        int tmp = 10000 * getHue().hashCode();
        tmp += 100 * getSaturation().hashCode();
        tmp += getBrightness().hashCode();
        return tmp;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof HSBType)) {
            return false;
        }
        HSBType other = (HSBType) obj;
        return getHue().equals(other.getHue()) && getSaturation().equals(other.getSaturation())
                && getBrightness().equals(other.getBrightness());
    }

    public DecimalType[] toRGB() {
        DecimalType red = null;
        DecimalType green = null;
        DecimalType blue = null;

        BigDecimal h = hue.divide(BigDecimal.valueOf(100), 10, BigDecimal.ROUND_HALF_UP);
        BigDecimal s = saturation.divide(BigDecimal.valueOf(100));

        int hInt = h.multiply(BigDecimal.valueOf(5)).divide(BigDecimal.valueOf(3), 10, BigDecimal.ROUND_HALF_UP)
                .intValue();
        BigDecimal f = h.multiply(BigDecimal.valueOf(5)).divide(BigDecimal.valueOf(3), 10, BigDecimal.ROUND_HALF_UP)
                .remainder(BigDecimal.ONE);
        DecimalType a = new DecimalType(value.multiply(BigDecimal.ONE.subtract(s)));
        DecimalType b = new DecimalType(value.multiply(BigDecimal.ONE.subtract(s.multiply(f))));
        DecimalType c = new DecimalType(
                value.multiply(BigDecimal.ONE.subtract((BigDecimal.ONE.subtract(f)).multiply(s))));

        switch (hInt) {
            case 0:
            case 6:
                red = getBrightness();
                green = c;
                blue = a;
                break;
            case 1:
                red = b;
                green = getBrightness();
                blue = a;
                break;
            case 2:
                red = a;
                green = getBrightness();
                blue = c;
                break;
            case 3:
                red = a;
                green = b;
                blue = getBrightness();
                break;
            case 4:
                red = c;
                green = a;
                blue = getBrightness();
                break;
            case 5:
                red = getBrightness();
                green = a;
                blue = b;
                break;
            default:
                throw new IllegalArgumentException("Could not convert to RGB.");
        }
        return new DecimalType[]{red, green, blue};
    }

    /**
     * Returns the xyY values representing this object's color in CIE XY color model.
     * Conversion from sRGB to CIE XY using D65 reference white
     * xy pair contains color information
     * Y represents relative luminance
     *
     * @return DecimalType[x, y, Y] values in the CIE XY color model
     */
    public DecimalType[] toXY() {
        // This makes sure we keep color information even if brightness is zero
        DecimalType[] sRGB = new HSBType(getHue(), getSaturation(), DecimalType.HUNDRED).toRGB();

        float r = gammaDecompress(sRGB[0].floatValue() / 100.0f);
        float g = gammaDecompress(sRGB[1].floatValue() / 100.0f);
        float b = gammaDecompress(sRGB[2].floatValue() / 100.0f);

        float tmpX = r * RGB2XY[0][0] + g * RGB2XY[0][1] + b * RGB2XY[0][2];
        float tmpY = r * RGB2XY[1][0] + g * RGB2XY[1][1] + b * RGB2XY[1][2];
        float tmpZ = r * RGB2XY[2][0] + g * RGB2XY[2][1] + b * RGB2XY[2][2];

        float x = tmpX / (tmpX + tmpY + tmpZ);
        float y = tmpY / (tmpX + tmpY + tmpZ);

        return new DecimalType[]{new DecimalType(x * 100.0f),
                new DecimalType(y * 100.0f),
                new DecimalType(tmpY * getBrightness().floatValue())};
    }

    private int convertPercentToByte(DecimalType percent) {
        return percent.getValue().multiply(BigDecimal.valueOf(255))
                .divide(BigDecimal.valueOf(100), 2, BigDecimal.ROUND_HALF_UP).intValue();
    }

    @Override
    public float floatValue() {
        throw new IllegalStateException("Unable to fetch float value from HSB type");
    }

    @Override
    public int intValue() {
        throw new IllegalStateException("Unable to fetch int value from HSB type");
    }

    @Override
    public boolean boolValue() {
        throw new IllegalStateException("Unable to fetch boolean value from HSB type");
    }

    public <T extends State> T as(Class<T> target) {
        if (target == OnOffType.class) {
            // if brightness is not completely off, we consider the state to be on
            return target.cast(getBrightness().equals(DecimalType.ZERO) ? OnOffType.OFF : OnOffType.ON);
        } else if (target == DecimalType.class) {
            return target.cast(new DecimalType(
                    getBrightness().toBigDecimal().divide(BigDecimal.valueOf(100), 8, RoundingMode.UP)));
        } else if (target == DecimalType.class) {
            return target.cast(new DecimalType(getBrightness().toBigDecimal()));
        } else {
            return super.as(target);
        }
    }
}
