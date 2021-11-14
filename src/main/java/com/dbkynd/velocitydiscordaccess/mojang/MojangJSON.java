package com.dbkynd.velocitydiscordaccess.mojang;

import java.util.UUID;

public class MojangJSON {
    String id;
    String name;

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public UUID getUUID() {
        String id = this.id;
        id = addChar(id, '-', 8);
        id = addChar(id, '-', 13);
        id = addChar(id, '-', 18);
        id = addChar(id, '-', 23);
        return UUID.fromString(id);
    }

    private String addChar(String str, char ch, int position) {
        return str.substring(0, position) + ch + str.substring(position);
    }
}
