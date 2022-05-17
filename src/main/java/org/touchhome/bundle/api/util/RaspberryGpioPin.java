package org.touchhome.bundle.api.util;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonValue;
import com.pi4j.io.gpio.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Getter
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
@RequiredArgsConstructor
public enum RaspberryGpioPin {
    PIN3(3, " I2C1 SDA", RaspiPin.GPIO_08, RaspiBcmPin.GPIO_02, "#D0BC7F", null),
    PIN5(5, " I2C1 SCL", RaspiPin.GPIO_09, RaspiBcmPin.GPIO_03, "#D0BC7F", null),
    PIN7(7, "   GPCLK0", RaspiPin.GPIO_07, RaspiBcmPin.GPIO_04, "#8CD1F8", PinMode.DIGITAL_INPUT),
    PIN8(8, "UART0 TXD", RaspiPin.GPIO_15, RaspiBcmPin.GPIO_14, "#DBB3A7", null),
    PIN10(10, "UART0 RXD", RaspiPin.GPIO_16, RaspiBcmPin.GPIO_15, "#DBB3A7", null),
    PIN11(11, "      FL1", RaspiPin.GPIO_00, RaspiBcmPin.GPIO_17, "#8CD1F8", PinMode.DIGITAL_INPUT),
    PIN12(12, "  PCM CLK", RaspiPin.GPIO_01, RaspiBcmPin.GPIO_18, "#8CD1F8", PinMode.DIGITAL_INPUT),
    PIN13(13, " SD0 DAT3", RaspiPin.GPIO_02, RaspiBcmPin.GPIO_27, "#8CD1F8", PinMode.DIGITAL_INPUT),
    PIN15(15, "  SD0 CLK", RaspiPin.GPIO_03, RaspiBcmPin.GPIO_22, "#8CD1F8", PinMode.DIGITAL_INPUT),
    PIN16(16, "  SD0 CMD", RaspiPin.GPIO_04, RaspiBcmPin.GPIO_23, "#8CD1F8", PinMode.DIGITAL_INPUT),
    PIN18(18, " SD0 DAT0", RaspiPin.GPIO_05, RaspiBcmPin.GPIO_24, "#8CD1F8", PinMode.DIGITAL_INPUT),
    PIN19(19, "SPI0 MOSI", RaspiPin.GPIO_12, RaspiBcmPin.GPIO_10, "#F1C16D", null),
    PIN21(21, "SPI0 MISO", RaspiPin.GPIO_13, RaspiBcmPin.GPIO_09, "#F1C16D", null),
    PIN22(22, " SD0 DAT1", RaspiPin.GPIO_06, RaspiBcmPin.GPIO_25, "#8CD1F8", PinMode.DIGITAL_INPUT),
    PIN23(23, "SPI0 SCLK", RaspiPin.GPIO_14, RaspiBcmPin.GPIO_11, "#F1C16D", null),
    PIN24(24, " SPI0 CE0", RaspiPin.GPIO_10, RaspiBcmPin.GPIO_08, "#F1C16D", PinMode.DIGITAL_OUTPUT),
    PIN26(26, " SPI0 CE1", RaspiPin.GPIO_11, RaspiBcmPin.GPIO_07, "#F1C16D", PinMode.DIGITAL_OUTPUT),

    PIN27(27, " I2C0 SDA", RaspiPin.GPIO_30, RaspiPin.GPIO_00, "#F595A3", null),
    PIN28(28, " I2C0 SCL", RaspiPin.GPIO_31, RaspiPin.GPIO_01, "#F595A3", null),

    PIN29(29, "   GPCLK1", RaspiPin.GPIO_21, RaspiBcmPin.GPIO_05, "#8CD1F8", PinMode.DIGITAL_INPUT),
    PIN31(31, "   GPCLK2", RaspiPin.GPIO_22, RaspiBcmPin.GPIO_06, "#8CD1F8", PinMode.DIGITAL_INPUT),
    PIN32(32, "     PWM0", RaspiPin.GPIO_26, RaspiBcmPin.GPIO_12, "#8CD1F8", PinMode.DIGITAL_INPUT),
    PIN33(33, "     PWM1", RaspiPin.GPIO_23, RaspiBcmPin.GPIO_13, "#8CD1F8", PinMode.DIGITAL_INPUT),
    PIN35(35, "   PCM FS", RaspiPin.GPIO_24, RaspiBcmPin.GPIO_19, "#8CD1F8", PinMode.DIGITAL_INPUT),
    PIN36(36, "      FL0", RaspiPin.GPIO_27, RaspiBcmPin.GPIO_16, "#8CD1F8", PinMode.DIGITAL_INPUT),
    PIN37(37, " SD0 DAT2", RaspiPin.GPIO_25, RaspiBcmPin.GPIO_26, "#8CD1F8", PinMode.DIGITAL_INPUT),
    PIN38(38, "  PCM DIN", RaspiPin.GPIO_28, RaspiBcmPin.GPIO_20, "#8CD1F8", PinMode.DIGITAL_INPUT),
    PIN40(40, " PCM DOUT", RaspiPin.GPIO_29, RaspiBcmPin.GPIO_21, "#8CD1F8", PinMode.DIGITAL_INPUT);

    private final int address;
    private final String name;
    private final Pin pin;
    private final Pin bcmPin;
    private final String color;
    private final PinMode pinMode;
    private String occupied;

    @JsonCreator
    public static RaspberryGpioPin fromValue(String value) {
        return Stream.of(RaspberryGpioPin.values()).filter(dp -> dp.pin.getName().equals(value)).findFirst().orElse(null);
    }

    public static void occupyPins(String device, RaspberryGpioPin... pins) {
        for (RaspberryGpioPin pin : pins) {
            pin.occupied = device;
        }
    }

    public static List<RaspberryGpioPin> values(PinMode pinMode, PinPullResistance pinPullResistance) {
        return Stream.of(RaspberryGpioPin.values())
                .filter(p ->
                        p.getPin().getSupportedPinModes().contains(pinMode) &&
                                (pinPullResistance == null || p.getPin().getSupportedPinPullResistance().contains(pinPullResistance)))
                .sorted(Comparator.comparingInt(o -> o.address))
                .collect(Collectors.toList());
    }


    @JsonValue
    public String toValue() {
        return name();
    }

    @Override
    public String toString() {
        return name + " (" + address + "/" + (bcmPin == null ? "" : bcmPin.getName()) + ")";
    }
}
