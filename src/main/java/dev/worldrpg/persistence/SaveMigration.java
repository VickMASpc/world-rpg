package dev.worldrpg.persistence;

public interface SaveMigration<T> {
    SaveSchemaVersion from();

    SaveSchemaVersion to();

    T migrate(T input);
}
