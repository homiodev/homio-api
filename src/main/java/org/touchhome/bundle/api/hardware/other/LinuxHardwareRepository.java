package org.touchhome.bundle.api.hardware.other;

import lombok.SneakyThrows;
import org.apache.commons.lang3.SystemUtils;
import org.touchhome.bundle.api.EntityContext;
import org.touchhome.bundle.api.hquery.api.HQueryParam;
import org.touchhome.bundle.api.hquery.api.HardwareQuery;
import org.touchhome.bundle.api.hquery.api.HardwareRepositoryAnnotation;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Enumeration;

@HardwareRepositoryAnnotation
public interface LinuxHardwareRepository {

    @HardwareQuery("df -m / | sed -e /^Filesystem/d")
    HardwareMemory getSDCardMemory();

    @HardwareQuery("top -bn1 | grep load | awk '{printf \"%.2f%%\", $(NF-2)}'")
    String getCpuLoad();

    @HardwareQuery("free -m | awk 'NR==2{printf \"%s/%sMB\", $3,$2 }'")
    String getMemory();

    @HardwareQuery("uptime -p | cut -d 'p' -f 2 | awk '{ printf \"%s\", $0 }'")
    String getUptime();

    @HardwareQuery(value = "cat /proc/device-tree/model", cache = true)
    String catDeviceModel();

    @HardwareQuery("iwgetid -r")
    String getWifiName();

    @HardwareQuery("systemctl is-active :serviceName")
    int getServiceStatus(@HQueryParam("serviceName") String serviceName);

    @HardwareQuery(echo = "Reboot device", value = "reboot")
    void reboot();

    @HardwareQuery(value = "cat /etc/os-release", cache = true)
    HardwareOs getOs();

    @HardwareQuery("chmod :mode -R :path")
    void setPermissions(@HQueryParam("path") Path path, @HQueryParam("mode") int mode);

    @HardwareQuery(value = "apt-get install :soft", echo = "Install software")
    void installSoftware(@HQueryParam("soft") String soft);

    default String getDeviceModel() {
        return EntityContext.isLinuxEnvironment() ? catDeviceModel() : SystemUtils.OS_NAME;
    }

    @SneakyThrows
    default String getIpAddress() {
        if (EntityContext.isDevEnvironment()) {
            return "127.0.0.1";
        }
        Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();
        StringBuilder address = new StringBuilder();
        for (NetworkInterface networkInterface : Collections.list(nets)) {
            for (InetAddress inetAddress : Collections.list(networkInterface.getInetAddresses())) {
                if (inetAddress.isSiteLocalAddress()) {
                    address.append(inetAddress.getHostAddress()).append(",");
                }
            }
        }
        if (address.length() > 0) {
            address.deleteCharAt(address.length() - 1);
        }
        return address.toString();
    }
}