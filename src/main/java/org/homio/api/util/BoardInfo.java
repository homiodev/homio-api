package org.homio.api.util;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;

@Getter
public final class BoardInfo {

    public static final String processor;
    public static final String bogoMIPS;
    public static final String[] features;
    public static final String cpuImplementer;
    public static final String cpuArchitecture;
    public static final String cpuVariant;
    public static final String cpuPart;
    public static final String cpuRevision;
    public static final String hardware;
    public static final String revision;
    public static final String serial;

    static {
        Map<String, String> cpuInfo = getCpuInfo();
        revision = cpuInfo.get("Revision");
        processor = cpuInfo.get("processor");
        bogoMIPS = cpuInfo.get("BogoMIPS");
        features = cpuInfo.getOrDefault("Features", "").split(" ");
        hardware = cpuInfo.get("Hardware");
        cpuImplementer = cpuInfo.get("CPU implementer");
        cpuArchitecture = cpuInfo.get("CPU architecture");
        cpuVariant = cpuInfo.get("CPU variant");
        cpuPart = cpuInfo.get("CPU part");
        cpuRevision = cpuInfo.get("CPU revision");
        serial = cpuInfo.get("Serial");
    }

    private static Map<String, String> getCpuInfo() {
        Map<String, String> cpuInfo = new HashMap<>();
        try {
            for (String line : Files.readAllLines(Paths.get("/proc/cpuinfo"))) {
                String[] parts = line.split(":", 2);
                if (parts.length >= 2 && !parts[0].trim().isEmpty() && !parts[1].trim().isEmpty()) {
                    String cpuKey = parts[0].trim();
                    cpuInfo.put(cpuKey, parts[1].trim());
                }
            }
        } catch (Exception ignore) {

        }
        return cpuInfo;
    }
}
