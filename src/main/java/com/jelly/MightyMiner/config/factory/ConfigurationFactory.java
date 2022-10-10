package com.jelly.MightyMiner.config.factory;

import com.jelly.MightyMiner.config.serdes.CoordsSerdes;
import eu.okaeri.configs.ConfigManager;
import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.yaml.snakeyaml.YamlSnakeYamlConfigurer;

import java.io.File;

public final class ConfigurationFactory {

    public <T extends OkaeriConfig> T create(Class<T> type, File file) {
        return ConfigManager.create(type, (config) -> {
            config.withConfigurer(new YamlSnakeYamlConfigurer(), new CoordsSerdes());
            config.withBindFile(file);
            config.saveDefaults();
            config.load();
        });
    }

}
