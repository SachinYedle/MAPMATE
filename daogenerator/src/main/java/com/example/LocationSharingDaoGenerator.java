package com.example;

import de.greenrobot.daogenerator.DaoGenerator;
import de.greenrobot.daogenerator.Entity;
import de.greenrobot.daogenerator.Schema;

public class LocationSharingDaoGenerator {
    public static void main(String[] args) throws Exception {
        Schema schema = new Schema(1, "com.example.admin1.locationsharing.db.dao");
        //addUserDataTable(schema);
        addSharedContactTable(schema);
        new DaoGenerator().generateAll(schema, "./app/src/main/java");
    }
    private static void addSharedContactTable(Schema schema){
        Entity entity = schema.addEntity("SharedContactTable");
        entity.addIntProperty("id").primaryKey();
        entity.addStringProperty("name");
        entity.addStringProperty("phone");
    }
}

