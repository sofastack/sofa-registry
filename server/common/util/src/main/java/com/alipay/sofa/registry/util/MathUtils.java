package com.alipay.sofa.registry.util;

/**
 * @author chen.zhu
 * <p>
 * Jan 12, 2021
 */
public class MathUtils {


    /**
     * Divide ceil int.
     * dividend         divisor         quotient
     * 20         /       4        =     5
     *
     * @param dividend the dividend
     * @param divisor  the divisor
     * @return the int
     */
    public static int divideCeil(int dividend, int divisor) {
        int result = dividend / divisor;
        result += dividend % divisor == 0 ? 0 : 1;
        return result;
    }
}
