package org.touchhome.bundle.api.hardware.network;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.touchhome.bundle.api.hquery.api.ListParse;

@Getter
@Setter
@ToString
@Accessors(chain = true)
public class NetworkDescription {
    public static final String IP_PATTERN = "((?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?))";

    @ListParse.LineParse("inet " + IP_PATTERN + ".*")
    private String inet;

    @ListParse.LineParse(".*netmask " + IP_PATTERN + ".*")
    private String netmask;

    @ListParse.LineParse(".*broadcast " + IP_PATTERN + ".*")
    private String broadcast;
}
