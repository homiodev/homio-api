package org.touchhome.bundle.api.video.ffmpeg;

import lombok.experimental.Accessors;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

@Accessors(chain = true)
public class FFMPEGVideoDevice extends FFMPEGBaseDevice {

    private final Dimension[] resolutions;
    private Dimension resolution = null;

    protected FFMPEGVideoDevice(String resolutions) {
        this.resolutions = readResolutions(resolutions);
    }

    @Override
    public FFMPEGVideoDevice setName(String name) {
        return (FFMPEGVideoDevice) super.setName(name);
    }

    private Dimension[] readResolutions(String res) {
        List<Dimension> resolutions = new ArrayList<>();
        String[] parts = res.split(" ");

        for (String part : parts) {
            String[] xy = part.split("x");
            resolutions.add(new Dimension(Integer.parseInt(xy[0]), Integer.parseInt(xy[1])));
        }

        return resolutions.toArray(new Dimension[0]);
    }

    public Dimension[] getResolutions() {
        return resolutions;
    }

    public Dimension getResolution() {
        if (resolution == null) {
            resolution = getResolutions()[0];
        }
        return resolution;
    }

    public void setResolution(Dimension resolution) {
        this.resolution = resolution;
    }

    private String getResolutionString() {
        Dimension d = getResolution();
        return String.format("%dx%d", d.width, d.height);
    }

    private int arraySize() {
        return resolution.width * resolution.height * 3;
    }
}
