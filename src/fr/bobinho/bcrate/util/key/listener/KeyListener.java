package fr.bobinho.bcrate.util.key.listener;

import fr.bobinho.bcrate.api.event.BEvent;
import fr.bobinho.bcrate.api.notification.BPlaceHolder;
import fr.bobinho.bcrate.api.validate.BValidate;
import fr.bobinho.bcrate.util.crate.notification.CrateNotification;
import fr.bobinho.bcrate.util.key.Key;
import fr.bobinho.bcrate.util.key.KeyManager;
import fr.bobinho.bcrate.util.key.notification.KeyNotification;
import fr.bobinho.bcrate.util.key.ux.KeyEditMenu;
import fr.bobinho.bcrate.util.key.ux.KeyShowMenu;
import fr.bobinho.bcrate.util.player.PlayerManager;
import fr.bobinho.bcrate.util.player.notification.PlayerNotification;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;

/**
 * Listener class for keys
 */
public class KeyListener {

    /**
     * Registers key listeners
     */
    public static void registerEvents() {
        onInteractWithKeyMenu();
        onEditKeys();
        onWithdraw();
        onDeposit();
    }

    /**
     * Listens interactions with crate menus
     */
    private static void onInteractWithKeyMenu() {
        BEvent.registerEvent(InventoryDragEvent.class)
                .filter(event -> event.getInventory().getHolder() instanceof KeyShowMenu)
                .consume(event -> event.setCancelled(true));

        BEvent.registerEvent(InventoryClickEvent.class)
                .filter(event -> event.getInventory().getHolder() instanceof KeyShowMenu)
                .consume(event -> event.setCancelled(true));

        BEvent.registerEvent(InventoryClickEvent.class)
                .filter(event -> event.getInventory().getHolder() instanceof KeyEditMenu)
                .filter(event -> event.getClickedInventory() != null)
                .filter(event -> (event.getClick() != ClickType.RIGHT && event.getClick() != ClickType.LEFT) || event.getClickedInventory().getType() == InventoryType.PLAYER)
                .consume(event -> event.setCancelled(true));
    }

    /**
     * Listens keys edit
     */
    private static void onEditKeys() {
        BEvent.registerEvent(InventoryClickEvent.class)
                .filter(event -> event.getInventory().getHolder() instanceof KeyEditMenu)
                .consume(event -> {
                    ItemStack curr = event.getCurrentItem();
                    ItemStack curs = event.getCursor();

                    if ((event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) && event.getCursor() != null && event.getCursor().getType() != Material.AIR) {
                        KeyManager.get(event.getCursor()).ifPresent(key -> key.slot().set(event.getSlot()));
                    }
                });
    }

    /**
     * Listens withdraw
     */
    private static void onWithdraw() {
        BEvent.registerEvent(InventoryClickEvent.class)
                .filter(event -> event.getClickedInventory() != null)
                .filter(event -> event.getClickedInventory().getHolder() instanceof KeyShowMenu)
                .consume(event -> event.setCancelled(true));

        BEvent.registerEvent(InventoryClickEvent.class)
                .filter(event -> event.getInventory().getHolder() instanceof KeyShowMenu)
                .filter(event -> event.getCurrentItem() != null)
                .filter(event -> event.getCurrentItem().getItemMeta() != null)
                .consume(event -> {
                    event.setCancelled(true);

                    if (event.getClick() != ClickType.LEFT) {
                        return;
                    }

                    KeyManager.get(event.getCurrentItem()).ifPresent(key -> {
                        askKeyNumberToWithdrawn((Player) event.getWhoClicked(), ((KeyShowMenu) event.getInventory().getHolder()).owner().get(), key);
                    });
                });
    }

    /**
     * Listens deposit
     */
    private static void onDeposit() {
        BEvent.registerEvent(InventoryClickEvent.class)
                .filter(event -> event.getInventory().getHolder() instanceof KeyShowMenu)
                .filter(event -> event.getCurrentItem() != null)
                .filter(event -> event.getCurrentItem().getItemMeta() != null)
                .consume(event -> {
                    event.setCancelled(true);

                    if (event.getClick() != ClickType.RIGHT) {
                        return;
                    }

                    KeyManager.get(event.getCurrentItem()).ifPresent(key -> {
                        askKeyNumberToDeposited((Player) event.getWhoClicked(), ((KeyShowMenu) event.getInventory().getHolder()).owner().get(), key);
                    });
                });
    }

    /**
     * Asks the number of key to withdraw
     *
     * @param player the player
     * @param key    the key
     */
    private static void askKeyNumberToWithdrawn(@Nonnull Player player, @Nonnull Player owner, @Nonnull Key key) {
        BValidate.notNull(player);
        BValidate.notNull(owner);
        BValidate.notNull(key);

        player.closeInventory();
        player.sendMessage(KeyNotification.KEY_ASK_WITHDRAW.getNotification(new BPlaceHolder("%name%", key.name().get())));

        BEvent.registerEvent(AsyncPlayerChatEvent.class)
                .limit(1)
                .filter(event -> event.getPlayer().equals(player))
                .consume(event -> {
                    event.setCancelled(true);

                    //Checks if the player is not registered
                    if (!PlayerManager.isRegistered(player.getUniqueId())) {
                        player.sendMessage(PlayerNotification.PLAYER_NOT_REGISTERED.getNotification(new BPlaceHolder("%name%", owner.getName())));
                        return;
                    }

                    //Checks if the value is valid
                    if (!event.getMessage().matches("^[1-9]\\d*$")) {
                        player.sendMessage(CrateNotification.UTIL_NOT_A_NUMBER.getNotification(new BPlaceHolder("%number%", event.getMessage())));
                        return;
                    }

                    //Checks if the player has enough keys
                    if (Integer.parseInt(event.getMessage()) > PlayerManager.getKeyNumberWithdrawable(owner.getUniqueId(), key)) {
                        if (player.equals(owner)) {
                            player.sendMessage(KeyNotification.KEY_YOU_NOT_ENOUGH_TO_WITHDRAW.getNotification(
                                    new BPlaceHolder("%amount%", String.valueOf(Integer.parseInt(event.getMessage()))),
                                    new BPlaceHolder("%name%", key.name().get())));
                        } else {
                            player.sendMessage(KeyNotification.KEY_PLAYER_NOT_ENOUGH_TO_WITHDRAW.getNotification(
                                    new BPlaceHolder("%amount%", String.valueOf(Integer.parseInt(event.getMessage()))),
                                    new BPlaceHolder("%player%", owner.getName()),
                                    new BPlaceHolder("%name%", key.name().get())));
                        }
                        return;
                    }

                    //Checks if the player has enough keys
                    if (!PlayerManager.canWithdrawKey(owner.getUniqueId(), key, Integer.parseInt(event.getMessage()))) {
                        if (player.equals(owner)) {
                            player.sendMessage(PlayerNotification.PLAYER_YOU_INVENTORY_FULL.getNotification());
                        } else {
                            player.sendMessage(PlayerNotification.PLAYER_INVENTORY_FULL.getNotification(new BPlaceHolder("%player", owner.getName())));
                        }
                        return;
                    }

                    if (player.equals(owner)) {
                        PlayerManager.withdrawKey(owner.getUniqueId(), key, Integer.parseInt(event.getMessage()));

                        player.sendMessage(KeyNotification.KEY_WITHDRAW.getNotification(
                                new BPlaceHolder("%amount%", String.valueOf(Integer.parseInt(event.getMessage()))),
                                new BPlaceHolder("%name%", key.name().get())));
                    } else {
                        PlayerManager.removeKey(owner.getUniqueId(), key, Integer.parseInt(event.getMessage()));
                        owner.sendMessage(PlayerNotification.PLAYER_LOOSE_KEY.getNotification(new BPlaceHolder("%name%", key.name().get()), new BPlaceHolder("%amount%", (event.getMessage()))));
                        player.sendMessage(PlayerNotification.PLAYER_REMOVE_KEY.getNotification(new BPlaceHolder("%name%", key.name().get()), new BPlaceHolder("%amount%", event.getMessage()), new BPlaceHolder("%player%", owner.getName())));
                    }
                });
    }

    /**
     * Asks the number of key to deposit
     *
     * @param player the player
     * @param key    the key
     */
    private static void askKeyNumberToDeposited(@Nonnull Player player, @Nonnull Player owner, @Nonnull Key key) {
        BValidate.notNull(player);
        BValidate.notNull(owner);
        BValidate.notNull(key);

        player.closeInventory();
        player.sendMessage(KeyNotification.KEY_ASK_DEPOSIT.getNotification(new BPlaceHolder("%name%", key.name().get())));

        BEvent.registerEvent(AsyncPlayerChatEvent.class)
                .limit(1)
                .filter(event -> event.getPlayer().equals(player))
                .consume(event -> {
                    event.setCancelled(true);

                    //Checks if the player is not registered
                    if (!PlayerManager.isRegistered(owner.getUniqueId())) {
                        player.sendMessage(PlayerNotification.PLAYER_NOT_REGISTERED.getNotification(new BPlaceHolder("%name%", owner.getName())));
                        return;
                    }

                    //Checks if the value is valid
                    if (!event.getMessage().matches("^[1-9]\\d*$")) {
                        player.sendMessage(CrateNotification.UTIL_NOT_A_NUMBER.getNotification(new BPlaceHolder("%number%", event.getMessage())));
                        return;
                    }

                    //Checks if the player has enough keys
                    if (Integer.parseInt(event.getMessage()) > PlayerManager.getKeyNumberDepositable(owner.getUniqueId(), key) && player.equals(owner)) {
                        if (player.equals(owner)) {
                            player.sendMessage(KeyNotification.KEY_YOU_NOT_ENOUGH_TO_DEPOSIT.getNotification(
                                    new BPlaceHolder("%amount%", String.valueOf(Integer.parseInt(event.getMessage()))),
                                    new BPlaceHolder("%name%", key.name().get())));
                        }
                        return;
                    }

                    if (player.equals(owner)) {
                        PlayerManager.depositKey(owner.getUniqueId(), key, Integer.parseInt(event.getMessage()));

                        player.sendMessage(KeyNotification.KEY_DEPOSIT.getNotification(
                                new BPlaceHolder("%amount%", String.valueOf(Integer.parseInt(event.getMessage()))),
                                new BPlaceHolder("%name%", key.name().get())));
                    } else {
                        PlayerManager.addKey(owner.getUniqueId(), key, Integer.parseInt(event.getMessage()));
                        owner.sendMessage(PlayerNotification.PLAYER_RECEIVE_KEY.getNotification(new BPlaceHolder("%name%", key.name().get()), new BPlaceHolder("%amount%", (event.getMessage()))));
                        player.sendMessage(PlayerNotification.PLAYER_GIVE_KEY.getNotification(new BPlaceHolder("%name%", key.name().get()), new BPlaceHolder("%amount%", event.getMessage()), new BPlaceHolder("%player%", owner.getName())));
                    }
                });
    }

}
