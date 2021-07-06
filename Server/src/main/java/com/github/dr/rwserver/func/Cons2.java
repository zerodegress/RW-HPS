package com.github.dr.rwserver.func;

/**
 * Cons2 轮循 Map专用
 * @author Dr
 */
public interface Cons2<T, N>{
    /**
     * echo -> .get(k.v)
     * @param t k
     * @param n v
     */
    void get(T t, N n);

    /**
     * 多个cons2
     * @param cons cons
     * @return
     */
    default Cons2<T, N> with(Cons2<T, N> cons){
        return (t, n) -> {
            get(t, n);
            cons.get(t, n);
        };
    }
}
