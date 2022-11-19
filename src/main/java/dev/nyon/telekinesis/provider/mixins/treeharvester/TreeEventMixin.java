package dev.nyon.telekinesis.provider.mixins.treeharvester;

import com.natamus.treeharvester.events.TreeEvent;
import dev.nyon.telekinesis.TelekinesisKt;
import dev.nyon.telekinesis.config.ConfigKt;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TreeEvent.class)
public class TreeEventMixin {

    @Inject(
        method = "onTreeHarvest",
        at = @At(
            value = "INVOKE",
            target = "Lcom/natamus/collective_fabric/functions/BlockFunctions;dropBlock(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;)V"
        )
    )
    private static void redirectDrops(
        Level world,
        Player player,
        BlockPos bpos,
        BlockState state,
        BlockEntity blockEntity,
        CallbackInfoReturnable<Boolean> cir
    ) {
        if (
            (
                EnchantmentHelper.getItemEnchantmentLevel(TelekinesisKt.getTelekinesis(), player.getUseItem()) == 0
                    && !ConfigKt.getConfig().getOnByDefault()
            )
                || !ConfigKt.getConfig().getBlockDrops()
        ) return;

        if (!player.addItem(new ItemStack(state.getBlock().asItem()))) return;
        cir.cancel();
    }
}
