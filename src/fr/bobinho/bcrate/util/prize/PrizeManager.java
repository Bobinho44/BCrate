package fr.bobinho.bcrate.util.prize;

import fr.bobinho.bcrate.api.validate.BValidate;
import fr.bobinho.bcrate.util.crate.Crate;
import fr.bobinho.bcrate.util.prize.listener.PrizeListener;
import fr.bobinho.bcrate.util.tag.Tag;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import java.util.Optional;

/**
 * Manager class for prizes
 */
public class PrizeManager {

    /**
     * Registers the prize manager
     */
    public static void register() {
        PrizeListener.registerEvents();
    }

    /**
     * Unregisters the prize manager
     */
    public static void unregister() {
    }

    /**
     * Gets an optional prize
     *
     * @param crate the crate
     * @param slot  the slot
     * @return an optional tag
     */
    public static @Nonnull Optional<Prize> get(@Nonnull Crate crate, int slot) {
        BValidate.notNull(crate);

        return crate.prizes().stream().filter(prize -> prize.slot().get() == slot).findFirst();
    }

    /**
     * Switchs tag selection
     *
     * @param prize the prize
     * @param tag   the tag
     */
    public static void switchTagSelection(@Nonnull Prize prize, @Nonnull Tag tag) {
        BValidate.notNull(prize);
        BValidate.notNull(tag);

        if (prize.tags().contains(tag)) {
            prize.tags().remove(tag);
        } else {
            prize.tags().add(tag);
        }
    }

    /**
     * Changes the chance
     *
     * @param prize  the prize
     * @param chance the chance
     */
    public static void changeChance(@Nonnull Prize prize, double chance) {
        BValidate.notNull(prize);

        prize.chance().set(chance);
    }

    /**
     * Changes the rarity
     *
     * @param prize  the prize
     */
    public static void changeRarity(@Nonnull Prize prize) {
        BValidate.notNull(prize);

        prize.rarity().set(!prize.rarity().get());
    }

    /**
     * Changes the skin
     *
     * @param prize  the prize
     * @param skin the skin
     */
    public static void changeSkin(@Nonnull Prize prize, @Nonnull ItemStack skin) {
        BValidate.notNull(prize);

        prize.skin().set(skin);
    }

    /**
     * Opens the prize edit menu
     *
     * @param player the player
     */
    public static void openEditMenu(@Nonnull Player player, @Nonnull Prize prize) {
        prize.editMenu().get().openInventory(player);
    }

    /**
     * Opens the prize skin menu
     *
     * @param player the player
     */
    public static void openSkinMenu(@Nonnull Player player, @Nonnull Prize prize) {
        prize.skinMenu().get().openInventory(player);
    }
}
