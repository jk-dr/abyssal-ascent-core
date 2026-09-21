package com.jkdr.abyssalascentcore.mixins.optimisations.entity.alexscavesexemplified;

import com.github.alexmodguy.alexscaves.server.entity.living.SeaPigEntity;
import java.util.Iterator;
import java.util.List;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.living.LivingEvent.LivingTickEvent;
import org.crimsoncrips.alexscavesexemplified.AlexsCavesExemplified;
import org.crimsoncrips.alexscavesexemplified.misc.ACExUtils;
import org.crimsoncrips.alexscavesexemplified.server.events.ACExemplifiedEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ACExemplifiedEvents.class, remap = false)
public abstract class MixinACExemplifiedEvents {

    @Inject(
        method = "mobTickEvents",
        at = @At("HEAD"),
        cancellable = true
    )
    private void onMobTickEvents(LivingTickEvent event, CallbackInfo ci) {

        LivingEntity livingEntity = event.getEntity();

        if (livingEntity == null) return;

        Level level = livingEntity.level();

        if (level == null || level.isClientSide()) return;

        RandomSource rand = livingEntity.getRandom();

        boolean poisonousSkinEnabled =
                AlexsCavesExemplified.COMMON_CONFIG.POISONOUS_SKIN_ENABLED.get();

        if (livingEntity instanceof SeaPigEntity seaPig) {

            if (poisonousSkinEnabled && rand.nextDouble() < 0.01D) {

                List<LivingEntity> nearby =
                        level.getEntitiesOfClass(
                                LivingEntity.class,
                                seaPig.getBoundingBox().inflate(0.5D),
                                e -> e != seaPig
                                        && e.distanceTo(seaPig) <= 3.5F
                                        && !(e instanceof SeaPigEntity)
                        );

                for (LivingEntity entity : nearby) {

                    entity.addEffect(
                            new MobEffectInstance(MobEffects.POISON, 60, 0)
                    );

                    ACExUtils.awardAdvancement(
                            entity,
                            "poisonous_skin",
                            "touched"
                    );
                }

                ci.cancel();
            }
        }
    }
}