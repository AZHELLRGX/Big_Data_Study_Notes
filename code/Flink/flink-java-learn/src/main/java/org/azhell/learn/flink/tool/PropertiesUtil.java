package org.azhell.learn.flink.tool;

import org.apache.commons.lang3.StringUtils;
import org.apache.flink.api.java.utils.ParameterTool;

import java.util.Properties;
import java.util.ResourceBundle;

/**
 * Properties 工具类.
 */
public final class PropertiesUtil {
    private static final ParameterTool overridePt = ParameterTool.fromArgs(new String[0]);
    
    private PropertiesUtil() {
        throw new IllegalStateException("工具类不允许被实例化");
    }

    private static Properties toProperties(ResourceBundle bundle) {
        final Properties properties = new Properties();

        for (String key : bundle.keySet()) {
            final String value = bundle.getString(key);
            if (StringUtils.isNoneEmpty(value)) {
                properties.setProperty(key, value);
            }
        }
        return properties;
    }

    public static Properties getBundleWithParameterOverride(String baseName) {
        return getBundleWithParameterOverride(baseName, overridePt);
    }

    public static Properties getBundleWithParameterOverride(String baseName, ParameterTool pt) {
        final ResourceBundle bundle = ResourceBundle.getBundle(baseName);
        final Properties res = toProperties(bundle);
        for (Object o : res.keySet()) {
            final String key = o.toString();
            final String value = pt.get(key);
            if (StringUtils.isNoneEmpty(value)) {
                // override
                res.setProperty(key, value);
            }
        }
        return res;
    }

}
