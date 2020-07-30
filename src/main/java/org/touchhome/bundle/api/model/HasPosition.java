package org.touchhome.bundle.api.model;

public interface HasPosition<T> {
    T setXb(int xb);

    T setYb(int yb);

    T setBw(int bw);

    T setBh(int bh);

    int getXb();

    int getYb();

    int getBw();

    int getBh();
}
