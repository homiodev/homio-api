package org.homio.api.ui.route;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@UIRouteMenu(
    order = 100,
    icon = "fas fa-video",
    parent = UIRouteMenu.TopSidebarMenu.MEDIA,
    color = "#5950A7",
    allowCreateItem = true,
    overridePath = UIRouteMedia.ROUTE,
    filter = {"*:fas fa-filter:#8DBA73", "status:fas fa-heart-crack:#C452C4"},
    sort = {
      "name~#FF9800:fas fa-arrow-up-a-z:fas fa-arrow-down-z-a",
      "model~#28A4AD:fas fa-arrow-up-short-wide:fas fa-arrow-down-wide-short",
      "manufacturer~#28A4AD:fas fa-arrows-down-to-line:fas fa-arrows-down-to-line fa-rotate-180",
      "updated~#7EAD28:fas fa-clock-rotate-left:fas fa-clock-rotate-left fa-flip-horizontal",
      "status~#7EAD28:fas fa-turn-up:fas fa-turn-down",
      "place~#9C27B0:fas fa-location-dot:fas fa-location-dot fa-rotate-180"
    })
public @interface UIRouteMedia {
  String ROUTE = "media";

  String icon() default "";

  String color() default "";

  boolean allowCreateItem() default true;
}
