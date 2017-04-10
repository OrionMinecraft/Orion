package eu.mikroskeem.orion.impl.configuration;

import eu.mikroskeem.orion.api.server.Configuration;
import eu.mikroskeem.shuriken.common.Ensure;
import eu.mikroskeem.shuriken.common.SneakyThrow;
import lombok.Getter;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.loader.HeaderMode;
import ninja.leaping.configurate.objectmapping.ObjectMapper;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;

import java.io.IOException;
import java.nio.file.Path;

/**
 * @author Mark Vainomaa
 */
public class HoconFileConfiguration implements Configuration {
    private final static String DEFAULT_HEADER =
            "Orion Core configuration.\n" +
            "You can reload configuration using '/orion reload' command\n" +
            "\n" +
            "DO NOT EDIT UNLESS YOU KNOW WHAT YOU ARE DOING.\n" +
            "Failure to understand this warning may lead to make server\n" +
            "trying to kill your cat or steal your pizza";

    @Getter private final Messages messages;
    @Getter private final Debug debug;
    @Getter private final Sentry sentry;

    /* Configuration loader/instance */
    private final ConfigurationLoader<CommentedConfigurationNode> loader;
    private ObjectMapper<OrionConfiguration>.BoundInstance orionConfigurationMapper = null;
    private CommentedConfigurationNode baseNode = null;
    private OrionConfiguration orionConfiguration = null;

    public HoconFileConfiguration(Path configurationPath) {
        loader = HoconConfigurationLoader
                .builder()
                .setDefaultOptions(getDefaultOptions())
                .setHeaderMode(HeaderMode.PRESERVE)
                .setPath(configurationPath)
                .build();

        try {
            orionConfigurationMapper = ObjectMapper.forClass(OrionConfiguration.class).bindToNew();
            orionConfiguration = orionConfigurationMapper.getInstance();
        } catch (ObjectMappingException e){
            SneakyThrow.throwException(e);
        }

        /* delete these */
        messages = new HoconMessages();
        debug = new HoconDebug();
        sentry = new HoconSentry();
    }

    @Override
    public void load() throws IOException {
        try {
            baseNode = loader.load();
            orionConfiguration = orionConfigurationMapper.populate(baseNode.getNode("orion"));
        } catch (ObjectMappingException e){
            throw new IOException(e);
        }
    }

    @Override
    public void save() throws IOException {
        try {
            orionConfigurationMapper.serialize(baseNode.getNode("orion"));
            loader.save(baseNode);
        } catch (ObjectMappingException e){
            throw new IOException(e);
        }
    }

    @Override
    public void reload() throws IOException {
        load();
        save();
    }

    /* Configuration options */
    private ConfigurationOptions getDefaultOptions(){
        return ConfigurationOptions.defaults()
            .setHeader(DEFAULT_HEADER)
            .setShouldCopyDefaults(true);
    }

    /* Subconfiguration implementations */
    public class HoconMessages implements Messages {
        @Override
        public boolean isOverridingPluginPermissionDeniedMessageEnabled() {
            Ensure.notNull(orionConfiguration, "Configuration is not loaded yet!");
            return orionConfiguration.getMessages().isOverridingPluginPermissionDeniedMessageEnabled();
        }

        @Override
        public String getCommandPermissionDeniedMessage() {
            Ensure.notNull(orionConfiguration, "Configuration is not loaded yet!");
            return orionConfiguration.getMessages().getPermissionDeniedMessage();
        }
    }

    public class HoconDebug implements Debug {
        @Override
        public boolean isEventDumpingAllowed() {
            Ensure.notNull(orionConfiguration, "Configuration is not loaded yet!");
            return orionConfiguration.getDebug().isEventDumpingAllowed();
        }

        @Override
        public boolean isScriptEventHandlerAllowed() {
            Ensure.notNull(orionConfiguration, "Configuration is not loaded yet!");
            return orionConfiguration.getDebug().isScriptEventHandlerAllowed();
        }

        @Override
        public boolean isReportingEventExceptionsToSentryAllowed() {
            Ensure.notNull(orionConfiguration, "Configuration is not loaded yet!");
            return orionConfiguration.getDebug().isReportingEventExceptionsToSentryAllowed();
        }

        @Override
        public boolean isReportingCommandExceptionsToSentryAllowed() {
            Ensure.notNull(orionConfiguration, "Configuration is not loaded yet!");
            return orionConfiguration.getDebug().isReportingCommandExceptionsToSentryAllowed();
        }

        @Override
        public String getHastebinUrl() {
            Ensure.notNull(orionConfiguration, "Configuration is not loaded yet!");
            return orionConfiguration.getDebug().getHastebinUrl();
        }
    }

    public class HoconSentry implements Sentry {
        @Override
        public String getSentryDSN() {
            Ensure.notNull(orionConfiguration, "Configuration is not loaded yet!");
            return orionConfiguration.getSentry().getSentryDSN();
        }
    }
}
