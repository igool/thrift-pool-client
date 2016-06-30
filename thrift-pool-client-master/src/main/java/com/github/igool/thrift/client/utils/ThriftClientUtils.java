/**
 * 
 */
package com.github.igool.thrift.client.utils;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * <p>
 * ThriftClientUtils class.
 * 修改了getInterfaceMethodNames的方法，去掉jdk8的写法，使用jdk7的写法
 * </p>
 *
 * @author w.vela
 * @author igool
 * @version $Id: $Id
 */
public final class ThriftClientUtils {

    private static final Random RANDOM = new Random();
    private static ConcurrentMap<Class<?>, Set<String>> interfaceMethodCache = new ConcurrentHashMap<>();

    private ThriftClientUtils() {
        throw new UnsupportedOperationException();
    }

    /**
     * <p>
     * randomNextInt.
     * </p>
     *
     * @return a int.
     */
    public static int randomNextInt() {
        return RANDOM.nextInt();
    }

    /**
     * <p>
     * getInterfaceMethodNames.
     * </p>
     *
     * @param ifaceClass a {@link java.lang.Class} object.
     * @return a {@link java.util.Set} object.
     */
    public static Set<String> getInterfaceMethodNames(Class<?> ifaceClass) {
    	Set<String> methodSet = new HashSet<String>();
    	Class[] classes = ifaceClass.getInterfaces();
    	for(Class classItem : classes){
    		Method[] methodes = classItem.getMethods();
    		for(Method method : methodes){
    			methodSet.add(method.getName());
    		}
    	}
    	interfaceMethodCache.putIfAbsent(ifaceClass,methodSet);
    	return methodSet;
       /* return interfaceMethodCache.putIfAbsent(ifaceClass, i -> of(i.getInterfaces()) //
                .flatMap(c -> of(c.getMethods())) //
                .map(Method::getName) //
                .collect(toSet()));*/
    }
}
