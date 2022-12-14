package fr.bobinho.bcrate.util.prize;

import fr.bobinho.bcrate.api.item.BItemBuilder;
import fr.bobinho.bcrate.api.notification.BPlaceHolder;
import fr.bobinho.bcrate.api.validate.BValidate;
import fr.bobinho.bcrate.util.crate.Crate;
import fr.bobinho.bcrate.util.prize.notification.PrizeNotification;
import fr.bobinho.bcrate.util.prize.ux.PrizeEditMenu;
import fr.bobinho.bcrate.util.prize.ux.PrizeSkinMenu;
import fr.bobinho.bcrate.util.tag.Tag;
import fr.bobinho.bcrate.wrapper.MonoValuedAttribute;
import fr.bobinho.bcrate.wrapper.MultiValuedAttribute;
import fr.bobinho.bcrate.wrapper.ReadOnlyMonoValuedAttribute;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

/**
 * Class representing the prize
 */
public class Prize {

    /**
     * Fields
     */
    private final MonoValuedAttribute<ItemStack> item;
    private final MonoValuedAttribute<ItemStack> skin;
    private final MonoValuedAttribute<Integer> slot;
    private final MonoValuedAttribute<Double> chance;
    private final MonoValuedAttribute<Boolean> rarity;
    private final MultiValuedAttribute<Tag> tags;
    private final ReadOnlyMonoValuedAttribute<PrizeEditMenu> editMenu;
    private final ReadOnlyMonoValuedAttribute<PrizeSkinMenu> skinMenu;


    /**
     * Creates a new prize
     *
     * @param item   the item
     * @param slot   the slot
     * @param chance the chance
     * @param tag    the tag
     */
    public Prize(@Nonnull ItemStack item, @Nonnull ItemStack skin, int slot, double chance, boolean rarity, @Nonnull List<Tag> tag) {
        BValidate.notNull(item);
        BValidate.notNull(skin);
        BValidate.notNull(tag);

        this.item = new MonoValuedAttribute<>(item);
        this.skin = new MonoValuedAttribute<>(skin);
        this.slot = new MonoValuedAttribute<>(slot);
        this.chance = new MonoValuedAttribute<>(chance);
        this.rarity = new MonoValuedAttribute<>(rarity);
        this.tags = new MultiValuedAttribute<>(tag);
        this.editMenu = new ReadOnlyMonoValuedAttribute<>(new PrizeEditMenu(this));
        this.skinMenu = new ReadOnlyMonoValuedAttribute<>(new PrizeSkinMenu(this));
    }

    /**
     * Creates a new prize without tag
     *
     * @param item   the item
     * @param slot   the slot
     * @param chance the chance
     */
    public Prize(@Nonnull ItemStack item, @Nonnull ItemStack skin, int slot, double chance) {
        this(item, skin, slot, chance, false, new ArrayList<>());
    }

    /**
     * Gets the item wrapper
     *
     * @return the item wrapper
     */
    public @Nonnull MonoValuedAttribute<ItemStack> item() {
        return item;
    }

    /**
     * Gets the skin wrapper
     *
     * @return the skin wrapper
     */
    public @Nonnull MonoValuedAttribute<ItemStack> skin() {
        return skin;
    }

    /**
     * Gets the slot wrapper
     *
     * @return the slot wrapper
     */
    public @Nonnull MonoValuedAttribute<Integer> slot() {
        return slot;
    }

    /**
     * Gets the chance wrapper
     *
     * @return the chance wrapper
     */
    public @Nonnull MonoValuedAttribute<Double> chance() {
        return chance;
    }

    /**
     * Gets the rare wrapper
     *
     * @return the rare wrapper
     */
    public @Nonnull MonoValuedAttribute<Boolean> rarity() {
        return rarity;
    }

    /**
     * Gets the tag wrapper
     *
     * @return the tag wrapper
     */
    public @Nonnull MultiValuedAttribute<Tag> tags() {
        return tags;
    }

    /**
     * Gets the edit menu wrapper
     *
     * @return the edit menu wrapper
     */
    public @Nonnull ReadOnlyMonoValuedAttribute<PrizeEditMenu> editMenu() {
        return editMenu;
    }

    /**
     * Gets the skin menu wrapper
     *
     * @return the skin menu wrapper
     */
    public @Nonnull ReadOnlyMonoValuedAttribute<PrizeSkinMenu> skinMenu() {
        return skinMenu;
    }

    /**
     * Gets the prize edit background
     *
     * @return the prize edit background
     */
    public @Nonnull ItemStack getEditBackground() {
        return new BItemBuilder(item.get())
                .lore(tags.get().stream().map(tag -> tag.description().get()).toList())
                .lore(List.of(
                        "",
                        PrizeNotification.PRIZE_CHANCE.getNotification(new BPlaceHolder("%chance%", String.valueOf(chance.get()))),
                        "",
                        rarity().get() ? PrizeNotification.PRIZE_RARE.getNotification() : PrizeNotification.PRIZE_NOT_RARE.getNotification()))
                .build();
    }

    /**
     * Gets the prize background
     *
     * @param crate the crate
     * @return the prize background
     */
    public @Nonnull ItemStack getBackground(@Nonnull Crate crate) {

        //Gets the item background (replaces barrier by color)
        return item.get().getType() == Material.BARRIER ? new BItemBuilder(Material.AIR).build()
                :
                new BItemBuilder(item.get())
                        .lore(tags.get().stream().map(tag -> tag.description().get()).toList())
                        .lore(List.of(
                                "",
                                PrizeNotification.PRIZE_CHANCE.getNotification(new BPlaceHolder("%chance%", String.valueOf(chance.get())))))
                        .build();
    }

}
