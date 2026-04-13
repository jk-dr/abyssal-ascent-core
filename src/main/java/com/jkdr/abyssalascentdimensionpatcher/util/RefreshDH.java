package com.jkdr.abyssalascentdimensionpatcher.util;

import com.jkdr.abyssalascentdimensionpatcher.interfaces.IKeyedClientLevelManagerAccess;
import com.jkdr.abyssalascentdimensionpatcher.mixins.compat.distantHorizons.IMixinClientApi;
import com.jkdr.abyssalascentdimensionpatcher.mixins.compat.distantHorizons.IMixinClientPluginChannelApi;
import com.seibel.distanthorizons.api.methods.events.abstractEvents.DhApiLevelLoadEvent;
import com.seibel.distanthorizons.core.api.internal.ClientApi;
import com.seibel.distanthorizons.core.api.internal.ClientPluginChannelApi;
import com.seibel.distanthorizons.core.api.internal.SharedApi;
import com.seibel.distanthorizons.core.level.IKeyedClientLevelManager;
import com.seibel.distanthorizons.core.level.IServerKeyedClientLevel;
import com.seibel.distanthorizons.core.wrapperInterfaces.world.IClientLevelWrapper;
import com.seibel.distanthorizons.coreapi.DependencyInjection.ApiEventInjector;
import java.util.HashMap;
import loaderCommon.forge.com.seibel.distanthorizons.common.wrappers.world.ClientLevelWrapper;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

public class RefreshDH {
    
    /**
     * Refreshes the Distant Horizons level state when switching dimensions.
     * @param fromLevel The level the player is leaving.
     * @param toLevel The level the player is entering.
     */
    public static void refreshDh(ClientLevel fromLevel, ClientLevel toLevel) {
        IKeyedClientLevelManager manager = DistantHorizonsCaching.getDhKeyedLevelManager();
        ClientPluginChannelApi clientPluginChannel = ((IMixinClientApi)ClientApi.INSTANCE).getPluginChannelApi();

        if (manager != null) {
            IServerKeyedClientLevel currentKeyedLevel = manager.getServerKeyedLevel();
            IServerKeyedClientLevel exitLevelCache = getCachedClientLevel(fromLevel);
            IServerKeyedClientLevel enterLevelCache = getCachedClientLevel(toLevel);

            // If the dimension we are leaving isn't in our local cache yet, add it.
            if (exitLevelCache == null) {
                ResourceKey<Level> dimensionKey = fromLevel.dimension(); // m_46472_
                ResourceLocation identifier = dimensionKey.location();   // m_135782_
                DistantHorizonsCaching.localDHKeyCache.put(identifier.toString(), currentKeyedLevel);
                exitLevelCache = getCachedClientLevel(fromLevel);
            }

            // Tell Distant Horizons to unload the 'from' level resources.
            ((IMixinClientPluginChannelApi)clientPluginChannel).getUnloadConsumer().accept(exitLevelCache);

            // Set the new 'to' level in the manager.
            if (enterLevelCache == null) {
                manager.clearKeyedLevel();
            } else {
                // This custom method name suggests it fixes a specific conflict with Immersive Portals
                ((IKeyedClientLevelManagerAccess)manager).setKeyedLevelForImmersivePortalsSupportPlease(enterLevelCache);
            }

            // Finalize the level load within the DH engine.
            IClientLevelWrapper wrapper = ClientLevelWrapper.getWrapper(toLevel);
            SharedApi.getAbstractDhWorld().getOrLoadLevel(wrapper);
            
            // Trigger the DH API events so other mods/subsystems know the level changed.
            ApiEventInjector.INSTANCE.fireAllEvents(DhApiLevelLoadEvent.class, new DhApiLevelLoadEvent.EventParam(wrapper));
        }
    }

    /**
     * Helper to look up a Distant Horizons 'KeyedLevel' from a local HashMap cache.
     */
    private static IServerKeyedClientLevel getCachedClientLevel(ClientLevel selectedLevel) {
        if (DistantHorizonsCaching.localDHKeyCache == null) {
            DistantHorizonsCaching.localDHKeyCache = new HashMap();
            return null;
        } else {
            ResourceKey<Level> dimensionKey = selectedLevel.dimension(); // m_46472_
            ResourceLocation identifier = dimensionKey.location();       // m_135782_
            return (IServerKeyedClientLevel)DistantHorizonsCaching.localDHKeyCache.get(identifier.toString());
        }
    }
}