/*
 * This file is part of project Orion, licensed under the MIT License (MIT).
 *
 * Copyright (c) 2017-2018 Mark Vainomaa <mikroskeem@mikroskeem.eu>
 * Copyright (c) Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package eu.mikroskeem.orion.api.configuration;

import eu.mikroskeem.orion.api.annotations.ConfigurationBaseNode;
import eu.mikroskeem.shuriken.common.SneakyThrow;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.loader.HeaderMode;
import ninja.leaping.configurate.objectmapping.ObjectMapper;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;

/**
 * Object mapping configuration loader using Configurate library
 *
 * @author Mark Vainomaa
 */
public class ObjectConfigurationLoader<T> {
    @NotNull private final String baseNodeName;
    @NotNull private final Class<T> configClass;
    @Nullable private final String header;

    /** Configuration file absolute location */
    @NotNull private final Path configurationPath;

    /** Configuration loader instance */
    @NotNull private final ConfigurationLoader<CommentedConfigurationNode> loader;

    /** Object mapper instance for {@code T} */
    @NotNull private final ObjectMapper<T>.BoundInstance mapper;

    /** Configuration lodaer base node */
    @Nullable private CommentedConfigurationNode baseNode;

    /** Configuration instance */
    @Nullable private T configuration;

    /** This configuration loader's default options */
    private ConfigurationOptions getDefaultOptions() {
        ConfigurationOptions options = ConfigurationOptions.defaults();
        if(header != null)
            options = options.setHeader(header);
        return options;
    }

    public ObjectConfigurationLoader(@NotNull Path configurationFile, @NotNull Class<T> configClass,
                                     @Nullable String baseNodeName, @Nullable String header) {
        this.configurationPath = configurationFile.toAbsolutePath();
        this.configClass = configClass;
        this.header = header;

        // Validate configuration file
        if(Files.isDirectory(configurationPath))
            throw new IllegalStateException("Path " + configurationPath.toString() + " is a directory!");

        try {
            if(Files.notExists(configurationPath))
                Files.createDirectories(configurationPath.getParent());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        // Validate configuration class
        if(!Modifier.isPublic(configClass.getModifiers()))
            throw new IllegalStateException("configClass must be public!");

        if(Arrays.stream(configClass.getAnnotations()).noneMatch(a -> a instanceof ConfigSerializable))
            throw new IllegalStateException("configClass must have @ConfigSerializable annotation!");

        // Check if annotation defining base node name is present, and use annotation name from there
        ConfigurationBaseNode baseNodeAnnotation = configClass.getAnnotation(ConfigurationBaseNode.class);
        if(baseNodeAnnotation != null)
            baseNodeName = baseNodeAnnotation.value();

        this.baseNodeName = baseNodeName != null && !baseNodeName.isEmpty() ? baseNodeName : configClass.getName()
                .substring(configClass.getName().lastIndexOf('.') + 1)
                .toLowerCase(Locale.ENGLISH);

        // Validate base node name
        if(this.baseNodeName.isEmpty())
            throw new IllegalStateException("baseNodeName must not be empty!");

        // Build configuration loader
        loader = HoconConfigurationLoader.builder()
                .setDefaultOptions(getDefaultOptions())
                .setHeaderMode(HeaderMode.PRESERVE)
                .setPath(configurationPath)
                .build();

        // Build object mapper
        try {
            mapper = ObjectMapper.forClass(configClass).bindToNew();
        } catch (ObjectMappingException e) {
            SneakyThrow.throwException(e);
            throw null;
        }
    }

    public ObjectConfigurationLoader(@NotNull Path configurationFile, @NotNull Class<T> configClass) {
        this(configurationFile, configClass, null, null);
    }

    /**
     * Loads configuration
     */
    public void load() {
        try {
            baseNode = loader.load();
            configuration = mapper.populate(Objects.requireNonNull(baseNode).getNode(baseNodeName));
        } catch (ObjectMappingException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Saves configuration
     */
    public void save() {
        try {
            mapper.serialize(Objects.requireNonNull(baseNode).getNode(baseNodeName));
            loader.save(baseNode);
        } catch (ObjectMappingException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Gets configuration class instance
     *
     * @return Instance of {@code T}
     */
    @Contract(pure = true)
    public T getConfiguration() {
        return Objects.requireNonNull(configuration);
    }

    /**
     * Gets wrapped configuation loader instance
     *
     * @return Wrapped configuration loader instance
     */
    @NotNull
    public ConfigurationLoader<CommentedConfigurationNode> getLoader() {
        return loader;
    }
}
