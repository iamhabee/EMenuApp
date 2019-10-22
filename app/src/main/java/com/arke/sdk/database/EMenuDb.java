package com.arke.sdk.database;

import com.raizlabs.android.dbflow.annotation.Database;

/**
 * @author Wan Clem
 */
@Database(name = EMenuDb.NAME, version = EMenuDb.VERSION, backupEnabled = true)
public class EMenuDb {
    static final String NAME = "EMenuDb";
    public static final int VERSION = 1;
}
