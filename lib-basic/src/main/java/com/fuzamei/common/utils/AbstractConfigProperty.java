package com.fuzamei.common.utils;

import android.content.Context;

import com.fuzamei.commonlib.R;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

/**
 * @author Mark
 * @since 2018/9/4
 * Description:
 */
public abstract class AbstractConfigProperty {
    public Properties config;

    public AbstractConfigProperty(Context context) {
        try {
            InputStream open = context.getAssets().open(getFileName());
            config = new Properties();
            config.load(new InputStreamReader(open, StandardCharsets.UTF_8));
            open.close();
        } catch (IOException e) {
            e.printStackTrace();
            LogUtils.e(context.getString(R.string.basic_error_open_property_file));
        }
    }

    public abstract String getFileName();
}
