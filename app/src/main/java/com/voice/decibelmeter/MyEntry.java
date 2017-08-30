package com.voice.decibelmeter;

import java.util.Map;

/**
 * Map의 Iteration에서 사용할 Entry
 * @param <K> Key 값의 타입
 * @param <V> Value 값의 타입
 */

final class MyEntry<K, V> implements Map.Entry<K, V> {
    private final K key;
    private V value;

    public MyEntry(K key, V value) {
        this.key = key;
        this.value = value;
    }

    @Override
    public K getKey() {
        return key;
    }

    @Override
    public V getValue() {
        return value;
    }

    @Override
    public V setValue(V value) {
        V old = this.value;
        this.value = value;
        return old;
    }
}
