package org.homio.api.storage;

import java.util.concurrent.atomic.AtomicLong;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;

@Getter
@Setter
@ToString
public abstract class DataStorageEntity implements Comparable<DataStorageEntity> {

    private static final AtomicLong sequence = new AtomicLong();

    private long id = sequence.incrementAndGet();
    private long created = System.currentTimeMillis();

    @Override
    public boolean equals(Object o) {
        if (this == o) {return true;}
        if (o == null || getClass() != o.getClass()) {return false;}

        DataStorageEntity that = (DataStorageEntity) o;

        return id == that.id;
    }

    @Override
    public int hashCode() {
        return (int) (id ^ (id >>> 32));
    }

    @Override
    public int compareTo(@NotNull DataStorageEntity o) {
        return Long.compare(this.id, o.id);
    }

    public abstract Object getValue();
}
