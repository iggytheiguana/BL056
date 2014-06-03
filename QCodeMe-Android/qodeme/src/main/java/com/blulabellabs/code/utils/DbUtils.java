package com.blulabellabs.code.utils;

/**
 * Created by Alex on 9/11/13.
 */
public class DbUtils {

    public static String getWhereClauseForIds(long[] ids, String idStr) {
        StringBuilder whereClause = new StringBuilder();
        whereClause.append("(");
        for (int i = 0; i < ids.length; i++) {
            if (i > 0) {
                whereClause.append("OR ");
            }
            whereClause.append(idStr);
            whereClause.append(" = ? ");
        }
        whereClause.append(")");
        return whereClause.toString();
    }

    public static String getWhereClauseForIds(long[] ids) {
        final String _ID = "_id";
        return getWhereClauseForIds(ids,_ID);
    }

    public static String getWhereClauseForId() {
        return getWhereClauseForIds(new long[]{1});
    }

    public static String[] getWhereArgsForIds(long[] ids) {
        String[] whereArgs = new String[ids.length];
        for (int i = 0; i < ids.length; i++) {
            whereArgs[i] = Long.toString(ids[i]);
        }
        return whereArgs;
    }

    public static String[] getWhereArgsForId(long id) {
        return getWhereArgsForIds(new long[]{id});
    }

}
