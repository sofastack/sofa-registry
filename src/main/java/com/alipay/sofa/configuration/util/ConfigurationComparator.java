package com.alipay.sofa.configuration.util;

import com.alipay.sofa.configuration.Configuration;

import java.util.Comparator;

public class ConfigurationComparator implements Comparator<Configuration> {

    public static final ConfigurationComparator DEFAULT = new ConfigurationComparator();

    public int compare(Configuration o1, Configuration o2) {
        if (o1 == o2) {
            return 0;
        }

        if (o1 == null) {
            return 1;
        }

        if (o2 == null) {
            return -1;
        }

        return (o1.order() < o2.order()) ? -1 : ((o1.order() == o2.order()) ? 0 : 1);
    }

}
