package be.groept.emedialab.util;

/**
 * A pair of two Objects. This class uses generics so that the type of the elements can be set.
 * @param <T1> Type of element1
 * @param <T2> Type of element2
 */
public class Tuple<T1, T2> {
    public final T1 element1;
    public final T2 element2;

    public Tuple(T1 element1, T2 element2){
        this.element1 = element1;
        this.element2 = element2;
    }
}
