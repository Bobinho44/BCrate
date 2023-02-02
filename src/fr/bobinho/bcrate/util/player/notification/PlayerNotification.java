package fr.bobinho.bcrate.util.player.notification;

import fr.bobinho.bcrate.BCrateCore;
import fr.bobinho.bcrate.api.color.BColor;
import fr.bobinho.bcrate.api.notification.BNotification;
import fr.bobinho.bcrate.api.notification.BPlaceHolder;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * Enum of player notifications
 */
public enum PlayerNotification implements BNotification {
    PLAYER_NOT_REGISTERED,
    PLAYER_YOU_INVENTORY_FULL,
    PLAYER_INVENTORY_FULL,
    PLAYER_HAVENT_KEY,
    PLAYER_GIVE_KEY,
    PLAYER_REMOVE_KEY,
    PLAYER_RECEIVE_KEY,
    PLAYER_LOOSE_KEY,
    PLAYER_EMPTY_HAND,
    PLAYER_ALREADY_USED_CRATE;

    /**
     * {@inheritDoc}
     */
    @Override
    public String getNotification() {
        return BColor.color(BCrateCore.getLangSetting().getString(name()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @Nonnull String getNotification(@Nonnull BPlaceHolder... placeholders) {
        String notification = BCrateCore.getLangSetting().getString(name());

        for (BPlaceHolder placeHolder : placeholders) {
            notification = notification.replaceAll(placeHolder.getOldValue(), placeHolder.getReplacement());
        }

        return BColor.color(notification);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @Nonnull List<String> getNotifications(@Nonnull BPlaceHolder... placeholders) {
        List<String> notifications = BCrateCore.getLangSetting().getStringList(name()).stream().toList();

        return notifications.stream().map(notification -> {
            for (BPlaceHolder placeHolder : placeholders) {
                notification = notification.replaceAll(placeHolder.getOldValue(), placeHolder.getReplacement());
            }

            return BColor.color(notification);
        }).toList();
    }

}