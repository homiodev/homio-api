package org.touchhome.bundle.api.inmemory;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;

@Getter
@Setter
@ToString
public abstract class InMemoryDBEntity implements Comparable<InMemoryDBEntity> {

    private long id = System.currentTimeMillis();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        InMemoryDBEntity that = (InMemoryDBEntity) o;

        return id == that.id;
    }

    @Override
    public int hashCode() {
        return (int) (id ^ (id >>> 32));
    }

    @Override
    public int compareTo(@NotNull InMemoryDBEntity o) {
        return Long.compare(this.id, o.id);
    }
}
