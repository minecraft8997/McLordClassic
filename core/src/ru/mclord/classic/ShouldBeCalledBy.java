package ru.mclord.classic;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.CONSTRUCTOR})
public @interface ShouldBeCalledBy {
    String thread();
}
