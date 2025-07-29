package org.homio.api.entity.device;

import jakarta.persistence.*;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import org.homio.api.entity.validation.MaxItems;
import org.homio.api.ui.field.UIField;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public abstract class DeviceEntityAndSeries<S extends DeviceSeriesEntity> extends DeviceBaseEntity {

  @Getter
  @Setter
  @OrderBy("priority asc")
  @UIField(order = 30, hideInView = true)
  @MaxItems(10)
  @OneToMany(
      fetch = FetchType.EAGER,
      cascade = CascadeType.ALL,
      orphanRemoval = true,
      mappedBy = "deviceEntity",
      targetEntity = DeviceSeriesEntity.class)
  private Set<S> series;

  @Override
  protected long getChildEntityHashCode() {
    long code = 0;
    for (S s : series) {
      code += s.getEntityHashCode();
    }
    return code;
  }
}
