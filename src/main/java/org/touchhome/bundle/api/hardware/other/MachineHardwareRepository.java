package org.touchhome.bundle.api.hardware.other;

import org.apache.commons.lang3.SystemUtils;
import org.touchhome.bundle.api.EntityContext;
import org.touchhome.bundle.api.hquery.api.HQueryParam;
import org.touchhome.bundle.api.hquery.api.HardwareQuery;
import org.touchhome.bundle.api.hquery.api.HardwareRepositoryAnnotation;

import java.nio.file.Path;

@HardwareRepositoryAnnotation(stringValueOnDisable = "N/A")
public interface MachineHardwareRepository {

    @HardwareQuery(name = "Execute general command", value = ":command", win = ":command")
    String execute(@HQueryParam("command") String command);

    @HardwareQuery(name = "Get SD card memory", value = "df -m / | sed -e /^Filesystem/d")
    HardwareMemory getSDCardMemory();

    @HardwareQuery(name = "Get cpu load", value = "top -bn1 | grep load | awk '{printf \"%.2f%%\", $(NF-2)}'")
    String getCpuLoad();

    @HardwareQuery(name = "Get memory", value = "free -m | awk 'NR==2{printf \"%s/%sMB\", $3,$2 }'")
    String getMemory();

    @HardwareQuery(name = "Get uptime", value = "uptime -p | cut -d 'p' -f 2 | awk '{ printf \"%s\", $0 }'")
    String getUptime();

    @HardwareQuery(name = "Get device model", value = "cat /proc/device-tree/model", cacheValid = Integer.MAX_VALUE)
    String catDeviceModel();

    @HardwareQuery(name = "Get wifi name", value = "iwgetid -r")
    String getWifiName();

    @HardwareQuery(name = "Get service status", value = "systemctl is-active :serviceName")
    int getServiceStatus(@HQueryParam("serviceName") String serviceName);

    @HardwareQuery(name = "Reboot device", value = "reboot")
    void reboot();

    @HardwareQuery(name = "Get OS name", value = "cat /etc/os-release", cacheValid = Integer.MAX_VALUE)
    HardwareOs getOs();

    @HardwareQuery(name = "Change file permission", value = "chmod :mode -R :path")
    void setPermissions(@HQueryParam("path") Path path, @HQueryParam("mode") int mode);

    @HardwareQuery(name = "Install software", value = "apt-get install :soft", printOutput = true)
    void installSoftware(@HQueryParam("soft") String soft);

    @HardwareQuery(name = "Check software installed", value = "which :soft", win = "where :soft", cacheValid = 60)
    boolean isSoftwareInstalled(@HQueryParam("soft") String soft);

    default String getDeviceModel() {
        return EntityContext.isLinuxEnvironment() ? catDeviceModel() : SystemUtils.OS_NAME;
    }
}
