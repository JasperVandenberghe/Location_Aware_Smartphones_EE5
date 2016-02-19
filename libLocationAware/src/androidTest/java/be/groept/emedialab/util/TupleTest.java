package be.groept.emedialab.util;

import junit.framework.TestCase;

import org.junit.Test;

/**
 * Created by Yoika on 6/10/2015.
 */
public class TupleTest extends TestCase {

    @Test
    public void testConstructor(){
        Tuple<String, String> tuple = new Tuple<>("Yoika", "Joren");
        assertEquals("Yoika", tuple.element1);
        assertEquals("Joren", tuple.element2);
    }

}