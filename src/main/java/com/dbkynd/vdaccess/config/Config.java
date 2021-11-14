package com.dbkynd.vdaccess.config;

import com.dbkynd.vdaccess.VDAccess;
import com.moandjiezana.toml.Toml;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class Config {
    private static final Logger logger = VDAccess.logger;
    private Path dataDirectory = VDAccess.dataDirectory;
    File directory = new File(String.valueOf(dataDirectory));
    File file = new File(directory, "config.toml");

    public void init() {
        if (!directory.exists()) {
            directory.mkdir();
        }

        if (!file.exists()) {
            try (InputStream defaults = getClass().getResourceAsStream("/" + file.getName())) {
                if (defaults != null) {
                    Files.copy(defaults, file.toPath());
                }
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }
    }

    public Toml read() {
        return new Toml().read(file);
    }
}
