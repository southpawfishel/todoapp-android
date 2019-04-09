package com.southpawfishel.todoapp

interface Lens<P, C> {
    fun get(parent: P): C
    fun set(parent: P, newChild: C): P

    operator fun <C1> times(rhs: Lens<C, C1>): Lens<P, C1> = object : Lens<P, C1> {
        override fun get(parent: P): C1 = rhs.get(this@Lens.get(parent))
        override fun set(parent: P, newChild: C1): P = this@Lens.set(parent, rhs.set(this@Lens.get(parent), newChild))
    }
}

class KeyLens<K, V> {
    companion object {
        fun <K,V> forKey(key: K): Lens<Map<K, V>, V> = object : Lens<Map<K, V>, V> {
            override fun get(parent: Map<K, V>) : V = parent.getValue(key)
            override fun set(parent: Map<K, V>, newChild: V): Map<K, V> {
                val temp = parent.toMutableMap()
                temp.set(key, newChild)
                return temp.toMap()
            }
        }
    }
}

class IndexLens<V> {
    companion object {
        fun <V> forIndex(index: Int): Lens<List<V>, V> = object : Lens<List<V>, V> {
            override fun get(parent: List<V>) : V = parent.get(index)
            override fun set(parent: List<V>, newChild: V): List<V> {
                val temp = parent.toMutableList()
                temp.set(index, newChild)
                return temp.toList()
            }
        }
    }
}