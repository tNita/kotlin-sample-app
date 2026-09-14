package com.example.bookmanager.domain;

import com.fasterxml.uuid.Generators;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Supplier;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/** 汎用ID（UUID v7固定）。 */
@NullMarked
public final class Id {
    private static final Supplier<UUID> GENERATOR = Generators.timeBasedEpochGenerator()::generate;

    private final UUID value;

    private Id(UUID value) {
        this.value = value;
    }

    public static Id generate() {
        return generate(GENERATOR);
    }

    public static Id generate(Supplier<UUID> uuidSupplier) {
        UUID uuid = Objects.requireNonNull(uuidSupplier.get(), "UUIDは必須です");
        if (uuid.version() != 7) {
            throw new IllegalArgumentException("IDはUUIDバージョン7で指定してください");
        }
        return new Id(uuid);
    }

    public UUID getValue() {
        return value;
    }

    @Override
    public boolean equals(@Nullable Object other) {
        return this == other || other instanceof Id id && value.equals(id.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public String toString() {
        return "Id(value=" + value + ")";
    }
}
