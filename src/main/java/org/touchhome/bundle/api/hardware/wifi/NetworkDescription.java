package org.touchhome.bundle.api.hardware.wifi;

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
    @ListParse.LineParse("inet ((?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)).*")
    private String inet;

    @ListParse.LineParse(".*netmask ((?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)).*")
    private String netmask;

    @ListParse.LineParse(".*broadcast ((?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)).*")
    private String broadcast;
}