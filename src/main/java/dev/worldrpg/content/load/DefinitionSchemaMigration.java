package dev.worldrpg.content.load;

import com.google.gson.JsonObject;
import dev.worldrpg.api.data.SchemaVersion;

public interface DefinitionSchemaMigration {
    SchemaVersion from();

    SchemaVersion to();

    JsonObject migrate(JsonObject input);
}
