package org.openengsb.core.edbi.performance;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * PlcGenerator
 */
public class PlcGenerator {
    private static final Logger LOG = LoggerFactory.getLogger(PlcGenerator.class);

    private final Random rng = new Random();

    public Plc nextObject() {
        Plc plc = new Plc();

        modify(plc);

        return plc;
    }

    public List<Plc> generate(int n) {
        List<Plc> plcs = new ArrayList<>(n);

        for (int i = 0; i < n; i++) {
            plcs.add(nextObject());
        }

        return plcs;
    }

    public void modify(Collection<Plc> plcs) {
        for (Plc plc : plcs) {
            modify(plc);
        }
    }

    public void modify(Plc plc) {
        String randomString1 = randomString(random(20, 100));
        String randomString2 = randomString(random(20, 100));
        plc.setCustom1(randomString1);
        plc.setCustom2(randomString1);
        plc.setCustom3(randomString1);
        plc.setCustom4(randomString1);
        plc.setCustom5(randomString1);
        plc.setCustom6(randomString1);
        plc.setCustom7(randomString1);
        plc.setCustom8(randomString1);
        plc.setCustom9(randomString1);
        plc.setCustom10(randomString2);
        plc.setCustom11(randomString2);
        plc.setCustom12(randomString2);
        plc.setCustom13(randomString2);
        plc.setCustom14(randomString2);
        plc.setCustom15(randomString2);
        plc.setCustom16(randomString2);
        plc.setCustom17(randomString2);
        plc.setCustom18(randomString2);
        plc.setCustom19(randomString2);
        plc.setCustom20(randomString2);
    }

    public int random(int min, int max) {
        return min + ((int) (rng.nextDouble() * (max - min)));
    }

    public String randomString(int len) {
        return org.apache.commons.lang.RandomStringUtils.randomAlphanumeric(len);
    }

}
