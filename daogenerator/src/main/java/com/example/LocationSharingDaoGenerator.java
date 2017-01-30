package com.example;

import de.greenrobot.daogenerator.DaoGenerator;
import de.greenrobot.daogenerator.Entity;
import de.greenrobot.daogenerator.Schema;

public class LocationSharingDaoGenerator {
    public static void main(String[] args) throws Exception {
        Schema schema = new Schema(1, "com.example.admin1.locationsharing.db.dao");
        addFriendsTable(schema);
        addUserLastKnownLocationTable(schema);
        addUserLocationsTable(schema);
        new DaoGenerator().generateAll(schema, "./app/src/main/java");
    }
    private static void addFriendsTable(Schema schema){
        Entity entity = schema.addEntity("Friends");
        entity.addIdProperty().primaryKey().autoincrement();
        entity.addStringProperty("friend_email");
        entity.addStringProperty("friend_first_name");
        entity.addStringProperty("friend_id");
        entity.addIntProperty("friend_request_id");
        entity.addStringProperty("requester_id");
        entity.addStringProperty("status");
    }

    public static void addUserLocationsTable(Schema schema){
        Entity entity = schema.addEntity("UserLocations");
        entity.addIdProperty().primaryKey().autoincrement();
        entity.addStringProperty("email");
        entity.addStringProperty("latitude");
        entity.addStringProperty("longitude");
        entity.addStringProperty("radius");
        entity.addStringProperty("time");
    }
    public static void addUserLastKnownLocationTable(Schema schema){
        Entity entity = schema.addEntity("UserLastKnownLocation");
        entity.addIdProperty().primaryKey().autoincrement();
        entity.addStringProperty("friend_first_name");
        entity.addStringProperty("email");
        entity.addStringProperty("latitude");
        entity.addStringProperty("longitude");
        entity.addStringProperty("time");
    }
}

