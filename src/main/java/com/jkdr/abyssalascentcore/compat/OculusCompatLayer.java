package com.jkdr.abyssalascentcore.compat;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;

public class OculusCompatLayer {
    private static final Supplier<Boolean> IRIS_PACKAGE_PRESENT =
            Suppliers.memoize(() -> classExists("net.irisshaders.iris.Iris"));

    public static boolean hasNewIrisPackage() {
        return IRIS_PACKAGE_PRESENT.get();
    }

    private static boolean classExists(String name) {
        try {
            Class.forName(name, false, OculusCompatLayer.class.getClassLoader());
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
