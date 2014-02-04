package biz.softtechnics.qodeme.tests.model.connection.rest.utils;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created with IntelliJ IDEA.
 * User: Alex Vegner
 * Date: 8/28/13
 * Time: 11:43 AM
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Order {
    public int order();
}

