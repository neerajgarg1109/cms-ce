package com.enonic.cms.core.config;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import org.springframework.core.convert.ConversionService;

import java.io.File;
import java.util.Properties;

final class GlobalConfigBuilder
{
    private final Properties props;
    private final ConversionService converter;

    public GlobalConfigBuilder(final Properties props, final ConversionService converter)
    {
        this.props = props;
        this.converter = converter;
    }

    public GlobalConfig build()
    {
        final GlobalConfigImpl config = new GlobalConfigImpl();
        config.setMap(ImmutableMap.copyOf(Maps.fromProperties(this.props)));

        final File homeDir = getProperty("cms.home", File.class);

        config.setHomeDir(homeDir);
        config.setConfigDir(new File(homeDir, "config"));

        return config;
    }

    private <T> T getProperty(final String key, final Class<T> type)
    {
        return this.converter.convert(this.props.getProperty(key), type);
    }
}