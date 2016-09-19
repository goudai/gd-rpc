package io.goudai.cluster.util;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by freeman on 2016/3/6.
 */
public class MethodUtil {


    public static Set<String> getMethods(String interfaceName) throws ClassNotFoundException {
        return getMethods(Class.forName(interfaceName));
    }

    public static Set<String> getMethods(Class<?> interfaceClass) {
        Set<String> methods = new HashSet<>();
        Arrays.asList(interfaceClass.getMethods()).stream().map(m -> m.getName()).forEach(methods::add);
        return methods;
    }
}
