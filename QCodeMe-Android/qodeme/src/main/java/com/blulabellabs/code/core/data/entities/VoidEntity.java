package com.blulabellabs.code.core.data.entities;

import android.content.ContentValues;

/**
 * Created by Alex on 8/16/13.
 * Void entity for Void response
 */
public final class VoidEntity implements BaseEntity {

    @Override
    public ContentValues toContentValues() {
        return null;
    }
}
