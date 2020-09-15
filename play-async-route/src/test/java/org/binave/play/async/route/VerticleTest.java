package org.binave.play.async.route;

import org.junit.Test;

/**
 * @author by bin jin on 2020/9/14 23:35.
 */
public class VerticleTest {

    class x extends JsonDataVerticle<Integer> {

        @Override
        public <T> T handler(Integer integer) {
            return null;
        }
    }

    class y extends x {

    }

    class z extends y {

    }

    @Test
    public void test() {

        new x();
        new y();
        new z();

    }

}
