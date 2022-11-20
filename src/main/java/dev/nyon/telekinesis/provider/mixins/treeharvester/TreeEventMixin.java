package dev.nyon.telekinesis.provider.mixins.treeharvester;

import com.natamus.treeharvester.events.TreeEvent;
import dev.nyon.telekinesis.TelekinesisKt;
import dev.nyon.telekinesis.config.ConfigKt;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(TreeEvent.class)
public class TreeEventMixin {

    @Redirect(
        method = "onTreeHarvest",
        at = @At(
            value = "INVOKE",
            target = "Lcom/natamus/collective_fabric/functions/BlockFunctions;dropBlock(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;)V"
        )
    )
    private static void redirectDrops(
        Level level,
        BlockPos blockPos,
        Level world,
        Player player,
        BlockPos bpos,
        BlockState state,
        BlockEntity blockEntity
    ) {
        var item = new ItemStack(state.getBlock().asItem());
        if ((EnchantmentHelper.getItemEnchantmentLevel(TelekinesisKt.getTelekinesis(), player.getItemInHand(player.getUsedItemHand())) == 0 && !ConfigKt.getConfig().getOnByDefault()) || !ConfigKt.getConfig().getBlockDrops())
            Block.popResource(level, blockPos, item);
        else if (!player.addItem(new ItemStack(state.getBlock().asItem()))) Block.popResource(level, blockPos, item);
        world.setBlock(blockPos, Blocks.AIR.defaultBlockState(), 3);
    }
}
