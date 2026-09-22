//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.jkdr.abyssalascentdimensionpatcher.util;

import com.seibel.distanthorizons.core.dependencyInjection.SingletonInjector;
import com.seibel.distanthorizons.core.level.IKeyedClientLevelManager;
import com.seibel.distanthorizons.core.level.IServerKeyedClientLevel;
import java.util.Map;

public class DistantHorizonsCaching {
    public static IKeyedClientLevelManager dhKeyedLevelManager = null;
    public static Map<String, IServerKeyedClientLevel> localDHKeyCache;

    public static IKeyedClientLevelManager getDhKeyedLevelManager() {
        if (dhKeyedLevelManager == null) {
            try {
                dhKeyedLevelManager = (IKeyedClientLevelManager)SingletonInjector.INSTANCE.get(IKeyedClientLevelManager.class);
            } catch (Exception e) {
                System.err.println("Could not get Distant Horizons KeyedClientLevelManager");
                e.printStackTrace();
            }
        }

        return dhKeyedLevelManager;
    }
}
