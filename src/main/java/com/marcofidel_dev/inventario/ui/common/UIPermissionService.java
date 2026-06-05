package com.marcofidel_dev.inventario.ui.common;

import com.marcofidel_dev.inventario.domain.entity.Role;
import com.marcofidel_dev.inventario.infrastructure.security.SessionContext;
import javafx.scene.Node;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * JavaFX utility that applies role-based visibility/disable rules to UI nodes.
 * Call in controller initialize() AFTER the session has been set.
 */
@Component
@RequiredArgsConstructor
public class UIPermissionService {

    private final SessionContext sessionContext;

    /** Hides the node (and removes it from layout) if the current user is not ADMIN. */
    public void hideIfNotAdmin(Node node) {
        boolean isAdmin = sessionContext.hasRole(Role.ADMIN);
        node.setVisible(isAdmin);
        node.setManaged(isAdmin);
    }

    /** Disables interaction on the node if the current user is not ADMIN. */
    public void disableIfNotAdmin(Node node) {
        node.setDisable(!sessionContext.hasRole(Role.ADMIN));
    }

    /**
     * Binds visibility to a required role.
     * The node is hidden and removed from layout if the role requirement is not met.
     */
    public void bindVisibility(Node node, Role requiredRole) {
        boolean visible = sessionContext.hasRole(requiredRole);
        node.setVisible(visible);
        node.setManaged(visible);
    }

    /** Returns true if the current session has the ADMIN role. */
    public boolean isAdmin() {
        return sessionContext.hasRole(Role.ADMIN);
    }
}
