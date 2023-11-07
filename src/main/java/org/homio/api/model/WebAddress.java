package org.homio.api.model;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.io.IOException;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.homio.api.model.WebAddress.WebAddressSerializer;
import org.jetbrains.annotations.Nullable;

@Getter
@Setter
@JsonSerialize(using = WebAddressSerializer.class)
public class WebAddress {

    private final String address;
    private @Nullable String title;
    private @Nullable Icon icon = new Icon("fas fa-globe", "#3A7EC4");

    public WebAddress(String address) {
        this.address = address;
    }

    public WebAddress(String address, @Nullable String title) {
        this.address = address;
        this.title = title;
    }

    public WebAddress(String address, @Nullable String title, @Nullable Icon icon) {
        this.address = address;
        this.title = title;
        this.icon = icon;
    }

    public static class WebAddressSerializer extends JsonSerializer<WebAddress> {

        @Override
        public void serialize(WebAddress web, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            String value = web.icon == null ? "" : "<i class=\"%s\" style=\"color: %s\"></i>".formatted(web.icon.getIcon(), web.icon.getColor());

            String title = StringUtils.defaultString(web.title, web.address);
            String address = web.address.startsWith("http") ? web.address : "http://" + web.address;
            value += "<a target=\"_blank\" style=\"margin-left: 5px\" href=\"%s\">%s</a>".formatted(address, title);

            gen.writeString(value);
        }
    }
}
