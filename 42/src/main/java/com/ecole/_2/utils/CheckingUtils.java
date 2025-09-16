package com.ecole._2.utils;

import com.ecole._2.models.User;

public class CheckingUtils {
    public static String determineUserKind(User user) {
        if (user.getKind() != null) return user.getKind();
        if (isAdminUser(user)) return "admin";
        return "student";
    }

    private static boolean isAdminUser(User user) {
        String[] adminLogins = {"admin", "root", "supervisor"};
        if (user.getLogin() != null) {
            for (String adminLogin : adminLogins) {
                if (user.getLogin().toLowerCase().contains(adminLogin)) return true;
            }
        }
        return false;
    }
}
