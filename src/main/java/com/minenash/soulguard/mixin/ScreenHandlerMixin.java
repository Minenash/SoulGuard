package com.minenash.soulguard.mixin;

import com.minenash.soulguard.inspect.OpInspectScreenHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ScreenHandler.class)
public class ScreenHandlerMixin {

    @Unique private static ScreenHandler handler;

    @Inject(method = "internalOnSlotClick", at = @At("HEAD"))
    private void getHandler(int slotIndex, int button, SlotActionType actionType, PlayerEntity player, CallbackInfo ci) {
        handler = (ScreenHandler)(Object)this;
    }

    @Inject(method = "canInsertItemIntoSlot", at = @At("HEAD"), cancellable = true)
    private static void ignoreStructureVoid(Slot slot, ItemStack stack, boolean allowOverflow, CallbackInfoReturnable<Boolean> cir) {
        if ( slot == null || (handler instanceof OpInspectScreenHandler && slot.getStack().isOf(Items.STRUCTURE_VOID) && slot.getStack().hasCustomName()) )
            cir.setReturnValue(true);
    }

    @Redirect(method = "internalOnSlotClick", at = @At(value = "INVOKE", ordinal = 3, target = "Lnet/minecraft/item/ItemStack;getCount()I"))
    public int getCount(ItemStack stack) {
        return stack.isOf(Items.STRUCTURE_VOID) && stack.hasCustomName() ? 0 : stack.getCount();
    }

}
