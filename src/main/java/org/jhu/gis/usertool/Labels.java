package org.jhu.gis.usertool;

import java.util.ResourceBundle;

public class Labels {

    final static String NEW_USER_BUTTON = "newusers.button";
    final static String EXPIRING_USER_BUTTON = "expiringusers.button";
    final static String USER_TOOL_LABEL = "usertool.label";

    private ResourceBundle bundle;

    public Labels(ResourceBundle bundle) {
        this.bundle = bundle;
    }

    public String get(String property) {
        if (!bundle.containsKey(property)) {
            return null;
        }
        return bundle.getString(property);
    }
}
