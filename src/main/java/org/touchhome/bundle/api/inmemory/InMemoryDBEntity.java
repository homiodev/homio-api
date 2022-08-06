package org.touchhome.bundle.api.inmemory;

import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;
import dev.morphia.annotations.Indexed;
import dev.morphia.utils.IndexDirection;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

@Getter
@NoArgsConstructor
@Entity
public abstract class InMemoryDBEntity implements Comparable<InMemoryDBEntity> {
    @Id
    private String id;

    private long created;

    @Indexed(IndexDirection.ASC)
    long updated; // package-private to able to update it inside InMemoryDB but not allow change by 3-th party libraries

    @Setter
    private Object value;

    public InMemoryDBEntity(Object value) {
        this(null, value);
    }

    public InMemoryDBEntity(String id, Object value) {
        this.id = id;
        this.created = System.currentTimeMillis();
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        InMemoryDBEntity that = (InMemoryDBEntity) o;
        return id != null ? id.equals(that.id) : that.id == null;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    @Override
    public int compareTo(@NotNull InMemoryDBEntity o) {
        return (int) (this.updated - o.updated);
    }
}
