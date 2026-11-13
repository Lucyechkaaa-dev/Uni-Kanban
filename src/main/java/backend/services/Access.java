package backend.services;

import backend.models.team.TeamRole;
import java.util.Locale;

public enum Access {
    NONE, VIEW, EDIT, MANAGE;     //do not change the order

    public boolean atLeast(Access needed) {
        return compareTo(needed) >= 0;
    }

    public static Access of(TeamRole role) {
        if (role == null) return NONE;
        return switch (role) {
            case OWNER, ADMIN -> MANAGE;
            case MEMBER -> EDIT;
            case VIEWER -> VIEW;
        };
    }

    public static Access ofBoardRole(String role) {
        if (role == null || role.isBlank()) return NONE;
        return switch (role.trim().toUpperCase(Locale.ROOT)) {
            case "VIEWER" -> VIEW;
            case "ADMIN", "OWNER", "MANAGER" -> MANAGE;
            default -> EDIT;
        };
    }
}