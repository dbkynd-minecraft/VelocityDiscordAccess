package com.dbkynd.vdaccess.http;

import com.dbkynd.vdaccess.mojang.MojangJSON;
import com.google.gson.Gson;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class WebRequest {

    public MojangJSON getMojangData(String username) {
        String url = "https://api.mojang.com/users/profiles/minecraft/" + username;
        try {
            String UUIDJson = IOUtils.toString(new URL(url), StandardCharsets.UTF_8);
            if (UUIDJson.isEmpty()) return null;
            Gson g = new Gson();
            return g.fromJson(UUIDJson, MojangJSON.class);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}
