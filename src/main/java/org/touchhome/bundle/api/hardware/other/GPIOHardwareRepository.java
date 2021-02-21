package org.touchhome.bundle.api.hardware.other;

import org.touchhome.bundle.api.hquery.api.HQueryParam;
import org.touchhome.bundle.api.hquery.api.HardwareQuery;
import org.touchhome.bundle.api.hquery.api.HardwareRepositoryAnnotation;

@HardwareRepositoryAnnotation
public interface GPIOHardwareRepository {
    @HardwareQuery(name = "Printing wiring PI info", value = "gpio readall", printOutput = true)
    boolean printWiringPiInfo();

    @HardwareQuery(name = "Install GPIO", value = "$PM install wiringpi", printOutput = true, ignoreOnError = true)
    void installWiringPiAuto();

    @HardwareQuery(name = "MkDir pi", value = "mkdir buildWiringPi")
    @HardwareQuery(name = "Copy files", value = "cp :sysDir/WiringPi-master.zip buildWiringPi/")
    @HardwareQuery(name = "Unzip files", value = "unzip buildWiringPi/WiringPi-master.zip -d buildWiringPi/")
    @HardwareQuery(name = "Fire build", value = "./build", dir = ":tomcatDir/buildWiringPi/WiringPi-master")
    @HardwareQuery(name = "Remove files", value = "rm -rf buildWiringPi")
    void installWiringPiManually(@HQueryParam("sysDir") String sysDir, @HQueryParam("tomcatDir") String tomcatDir);
}
