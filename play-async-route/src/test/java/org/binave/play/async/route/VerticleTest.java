package org.binave.play.async.route;

import org.junit.Test;

import java.util.List;

/**
 * @author by bin jin on 2020/9/14 23:35.
 */
public class VerticleTest {

    @Test
    public void test() {
        new JsonDataVerticle<String>() {

            @Override
            public List handler(String pojo) {
                return null;
            }

        };
    }

}
