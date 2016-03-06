package io.goudai.cluster.util;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by freeman on 2016/3/6.
 */
public class MethodUtil {



    public static Set<String> getMethods(String interfaceName) throws ClassNotFoundException {
        Set<String> methods = new HashSet<>();
        Arrays.asList(Class.forName(interfaceName).getMethods()).forEach(method -> methods.add(method.getName()));
        return methods;
    }

    public static Set<String> getMethods(Class<?> interfaceClass) {
        Set<String> methods = new HashSet<>();
        Arrays.asList(interfaceClass.getMethods()).forEach(method -> methods.add(method.getName()));
        return methods;
    }
}
