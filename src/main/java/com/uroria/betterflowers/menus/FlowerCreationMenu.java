package com.uroria.betterflowers.menus;

import com.uroria.betterflowers.BetterFlowers;
import com.uroria.betterflowers.flowers.SingleFlower;
import com.uroria.betterflowers.flowers.placable.FlowerGroup;
import com.uroria.betterflowers.utils.BukkitPlayerInventory;
import com.uroria.betterflowers.utils.FlowerCollection;
import com.uroria.betterflowers.data.FlowerData;
import com.uroria.betterflowers.utils.ItemBuilder;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class FlowerCreationMenu extends BukkitPlayerInventory {

    private final List<FlowerData> personalFlower;
    private final List<Boolean> isGroup;
    private final List<Boolean> randomizer;
    private final Player player;
    private final ItemStack active;
    private final ItemStack notActive;
    private final ItemStack wholeCategoryRan;
    private final ItemStack wholeCategory;
    private final BetterFlowers betterFlowers;

    public FlowerCreationMenu(Player player, BetterFlowers betterFlowers) {
        super(MiniMessage.miniMessage().deserialize("<gradient:#232526:#414345>Flower Creator"), 6);

        this.player = player;
        this.betterFlowers = betterFlowers;

        this.personalFlower = new ArrayList<>();
        this.randomizer = new ArrayList<>();
        this.isGroup = new ArrayList<>();

        this.active = new ItemBuilder(Material.LIME_STAINED_GLASS_PANE).setName("<green>[randomizer]</green> <red>[group]</red>").build();
        this.notActive = new ItemBuilder(Material.RED_STAINED_GLASS_PANE).setName("<red>[randomizer] [group]</red>>").build();
        this.wholeCategoryRan = new ItemBuilder(Material.BLUE_STAINED_GLASS_PANE).setName("<green>[randomizer] [group]</green>").build();
        this.wholeCategory = new ItemBuilder(Material.MAGENTA_STAINED_GLASS_PANE).setName("<red>[randomizer]</red> <green>[group]</green>").build();
    }

    public void open() {

        this.closeActions.add(() -> {
            personalFlower.clear();
            randomizer.clear();
            isGroup.clear();
        });

        generateCategories();
        openInventory(player);
    }

    private void generateFlowerOverlay() {

        //generates placeholder
        for (var index = 27; index < 54; index++) {
            if (index >= 36 && index <= 44) continue;
            this.setSlot(index, new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE).setName(" ").build(), this::cancelClick);
        }

        //generates the display for the randomizer
        for (var index = 0; index < 9; index++) {
            if (index >= randomizer.size() || index >= isGroup.size()) break;
            if (isGroup.get(index) && randomizer.get(index)) setSlot(45 + index, wholeCategoryRan, this::cancelClick);
            if (isGroup.get(index) && !randomizer.get(index)) setSlot(45 + index, wholeCategory, this::cancelClick);
            if (!isGroup.get(index) && randomizer.get(index)) setSlot((45 + index), active, this::cancelClick);
            if (!isGroup.get(index) && !randomizer.get(index)) setSlot((45 + index), notActive, this::cancelClick);
        }

        //generates the chosen list of flowers to display the current flower list
        for (var index = 0; index < personalFlower.size(); index++) {
            final var singleFlower = personalFlower.get(index);
            final var name = "<green>" + singleFlower.getName() + " ID" + index + "</green>";
            setSlot((36 + index), new ItemBuilder(singleFlower.getDisplay()).setName(name).build(), this::cancelClick);
        }

        setSlot(29, new ItemBuilder(Material.ECHO_SHARD).setName("<gradient:#a8ff78:#78ffd6>Create the flower").build(), this::onCreateClick);
        setSlot(30, new ItemBuilder(Material.STRUCTURE_VOID).setName("<gradient:#a8ff78:#78ffd6>Back to Menu").build(), this::onBackClick);
        setSlot(32, new ItemBuilder(Material.BARRIER).setName("<gradient:#EB3349:#F45C43>Delete all flowers").build(), this::onDeleteClick);
        setSlot(33, new ItemBuilder(Material.REDSTONE).setName("<gradient:#EB3349:#F45C43>Remove top plant").build(), this::onRemoveClick);
    }

    private void generateCategories() {

        clearSlots();
        generateFlowerOverlay();

        List<FlowerCollection> flowers = List.copyOf(Arrays.stream(FlowerCollection.values()).toList());

        for (int index = 0; index < 28; index++) {

            if (index >= flowers.size()) break;

            final var currentFlowers = flowers.get(index).getFlowerGroup();

            setSlot(index, new ItemBuilder(currentFlowers.getDisplay()).setName(currentFlowers.getDisplayName()).setLore(List.of(
                    " ",
                    "<gray> - shift click to add whole group</gray>",
                    "<gray> - shift + left click to add without</gray> <red>randomizer</red>",
                    "<gray> - shift + right click to add</gray> <green>with randomizer</green>")).build(),
                    inventoryClickEvent -> onCategoryClick(inventoryClickEvent, currentFlowers)
            );
        }
    }

    private void generateSubCategories(FlowerGroup flowerGroup) {

        clearSlots();
        generateFlowerOverlay();

        for (int index = 0; index < 53; index++) {

            if (index >= flowerGroup.getFlowers().size()) break;

            final var singleFlower = flowerGroup.getFlowers().get(index);

            setSlot(index, new ItemBuilder(singleFlower.getDisplay()).setName((singleFlower.getDisplayName())).setLore(List.of(
                    " ",
                    "<gray> - left click to add without</gray> <red>randomizer</red>",
                    "<gray> - right click to add</gray> <green>with randomizer</green>")).build(),
                    inventoryClickEvent -> onSubCategoryClick(inventoryClickEvent, singleFlower)
            );
        }
    }

    private void cancelClick(InventoryClickEvent inventoryClickEvent) {
        inventoryClickEvent.setCancelled(true);
    }

    private void onCreateClick(InventoryClickEvent inventoryClickEvent) {
        inventoryClickEvent.setCancelled(true);

        if (personalFlower.isEmpty()) {
            player.getInventory().close();
            return;
        }

        final List<String> description = new ArrayList<>();
        description.add(" ");
        description.add("<gray>This Flower contains:</gray>");

        personalFlower.forEach(singleFlower -> description.add(" <gray>- " + singleFlower.getDisplay() + "</gray>"));

        //just takes the current system-time as a display name
        final var name = "<green>ID: " + System.currentTimeMillis() + "</green>";
        final var placer = new ItemBuilder(Material.FLOWER_POT).setName(name).setLore(description).build();

        player.getInventory().addItem(placer);
        player.sendMessage(MiniMessage.miniMessage().deserialize("<gradient:#a8ff78:#78ffd6>Flower has been created"));

        betterFlowers.getFlowerManager().getFlowers().put(placer, List.copyOf(personalFlower));
        betterFlowers.getFlowerManager().getFlowerRandomizer().put(placer, List.copyOf(randomizer));

        player.getInventory().close();
    }

    private void onBackClick(InventoryClickEvent inventoryClickEvent) {
        inventoryClickEvent.setCancelled(true);
        generateCategories();
    }

    private void onRemoveClick(InventoryClickEvent inventoryClickEvent) {
        inventoryClickEvent.setCancelled(true);

        if (!personalFlower.isEmpty() && !randomizer.isEmpty()) {
            personalFlower.remove(personalFlower.size() - 1);
            randomizer.remove(randomizer.size() - 1);
        }

        player.playSound(player.getLocation(), Sound.BLOCK_COMPOSTER_EMPTY, 1, 0);
        generateCategories();
    }

    private void onDeleteClick(InventoryClickEvent inventoryClickEvent) {
        inventoryClickEvent.setCancelled(true);

        personalFlower.clear();
        randomizer.clear();
        generateCategories();
    }

    private void onCategoryClick(InventoryClickEvent inventoryClickEvent, FlowerGroup flowerGroup) {
        final var currentData = new FlowerData(flowerGroup.getFlowers(), flowerGroup.getDisplayName(), flowerGroup.getDisplay());

        inventoryClickEvent.setCancelled(true);

        if (!inventoryClickEvent.isShiftClick()) {
            generateSubCategories(flowerGroup);
            return;
        }

        if (personalFlower.size() > 8) {
            player.sendMessage(MiniMessage.miniMessage().deserialize("<gradient:#EB3349:#F45C43>You already reached the maximum amount of flowers!</gradient>"));
            return;
        }

        if (inventoryClickEvent.isRightClick()) randomizer.add(true);
        else randomizer.add(false);
        isGroup.add(true);

        personalFlower.add(currentData);
        player.playSound(player.getLocation(), Sound.BLOCK_CAVE_VINES_PLACE, 1, 0);
        generateCategories();
    }

    private void onSubCategoryClick(InventoryClickEvent inventoryClickEvent, SingleFlower singleFlower) {
        inventoryClickEvent.setCancelled(true);

        if (personalFlower.size() > 8) {
            player.sendMessage(MiniMessage.miniMessage().deserialize("<gradient:#EB3349:#F45C43>You already reached the maximum amount of flowers!</gradient>"));
            return;
        }

        if (inventoryClickEvent.isRightClick()) randomizer.add(true);
        else randomizer.add(false);
        isGroup.add(false);

        personalFlower.add(new FlowerData(singleFlower));
        player.playSound(player.getLocation(), Sound.BLOCK_CAVE_VINES_PLACE, 1, 0);
        generateCategories();
    }
}