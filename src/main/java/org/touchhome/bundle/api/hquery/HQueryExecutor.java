package org.touchhome.bundle.api.hquery;

import org.touchhome.bundle.api.hquery.api.HardwareQuery;

import java.io.File;

public interface HQueryExecutor {
    String[] getValues(HardwareQuery hardwareQuery);

    default String updateCommand(String cmd) {
        return cmd;
    }

    Process createProcess(String[] cmdParts, String[] env, File dir);
}
