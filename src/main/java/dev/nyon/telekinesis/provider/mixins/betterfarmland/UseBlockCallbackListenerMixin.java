package dev.nyon.telekinesis.provider.mixins.betterfarmland;

import de.rafael.mods.better.fabric.callback.UseBlockCallbackListener;
import de.rafael.mods.better.farmland.BetterFarmland;
import de.rafael.mods.better.farmland.classes.BlockChange;
import de.rafael.mods.better.farmland.mixin.CropBlockInvoker;
import dev.nyon.telekinesis.TelekinesisKt;
import dev.nyon.telekinesis.config.ConfigKt;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;
import java.util.Optional;

@Mixin(UseBlockCallbackListener.class)
public abstract class UseBlockCallbackListenerMixin {

    @Redirect(
        method = "interact",
        at = @At(
            value = "INVOKE",
            target = "Lde/rafael/mods/better/farmland/logic/UseBlockLogic;interact(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/InteractionHand;Lnet/minecraft/world/phys/BlockHitResult;)Lnet/minecraft/world/InteractionResult;"
        )
    )
    private static InteractionResult manipulateDrops(
        Level level,
        InteractionHand interactionHand,
        BlockHitResult hitResult,
        Player player,
        Level _level,
        InteractionHand _interactionHand,
        BlockHitResult _hitResult
    ) {
        if (!level.isClientSide() && interactionHand == InteractionHand.MAIN_HAND && level instanceof ServerLevel serverWorld) {
            BlockState blockState = level.getBlockState(hitResult.getBlockPos());
            if (blockState.getBlock() instanceof CropBlock cropBlock && blockState.getValue(cropBlock.getAgeProperty()) == cropBlock.getMaxAge()) {
                List<ItemStack> itemStacks = Block.getDrops(blockState, serverWorld, hitResult.getBlockPos(), null);
                Optional<ItemStack> seedStack = itemStacks.stream().filter(itemStack -> itemStack.is(((CropBlockInvoker) cropBlock).invokeGetSeedsItem().asItem())).findFirst();
                if (seedStack.isPresent()) {
                    int newCount = seedStack.get().getCount() - 1;
                    if (newCount < 1) {
                        itemStacks.remove(seedStack.get());
                    } else {
                        seedStack.get().setCount(newCount);
                    }
                }
                for (ItemStack itemStack : itemStacks) {
                    if ((EnchantmentHelper.getItemEnchantmentLevel(TelekinesisKt.getTelekinesis(), player.getItemInHand(player.getUsedItemHand())) == 0 && !ConfigKt.getConfig().getOnByDefault()) || !ConfigKt.getConfig().getBlockDrops())
                        Block.popResource(level, hitResult.getBlockPos(), itemStack);
                    else if (!player.addItem(itemStack)) Block.popResource(level, hitResult.getBlockPos(), itemStack);
                }

                level.setBlock(hitResult.getBlockPos(), cropBlock.getStateForAge(0), Block.UPDATE_ALL);

                for (BlockChange.ChangeSound harvestSound : BetterFarmland.getConfigManager().getHarvestSounds()) {
                    level.playSound(null, hitResult.getBlockPos(), harvestSound.sound(), SoundSource.BLOCKS, harvestSound.soundVolume(), harvestSound.soundPitch());
                }
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.PASS;
    }
}
