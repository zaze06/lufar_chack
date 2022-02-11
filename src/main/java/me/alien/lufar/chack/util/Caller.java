package me.alien.lufar.chack.util;

import me.alien.lufar.chack.Main;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class Caller {
    public static List<Pair<Annotation, Method>> getMethodsAnnotatedWith(final Class<? extends Annotation> annotation) {
        final List<Pair<Annotation, Method>> methods = new ArrayList<>();
        for(Class<?> type : Main.listeners) {
            Class<?> klass = type;
            while (klass != Object.class) { // need to iterated thought hierarchy in order to retrieve methods from above the current instance
                // iterate though the list of methods declared in the class represented by klass variable, and add those annotated with the specified annotation
                for (final Method method : klass.getDeclaredMethods()){
                    if (method.isAnnotationPresent(annotation)) {
                        Annotation annotInstance = method.getAnnotation(annotation);
                        methods.add(new Pair<>(annotInstance, method));
                    }
                }
                // move to the upper class in the hierarchy in search for more methods
                klass = klass.getSuperclass();
            }
        }
        return methods;
    }
}
