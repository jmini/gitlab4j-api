package org.gitlab4j.api.mockserver;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Properties;

public class GitlabMockData {

    private Properties data;

    public GitlabMockData(Path file) {
        data = loadProperties(file);
    }

    public String getHostUrl() {
        return get("HOST");
    }

    public String getPersonalAccessToken() {
        return get("PAT");
    }

    public Long getUserId() {
        return getLong("user.id");
    }

    private Long getLong(String key) {
        String value = get(key);
        return Long.valueOf(value);
    }

    private String get(String key) {
        if (!data.containsKey(key)) {
            throw new IllegalStateException("MockData properties file must contains key '" + key + "'");
        }
        String value = data.getProperty(key);
        if (value == null) {
            throw new IllegalStateException("MockData properties value for key '" + key + "' can't be null");
        }
        return value;
    }

    private static Properties loadProperties(Path file) {
        try (FileInputStream inStream = new FileInputStream(file.toFile())) {
            Properties properties = new Properties();
            properties.load(inStream);
            return properties;
        } catch (IOException e) {
            throw new IllegalStateException("Can not load properties file " + file, e);
        }
    }
}
