package com.bootstart.authentication.models;

public enum ERole {
    ROLE_USER("ROLE_USER", "The account is active if this role is granted"),
    ROLE_ADMIN("ROLE_ADMIN", "Grants the authority to display, create, edit (includes management of authorities) and delete users");

    private String roleId;
    private String roleDescription;

    ERole(String roleId, String roleDescription) {
        this.roleId = roleId;
        this.roleDescription = roleDescription;
    }

    public String getRoleId() {
        return roleId;
    }

    public void setRoleId(String roleId) {
        this.roleId = roleId;
    }

    public String getRoleDescription() {
        return roleDescription;
    }

    public void setRoleDescription(String roleDescription) {
        this.roleDescription = roleDescription;
    }
}
