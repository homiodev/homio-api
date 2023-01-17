package org.touchhome.bundle.api.service;

import org.touchhome.bundle.api.model.StylePosition;

public interface ImageProviderService {
    byte[] setBrightness(byte[] image, float brightnessPercentage, String formatType);

    byte[] flipImage(byte[] image, boolean flipVertically, String formatType);

    byte[] cropImage(byte[] image, int x, int y, int width, int height, String formatType);

    byte[] overlayImage(
            byte[] bgImage, byte[] fgImage, int x, int y, int width, int height, String formatType);

    byte[] scaleImage(byte[] image, float scaleX, float scaleY, String formatType);

    byte[] resizeImage(byte[] image, int width, int height, String formatType);

    byte[] rotateImage(byte[] image, int angle, String formatType);

    byte[] translateImage(byte[] image, float tx, float ty, String formatType);

    byte[] addText(
            byte[] image,
            StylePosition stylePosition,
            String color,
            String text,
            String formatType);
}
