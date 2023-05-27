/*
 * Copyright 2020-2023 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package net.rwhps.server.struct

import net.rwhps.server.func.Cons
import net.rwhps.server.func.Find

/**
 * @date  2023/5/26 13:55
 * @author  RW-HPS/Dr
 */
abstract class BaseSeq<T>(protected val list: java.util.List<T>): MutableList<T>, List<T> {

    override val size: Int get() = list.size
    override fun add(element: T): Boolean = list.add(element)
    override fun add(index: Int, element: T) = list.add(index, element)
    override fun addAll(elements: Collection<T>): Boolean = list.addAll(elements)
    override fun addAll(index: Int, elements: Collection<T>): Boolean = list.addAll(index, elements)

    /**
     * 移除最后一个元素
     * @return T
     */
    fun pop(): T = removeAt(size - 1)
    /**
     * 获取最后一个元素
     * @return T
     */
    fun peek(): T = get(size - 1)
    /**
     * 获取第一个元素
     * @return T
     */
    fun first(): T = get(0)
    override fun isEmpty(): Boolean = list.isEmpty()
    override fun iterator(): MutableIterator<T> = list.iterator()
    override fun listIterator(): MutableListIterator<T> = list.listIterator()
    override fun listIterator(index: Int): MutableListIterator<T> = list.listIterator(index)
    fun any(): Boolean = list.size > 0
    override fun get(index: Int): T = list[index]
    override fun indexOf(element: @UnsafeVariance T): Int = list.indexOf(element)
    override fun lastIndexOf(element: @UnsafeVariance T): Int = list.lastIndexOf(element)
    override fun remove(element: T): Boolean = list.remove(element)
    override fun removeAt(index: Int): T = list.remove(index)
    override fun removeAll(elements: Collection<T>): Boolean = list.removeAll(elements.toSet())
    override fun subList(fromIndex: Int, toIndex: Int): MutableList<T> = list.subList(fromIndex, toIndex)
    override fun set(index: Int, element: T): T = list.set(index, element)
    override fun retainAll(elements: Collection<T>): Boolean = list.retainAll(elements.toSet())
    override fun contains(element: T): Boolean = list.contains(element)
    override fun containsAll(elements: Collection<T>): Boolean = list.containsAll(elements)

    fun find(findA: Find<T, Boolean>): T? {
        list.forEach { if (findA(it)) return it }
        return null
    }

    fun find(findA: Find<T, Boolean>, findB: Find<T, Boolean>): T? {
        list.forEach { if (findA(it) && findB(it)) return it }
        return null
    }

    fun eachAll(block: Cons<T>) {
        list.forEach { block(it) }
    }

    fun eachAllFind(find: Find<T, Boolean>, block: Cons<T>) = eachAll {
        if (find(it)) {
            block(it)
        }
    }

    fun eachFind(find: Find<T, Boolean>, block: Cons<T>) = find(find)?.let { block(it) }
    fun eachAllFinds(findA: Find<T, Boolean>, findB: Find<T, Boolean>, block: Cons<T>) = find(findA, findB)?.let { block(it) }

    override fun clear() = list.clear()

    @Suppress("UNCHECKED_CAST")
    fun <E> toArray(classJava: Class<E>): Array<E> = list.toArray(java.lang.reflect.Array.newInstance(classJava, size) as Array<out E>)

    override fun toString(): String = list.toString()
}