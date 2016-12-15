package com.example;

import de.greenrobot.daogenerator.DaoGenerator;
import de.greenrobot.daogenerator.Entity;
import de.greenrobot.daogenerator.Schema;

public class LocationSharingDaoGenerator {
    public static void main(String[] args) throws Exception {
        Schema schema = new Schema(1, "com.example.admin1.locationsharing.db.dao");
        addContactsTable(schema);
        addUserLastKnownLocationTable(schema);
        addUserLocationsTable(schema);
        new DaoGenerator().generateAll(schema, "./app/src/main/java");
    }
    private static void addContactsTable(Schema schema){
        Entity entity = schema.addEntity("Contacts");
        entity.addIdProperty().primaryKey().autoincrement();
        entity.addStringProperty("first_name");
        entity.addStringProperty("last_name");
        entity.addIntProperty("contact_id");
        entity.addStringProperty("phone");
        entity.addStringProperty("photo");
        entity.addBooleanProperty("is_modified");
        entity.addBooleanProperty("is_contact_added");
        entity.addBooleanProperty("is_location_requested");
        entity.addBooleanProperty("is_location_shared");
    }

    public static void addUserLocationsTable(Schema schema){
        Entity entity = schema.addEntity("UserLocations");
        entity.addIdProperty().primaryKey().autoincrement();
        entity.addStringProperty("phone");
        entity.addStringProperty("name");
        entity.addStringProperty("latitude");
        entity.addStringProperty("longitude");
        entity.addStringProperty("time");
    }
    public static void addUserLastKnownLocationTable(Schema schema){
        Entity entity = schema.addEntity("UserLastKnownLocation");
        entity.addStringProperty("name");
        entity.addStringProperty("phone").primaryKey();
        entity.addStringProperty("latitude");
        entity.addStringProperty("longitude");
    }
}

