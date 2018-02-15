package de.btobastian.javacord.utils.handler.server.role;

import com.fasterxml.jackson.databind.JsonNode;
import de.btobastian.javacord.DiscordApi;
import de.btobastian.javacord.entities.permissions.Permissions;
import de.btobastian.javacord.entities.permissions.impl.ImplPermissions;
import de.btobastian.javacord.entities.permissions.impl.ImplRole;
import de.btobastian.javacord.events.server.role.RoleChangeColorEvent;
import de.btobastian.javacord.events.server.role.RoleChangeHoistEvent;
import de.btobastian.javacord.events.server.role.RoleChangePermissionsEvent;
import de.btobastian.javacord.events.server.role.RoleChangePositionEvent;
import de.btobastian.javacord.listeners.server.role.RoleChangeColorListener;
import de.btobastian.javacord.listeners.server.role.RoleChangeHoistListener;
import de.btobastian.javacord.listeners.server.role.RoleChangePermissionsListener;
import de.btobastian.javacord.listeners.server.role.RoleChangePositionListener;
import de.btobastian.javacord.utils.PacketHandler;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles the guild role create packet.
 */
public class GuildRoleUpdateHandler extends PacketHandler {

    /**
     * Creates a new instance of this class.
     *
     * @param api The api.
     */
    public GuildRoleUpdateHandler(DiscordApi api) {
        super(api, true, "GUILD_ROLE_UPDATE");
    }

    @Override
    public void handle(JsonNode packet) {
        JsonNode roleJson = packet.get("role");
        long roleId = roleJson.get("id").asLong();
        api.getRoleById(roleId).map(role -> (ImplRole) role).ifPresent(role -> {
            Color oldColorObject = role.getColor().orElse(null);
            int oldColor = role.getColorAsInt();
            int newColor = roleJson.get("color").asInt(0);
            if (oldColor != newColor) {
                role.setColor(newColor);

                RoleChangeColorEvent event = new RoleChangeColorEvent(
                        api, role, role.getColor().orElse(null), oldColorObject);

                List<RoleChangeColorListener> listeners = new ArrayList<>();
                listeners.addAll(role.getRoleChangeColorListeners());
                listeners.addAll(role.getServer().getRoleChangeColorListeners());
                listeners.addAll(api.getRoleChangeColorListeners());

                dispatchEvent(listeners, listener -> listener.onRoleChangeColor(event));
            }

            boolean oldHoist = role.isDisplayedSeparately();
            boolean newHoist = roleJson.get("hoist").asBoolean(false);
            if (oldHoist != newHoist) {
                role.setHoist(newHoist);

                RoleChangeHoistEvent event = new RoleChangeHoistEvent(api, role, oldHoist);

                List<RoleChangeHoistListener> listeners = new ArrayList<>();
                listeners.addAll(role.getRoleChangeHoistListeners());
                listeners.addAll(role.getServer().getRoleChangeHoistListeners());
                listeners.addAll(api.getRoleChangeHoistListeners());

                dispatchEvent(listeners, listener -> listener.onRoleChangeHoist(event));
            }

            Permissions oldPermissions = role.getPermissions();
            ImplPermissions newPermissions = new ImplPermissions(roleJson.get("permissions").asInt(), 0);
            if (!oldPermissions.equals(newPermissions)) {
                role.setPermissions(newPermissions);

                RoleChangePermissionsEvent event =
                        new RoleChangePermissionsEvent(api, role, newPermissions, oldPermissions);

                List<RoleChangePermissionsListener> listeners = new ArrayList<>();
                listeners.addAll(role.getRoleChangePermissionsListeners());
                listeners.addAll(role.getServer().getRoleChangePermissionsListeners());
                listeners.addAll(api.getRoleChangePermissionsListeners());

                dispatchEvent(listeners, listener -> listener.onRoleChangePermissions(event));
            }

            int oldPosition = role.getPosition();
            int newPosition = roleJson.get("position").asInt();
            if (oldPosition != newPosition) {
                role.setPosition(newPosition);

                RoleChangePositionEvent event = new RoleChangePositionEvent(api, role, newPosition, oldPosition);

                List<RoleChangePositionListener> listeners = new ArrayList<>();
                listeners.addAll(role.getRoleChangePositionListeners());
                listeners.addAll(role.getServer().getRoleChangePositionListeners());
                listeners.addAll(api.getRoleChangePositionListeners());

                dispatchEvent(listeners, listener -> listener.onRoleChangePosition(event));
            }
        });
    }

}