/**
 * This file is part of the Java Machine Learning Library
 * 
 * The Java Machine Learning Library is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * The Java Machine Learning Library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with the Java Machine Learning Library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 * 
 * Copyright (c) 2006-2009, Thomas Abeel
 * 
 * Project: http://java-ml.sourceforge.net/
 * 
 */
package utils.mcl;

import java.util.Arrays;

/**
 * DoubleFormat formats double numbers into a specified digit format. The
 * difference to NumberFormat and DecimalFormat is that this DigitFormat is
 * based on a strategy to focus on a given number of nonzero digits, for which
 * it rounds numbers if necessary. In addition, it is possible to format a
 * number into a String that has a specified length, with a strategy to first
 * obey the significant-digit rule and then try to either space-pad the string
 * on the left or shorten the string by rounding, converting to
 * mantissa-exponent format or indicating overflow or underflow. Although the
 * class does not yield separator-aligned numbers, it yields a readable output.
 * 
 * @author heinrich
 */
public class DoubleFormat {

    /**
     * Format the number x with n significant digits.
     * 
     * @param x
     * @param ndigits
     * @return
     */
    public static double format(double x, int ndigits) {
        int magnitude = ExpDouble.orderOfMagnitude(x);
        double factor = Math.pow(10, ndigits - 1 - magnitude);
        double y = Math.round(x * factor) / factor;

        return y;
    }

    /**
     * Format the number so it becomes exactly as large as the argument strlen.
     * The number of nonzero digits is tried to be ndigits. If strlen does not
     * have enough space for this, this number is reduced or the formatting
     * switched to exponential notation (in that priority order). If this is not
     * successful, null is returned;
     * <p>
     * TODO: debug so that lengths < 5 can be allowed
     *  
     * TODO: debug conditions for exponential notation
     *  
     * TODO: debug truncation for mixed numbers
     * 
     * @param x the number to be formatted.
     * @param ndigits the maximum number of significant (non-zero) digits
     * @param strlen the exact number of characters in the string returned
     *        (positive for right-aligned, negative for left-aligned)
     * @return the formatted number string or null
     */
    static String format(double x, int ndigits, int strlen) {
        String s = null;
        boolean leftalign = false;
        if (strlen < 0) {
            strlen = -strlen;
            leftalign = true;
        }
        if (strlen < 5)
            throw new IllegalArgumentException("Cannot use yet abs(strlen) < 5");

        int pad = 0;

        ExpDouble d = new ExpDouble(x, ndigits);
        int len = d.strlen();
        pad = strlen - len;
        if (pad >= 0) {
            s = d.toString();
        } else {
            // if it has a fraction, it can be right-trucated
            if (d.exponent < 0) {
                // retry rounding if at least one digit can be displayed as
                // 0.00#
                int newdigits = d.digits - -pad;
                // at least one digit necessary
                if (newdigits > 0) {
                    boolean plusone = d.round(newdigits);
                    // if rounding up to next order of magnitude
                    if (plusone && newdigits > 1) {
                        d.round(newdigits - 1);
                    } else {
                        s = exponentialNotation(d, strlen);
                    }
                    s = d.toString();
                } else {
                    s = exponentialNotation(d, strlen);
                }
            } else {
                // try exponential notation (1.2E3)
                s = exponentialNotation(d, strlen);
            }
            pad = strlen - s.length();
        }

        String spc = space(pad);
        if (leftalign) {
            return s + spc;
        } else {
            return spc + s;
        }
    }

    /**
     * @param d expdouble object
     * @param strlen maximum string length
     * @return number as string in exponential notation
     */
    private static String exponentialNotation(ExpDouble d, int strlen) {
        String s;
        int len;
        len = d.strlenexp();
        int minlen = d.minstrlenexp();
        if (minlen > strlen) {
            char[] cc = new char[strlen];
            if (d.exponent >= 0) {
                Arrays.fill(cc, '>');
            } else {
                Arrays.fill(cc, '<');
            }
            s = new String(cc);
        }

        // do all digits fit?
        int diff = strlen - len;
        if (diff == -1) {
            // at least two digits (with additional point
            d.round(d.digits + diff);
        } else {
            // only one digit (this yields minlen)
            d.round(1);
        }
        s = d.toExpString();
        return s;
    }

    private static String space(int pad) {
        char[] ch = new char[pad];
        Arrays.fill(ch, ' ');
        String spc = new String(ch);
        return spc;
    }

}
