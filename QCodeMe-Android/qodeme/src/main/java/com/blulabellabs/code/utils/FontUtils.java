package com.blulabellabs.code.utils;

import android.content.res.AssetManager;
import android.graphics.Typeface;

import java.lang.reflect.Field;

/**
 * Created by Alex on 1/23/14.
 */
public class FontUtils {

    public static void setDefaultFontFormAssets(AssetManager assetManager, String regularFont, String boldFont, String italicFont, String boldItalicFont) {
        try {
            final Typeface regular = Typeface.createFromAsset(assetManager, regularFont);
            final Typeface bold = Typeface.createFromAsset(assetManager, boldFont);
            final Typeface italic = Typeface.createFromAsset(assetManager, italicFont);
            final Typeface boldItalic = Typeface.createFromAsset(assetManager, boldItalicFont);

            Field DEFAULT = Typeface.class.getDeclaredField("DEFAULT");
            DEFAULT.setAccessible(true);
            DEFAULT.set(null, regular);

            Field DEFAULT_BOLD = Typeface.class.getDeclaredField("DEFAULT_BOLD");
            DEFAULT_BOLD.setAccessible(true);
            DEFAULT_BOLD.set(null, bold);

            Field sDefaults = Typeface.class.getDeclaredField("sDefaults");
            sDefaults.setAccessible(true);
            sDefaults.set(null, new Typeface[]{regular, bold, italic, boldItalic});
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

}
