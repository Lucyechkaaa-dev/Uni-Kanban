package backend.services;

import backend.models.team.TeamRole;

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

}