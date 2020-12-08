package org.touchhome.bundle.api.entity.micro;

import lombok.experimental.Accessors;

/**
 * Interface that micro controller type must implement for searching running devices
 */
@Accessors(chain = true)
public interface MicroControllerScanner {
    int scan();
}
