package dev.instaflint;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.vehicle.TntMinecartEntity;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class InstaFlintClient implements ClientModInitializer {
    private static KeyBinding toggleKey;
    private static boolean enabled = true;

    @Override
    public void onInitializeClient() {
        KeyBinding.Category category = KeyBinding.Category.create(Identifier.of("instaflint", "main"));
        toggleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.instaflint.toggle", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_R, category));

        ClientTickEvents.END_CLIENT_TICK.register(this::onTick);
    }

    private void onTick(MinecraftClient client) {
        ClientPlayerEntity player = client.player;
        if (player == null || client.interactionManager == null) return;

        while (toggleKey.wasPressed()) {
            enabled = !enabled;
            player.sendMessage(Text.literal("InstaFlint: " + (enabled ? "ON" : "OFF")), true);
        }
        if (!enabled || client.currentScreen != null) return;

        HitResult hit = client.crosshairTarget;
        if (!(hit instanceof EntityHitResult ehr)) return;
        if (!(ehr.getEntity() instanceof TntMinecartEntity cart)) return;
        if (cart.isPrimed()) return;

        int flintSlot = -1;
        for (int i = 0; i < 9; i++) {
            if (player.getInventory().getStack(i).isOf(Items.FLINT_AND_STEEL)) {
                flintSlot = i;
                break;
            }
        }
        if (flintSlot == -1) return;

        int prev = player.getInventory().getSelectedSlot();
        player.getInventory().setSelectedSlot(flintSlot);
        client.interactionManager.interactEntity(player, cart, Hand.MAIN_HAND);
        player.swingHand(Hand.MAIN_HAND);
        player.getInventory().setSelectedSlot(prev);
    }
}
