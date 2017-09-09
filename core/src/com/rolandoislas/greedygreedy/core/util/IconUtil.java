package com.rolandoislas.greedygreedy.core.util;

import com.rolandoislas.greedygreedy.core.data.Icon;

public class IconUtil {
    public static String getIconPath(Icon icon) {
        String iconPath = "image/icon/";
        switch (icon) {
            case DIE_FIVE:
                iconPath += "icon_die_five.png";
                break;
            case DIE_ONE:
                iconPath += "icon_die_one.png";
                break;
            case DIE_TWO:
                iconPath += "icon_die_two.png";
                break;
            case DIE_THREE:
                iconPath += "icon_die_three.png";
                break;
            case DIE_FOUR:
                iconPath += "icon_die_four.png";
                break;
            case DIE_SIX:
                iconPath += "icon_die_six.png";
                break;
            default:
                iconPath += "icon_die_five.png";
                break;
        }
        return iconPath;
    }

    public static String getIconPath(int iconOrdinal) {
        if (iconOrdinal >= Icon.values().length || iconOrdinal < 0)
            return getIconPath(Icon.DIE_FIVE);
        return getIconPath(Icon.values()[iconOrdinal]);
    }
}
