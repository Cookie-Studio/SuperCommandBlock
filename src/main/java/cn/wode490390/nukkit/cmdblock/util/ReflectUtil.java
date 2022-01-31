package cn.wode490390.nukkit.cmdblock.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ReflectUtil {
    public static Object callUnaccessibleMethod(Object obj,String methodName,Object... args){
        Class clazz = obj.getClass();
        Method method = null;
        Object result = null;
        try {
            method = clazz.getMethod("getStepHeight");
            method.setAccessible(true);
            result = method.invoke(obj,args);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return result;
    }
}
