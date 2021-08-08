package com.github.dr.rwserver.util.alone.annotations;

import java.lang.annotation.*;

/**
 * 指定的方法是Server的主要实现 里面的部分方法为protected 而不是private
 * 一般来说，这并不会改变IDEA的行为——它只是一个标记，表明指定方法是未完成的。
 * The specified method is the main implementation of the server. Some of the methods are protected rather than private
 * In general, this doesn't change the behavior of the idea - it's just a token that indicates that the specified method is incomplete。
 * @author Dr
 */
@Documented
@Retention(RetentionPolicy.CLASS)
@Target({ElementType.TYPE,ElementType.METHOD})
public @interface MainProtocolImplementation {
}