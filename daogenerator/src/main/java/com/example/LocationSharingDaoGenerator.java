package com.example;

import de.greenrobot.daogenerator.DaoGenerator;
import de.greenrobot.daogenerator.Entity;
import de.greenrobot.daogenerator.Schema;

public class LocationSharingDaoGenerator {
    public static void main(String[] args) throws Exception {
        Schema schema = new Schema(1, "com.example.admin1.locationsharing.db.dao");
        addUserDataTable(schema);
        new DaoGenerator().generateAll(schema, "./app/src/main/java");
    }
    private static void addUserDataTable(Schema schema) {
        Entity entity = schema.addEntity("UserDataTable");
        entity.addIntProperty("id").primaryKey();
        entity.addStringProperty("name");
        entity.addStringProperty("phone");
        entity.addStringProperty("latitude");
        entity.addStringProperty("longitude");
    }
}

