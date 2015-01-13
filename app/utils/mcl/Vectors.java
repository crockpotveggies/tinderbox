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

import java.util.Vector;

/**
 * Static vector manipulation routines for Matlab porting and other numeric
 * operations. The routines work for int and double partly; the class is
 * extended as needed.
 * <p>
 * TODO: The question remains whether it makes sense to have a formal IntVector,
 * analoguous to IntMatrix that allows performing search and indexing
 * operations, such as views, etc.
 * 
 * @author heinrich
 */
public class Vectors {

    public static int ndigits = 0;

    public static int colwidth = 0;

    /**
     * @param start
     * @param end
     * @param step
     * @return [start : step : end]
     */
    public static int[] range(int start, int end, int step) {

        int[] out = new int[(int) Math.floor((end - start) / step) + 1];
        for (int i = 0; i < out.length; i++) {
            out[i] = start + step * i;
        }
        return out;
    }

    /**
     * @param start
     * @param end
     * @return [start : end]
     */
    public static int[] range(int start, int end) {
        return range(start, end, end - start > 0 ? 1 : -1);
    }

    /**
     * create sequence [start : step : end] of double values. TODO: check
     * precision.
     * 
     * @param start
     *            double value of start, if integer, use "1.0" notation.
     * @param end
     *            double value of end, if integer, use "1.0" notation.
     * @param step
     *            double value of step size
     * @return
     */
    public static double[] range(double start, double end, double step) {

        double[] out = new double[(int) Math.floor((end - start) / step) + 1];
        for (int i = 0; i < out.length; i++) {
            out[i] = start + step * i;
        }
        return out;
    }

    /**
     * @param start
     * @param end
     * @return [start : end]
     */
    public static double[] range(double start, double end) {
        return range(start, end, end - start > 0 ? 1 : -1);
    }

    /**
     * sum the elements of vec
     * 
     * @param vec
     * @return
     */
    public static double sum(double[] vec) {
        double sum = 0;
        for (int i = 0; i < vec.length; i++) {
            sum += vec[i];
        }
        return sum;

    }

    /**
     * sum the elements of vec
     * 
     * @param vec
     * @return
     */
    public static int sum(int vec[]) {
        int sum = 0;
        for (int i = 0; i < vec.length; i++)
            sum += vec[i];

        return sum;
    }

    /**
     * cumulative sum of the elements, starting at element 0.
     * 
     * @param vec
     * @return vector containing the cumulative sum of the elements of vec
     */
    public static double[] cumsum(double[] vec) {
        double[] x = new double[vec.length];
        x[0] = vec[0];
        for (int i = 1; i < vec.length; i++) {
            x[i] = vec[i] + x[i - 1];
        }
        return x;
    }

    /**
     * maximum value in vec
     * 
     * @param vec
     * @return
     */
    public static int max(int[] vec) {
        int max = vec[0];
        for (int i = 1; i < vec.length; i++) {
            if (vec[i] > max)
                max = vec[i];
        }
        return max;
    }

    /**
     * maximum value in vec
     * 
     * @param vec
     * @return
     */
    public static double max(double[] vec) {
        double max = vec[0];
        for (int i = 1; i < vec.length; i++) {
            if (vec[i] > max)
                max = vec[i];
        }
        return max;
    }

    /**
     * minimum value in vec
     * 
     * @param vec
     * @return
     */
    public static int min(int[] vec) {
        int min = vec[0];
        for (int i = 1; i < vec.length; i++) {
            if (vec[i] < min)
                min = vec[i];
        }
        return min;
    }

    /**
     * minimum value in vec
     * 
     * @param vec
     * @return
     */
    public static double min(double[] vec) {
        double min = vec[0];
        for (int i = 1; i < vec.length; i++) {
            if (vec[i] < min)
                min = vec[i];
        }
        return min;
    }

    /**
     * @param x
     * @param y
     * @return [x y]
     */
    public static double[] concat(double[] x, double[] y) {
        double[] z = new double[x.length + y.length];
        System.arraycopy(x, 0, z, 0, x.length);
        System.arraycopy(y, 0, z, x.length, y.length);
        return z;
    }

    /**
     * w = [x y z]
     * 
     * @param x
     * @param y
     * @return [x y z]
     */
    public static double[] concat(double[] x, double[] y, double[] z) {
        double[] w = new double[x.length + y.length + z.length];
        System.arraycopy(x, 0, w, 0, x.length);
        System.arraycopy(y, 0, w, x.length, y.length);
        System.arraycopy(y, 0, w, x.length + y.length, z.length);
        return w;
    }

    /**
     * Create new vector of larger size and data of the argument.
     * 
     * @param vector
     *            source array
     * @param moreelements
     *            number of elements to add
     * @return larger vector
     */
    public static double[] increaseSize(final double[] vector, int moreelements) {
        double[] longer = new double[vector.length + moreelements];
        System.arraycopy(vector, 0, longer, 0, vector.length);
        return longer;
    }

    /**
     * Create new matrix of larger size and data of the argument.
     * 
     * @param matrix
     * @param more
     *            rows
     * @param more
     *            cols
     * @return larger matrix
     */
    public static double[][] increaseSize(final double[][] matrix, int morerows, int morecols) {

        double[][] array2 = new double[matrix.length + morerows][];
        for (int i = 0; i < matrix.length; i++) {
            array2[i] = (morecols > 0) ? increaseSize(matrix[i], morecols) : matrix[i];
        }
        for (int i = matrix.length; i < array2.length; i++) {
            array2[i] = new double[matrix[0].length + morecols];
        }

        return array2;
    }

    /**
     * Create new vector with data of the argument and removed element.
     * 
     * @param vector
     * @param element
     * @return shorter vector
     */
    public static double[] removeElement(final double[] vector, int element) {
        double[] shorter = new double[vector.length - 1];
        System.arraycopy(vector, 0, shorter, 0, element);
        System.arraycopy(vector, element + 1, shorter, element, vector.length - element - 1);
        return shorter;
    }

    /**
     * Create new matrix with data of the argument and removed rows and columns.
     * 
     * @param matrix
     * @param rows
     *            ordered vector of rows to remove
     * @param cols
     *            ordered vector of cols to remove
     * @return smaller matrix
     */
    public static double[][] removeElements(final double[][] matrix, int[] rows, int[] cols) {
        return chooseElements(matrix, rangeComplement(rows, matrix.length), rangeComplement(cols, matrix[0].length));
    }

    /**
     * Create new vector with data of the argument and removed elements.
     * 
     * @param vector
     * @param elements
     *            ordered elements to remove
     * @return smaller vector
     */
    public static double[] removeElements(final double[] vector, int[] elements) {
        return chooseElements(vector, rangeComplement(elements, vector.length));
    }

    /**
     * return the complement of the sorted subset of the set 0:length-1 in
     * Matlab notation
     * 
     * @param set
     *            sorted set of elements < length
     * @param length
     *            of superset of set and its returned complement
     * @return
     */
    public static int[] rangeComplement(int[] set, int length) {
        int[] complement = new int[length - set.length];
        int sindex = 0;
        int cindex = 0;
        for (int i = 0; i < length; i++) {
            if (sindex >= set.length || set[sindex] != i) {
                complement[cindex] = i;
                cindex++;
            } else {
                sindex++;
            }
        }
        return complement;
    }

    /**
     * Create a matrix that contains the rows and columns of the argument matrix
     * in the order given by rows and cols
     * 
     * @param matrix
     * @param rows
     * @param cols
     * @return
     */
    public static double[][] chooseElements(double[][] matrix, int[] rows, int[] cols) {

        double[][] matrix2 = new double[rows.length][cols.length];

        for (int i = 0; i < rows.length; i++) {
            matrix2[i] = chooseElements(matrix[rows[i]], cols);
        }

        return matrix2;
    }

    /**
     * Create vector that contains the elements of the argument in the order as
     * given by keep
     * 
     * @param vector
     * @param keep
     * @return
     */
    public static double[] chooseElements(double[] vector, int[] keep) {
        double[] vector2 = new double[keep.length];

        for (int i = 0; i < keep.length; i++) {
            vector2[i] = vector[keep[i]];
        }
        return vector2;
    }

    /**
     * Create new vector of larger size and data of the argument.
     * 
     * @param vector
     *            source array
     * @param moreelements
     *            number of elements to add
     * @return larger vector
     */
    public static int[] increaseSize(final int[] vector, int moreelements) {
        int[] longer = new int[vector.length + moreelements];
        System.arraycopy(vector, 0, longer, 0, vector.length);
        return longer;
    }

    /**
     * Create new matrix of larger size and data of the argument.
     * 
     * @param matrix
     * @param more
     *            rows
     * @param more
     *            cols
     * @return larger matrix
     */
    public static int[][] increaseSize(final int[][] matrix, int morerows, int morecols) {

        int[][] array2 = new int[matrix.length + morerows][];
        for (int i = 0; i < matrix.length; i++) {
            array2[i] = (morecols > 0) ? increaseSize(matrix[i], morecols) : matrix[i];
        }
        for (int i = matrix.length; i < array2.length; i++) {
            array2[i] = new int[matrix[0].length + morecols];
        }

        return array2;
    }

    /**
     * Create new vector with data of the argument and removed element.
     * 
     * @param vector
     * @param element
     * @return shorter vector
     */
    public static int[] removeElement(final int[] vector, int element) {
        int[] shorter = new int[vector.length - 1];
        System.arraycopy(vector, 0, shorter, 0, element);
        System.arraycopy(vector, element + 1, shorter, element, vector.length - element - 1);
        return shorter;
    }

    /**
     * Create new matrix with data of the argument and removed rows and columns.
     * 
     * @param matrix
     * @param rows
     *            ordered vector of rows to remove
     * @param cols
     *            ordered vector of cols to remove
     * @return smaller matrix
     */
    public static int[][] removeElements(final int[][] matrix, int[] rows, int[] cols) {
        return chooseElements(matrix, rangeComplement(rows, matrix.length), rangeComplement(cols, matrix[0].length));
    }

    /**
     * Create new vector with data of the argument and removed elements.
     * 
     * @param vector
     * @param elements
     *            ordered elements to remove
     * @return smaller vector
     */
    public static int[] removeElements(final int[] vector, int[] elements) {
        return chooseElements(vector, rangeComplement(elements, vector.length));
    }

    /**
     * Create a matrix that contains the rows and columns of the argument matrix
     * in the order given by rows and cols
     * 
     * @param matrix
     * @param rows
     * @param cols
     * @return
     */
    public static int[][] chooseElements(int[][] matrix, int[] rows, int[] cols) {

        int[][] matrix2 = new int[rows.length][cols.length];

        for (int i = 0; i < rows.length; i++) {
            matrix2[i] = chooseElements(matrix[rows[i]], cols);
        }

        return matrix2;
    }

    /**
     * Create vector that contains the elements of the argument in the order as
     * given by keep
     * 
     * @param vector
     * @param keep
     * @return
     */
    public static int[] chooseElements(int[] vector, int[] keep) {
        int[] vector2 = new int[keep.length];

        for (int i = 0; i < keep.length; i++) {
            vector2[i] = vector[keep[i]];
        }
        return vector2;
    }

    /**
     * prints a double representation of the vector.
     * 
     * @param x
     * @return
     */
    public static String print(double[] x) {
        if (x == null)
        	return "null";     
        StringBuffer b = new StringBuffer();
        for (int i = 0; i < x.length - 1; i++) {
            b.append(format(x[i])).append(" ");
        }
        b.append(format(x[x.length - 1]));
        return b.toString();
    }

    private static String format(double x) {
        if (ndigits > 0) {
            return DoubleFormat.format(x, ndigits, colwidth);
        } else {
            return Double.toString(x);
        }
    }

    /**
     * prints a double representation of an array.
     * 
     * @param x
     * @return
     */
    public static String print(double[][] x) {
        if (x == null)
            return "null";
        StringBuffer b = new StringBuffer();
        for (int i = 0; i < x.length - 1; i++) {
            b.append(print(x[i])).append("\n");
        }
        b.append(print(x[x.length - 1]));
        return b.toString();
    }

    /**
     * prints a double representation of the vector.
     * 
     * @param x
     * @return
     */
    public static String print(int[] x) {
        if (x == null)
            return "null";
        StringBuffer b = new StringBuffer();
        for (int i = 0; i < x.length - 1; i++) {
            b.append(x[i]).append(" ");
        }
        b.append(x[x.length - 1]);
        return b.toString();
    }

    /**
     * prints a double representation of an array.
     * 
     * @param x
     * @return
     */
    public static String print(int[][] x) {
        if (x == null)
            return "null";
        StringBuffer b = new StringBuffer();
        for (int i = 0; i < x.length - 1; i++) {
            b.append(print(x[i])).append("\n");
        }
        b.append(print(x[x.length - 1]));
        return b.toString();
    }

    /**
     * @param len
     * @param factor
     * @return factor * ones(1, len);
     */
    public static double[] ones(int len, double factor) {
        double[] x = new double[len];
        for (int i = 0; i < x.length; i++) {
            x[i] = 1;
        }
        return x;
    }

    /**
     * @param len
     * @param factor
     * @return factor * ones(1, len);
     */
    public static int[] ones(int len, int factor) {
        int[] x = new int[len];
        for (int i = 0; i < x.length; i++) {
            x[i] = factor;
        }
        return x;
    }

    /**
     * @param len
     * @return zeros(1, len)
     */
    public static double[] zeros(int len) {
        return new double[len];
    }

    /**
     * @param len
     * @return ones(1, len)
     */
    public static int[] ones(int len) {
        return ones(len, 1);
    }

    /**
     * cast a double[] to an int[]
     * 
     * @param vec
     * @return
     */
    public static int[] cast(double[] vec) {
        int[] ivec = new int[vec.length];
        for (int i = 0; i < ivec.length; i++) {
            ivec[i] = (int) vec[i];
        }
        return ivec;
    }

    /**
     * cast a double[] to an int[]
     * 
     * @param vec
     * @return
     */
    public static double[] cast(int[] vec) {
        double[] dvec = new double[vec.length];
        for (int i = 0; i < dvec.length; i++) {
            dvec[i] = (double) vec[i];
        }
        return dvec;
    }

    /**
     * find indices with val
     * 
     * @param vec
     * @param val
     * @return vector with 0-based indices.
     */
    public static int[] find(int[] vec, int val) {
        Vector<Integer> v = new Vector<Integer>();
        for (int i = 0; i < vec.length; i++) {
            if (vec[i] == val) {
                v.add(new Integer(i));
            }
        }
        int[] vv = new int[v.size()];
        for (int i = 0; i < vv.length; i++) {
            vv[i] = ((Integer) v.get(i)).intValue();
        }
        return vv;
    }

    /**
     * returns a copy of the vector elements with the given indices in the
     * original vector.
     * 
     * @param indices
     * @return
     */
    public static double[] subVector(double[] vec, int[] indices) {
        double[] x = new double[indices.length];
        for (int i = 0; i < x.length; i++) {
            x[i] = vec[indices[i]];
        }
        return x;
    }

    /**
     * returns a copy of the vector elements with the given indices in the
     * original vector.
     * 
     * @param cols
     * @return
     */
    public static int[] subVector(int[] vec, int[] indices) {
        int[] x = new int[indices.length];
        for (int i = 0; i < x.length; i++) {
            x[i] = vec[indices[i]];
        }
        return x;
    }

    /**
     * @param weights
     * @param i
     * @param j
     * @return
     */
    public static double[] subVector(double[] vec, int start, int end) {
        double[] x = new double[end - start + 1];
        for (int i = 0; i <= end - start; i++) {
            x[i] = vec[start + i];
        }
        return x;
    }

    /**
     * set the elements of vec at indices with the respective replacements.
     * TODO: implement views as in the colt library
     * 
     * @param vec
     * @param indices
     * @param replacements
     * @return
     */
    public static void setSubVector(int[] vec, int[] indices, int[] replacements) {
        for (int i = 0; i < indices.length; i++) {
            vec[indices[i]] = replacements[i];
        }
    }

    /**
     * set the elements of vec at indices with the replacement. 
     * 
     * TODO: implement views as in the colt library
     * 
     * @param vec
     * @param indices
     * @param replacement
     * @return
     */
    public static void setSubVector(int[] vec, int[] indices, int replacement) {
        for (int i = 0; i < indices.length; i++) {
            vec[indices[i]] = replacement;
        }
    }

    /**
     * add a scalar to the vector
     * 
     * @param vec
     * @param scalar
     */
    public static void add(int[] vec, int scalar) {
        for (int i = 0; i < vec.length; i++) {
            vec[i] += scalar;
        }
    }

    /**
     * set the elements of a copy of vec at indices with the respective
     * replacements. TODO: implement views as in the colt library
     * 
     * @param vec
     * @param indices
     * @param replacements
     * @return the copied vector with the replacements;
     */
    public static int[] setSubVectorCopy(int[] vec, int[] indices, int[] replacements) {
        int[] x = new int[vec.length];
        for (int i = 0; i < indices.length; i++) {
            x[indices[i]] = replacements[i];
        }
        return x;
    }

    /**
     * copies a the source to the destination
     * 
     * @param alpha
     * @return
     */
    public static double[] copy(double[] source) {
        if (source == null)
            return null;
        double[] dest = new double[source.length];
        System.arraycopy(source, 0, dest, 0, source.length);
        return dest;
    }

    /**
     * copies a the source to the destination
     * 
     * @param alpha
     * @return
     */
    public static int[] copy(int[] source) {
        if (source == null)
            return null;
        int[] dest = new int[source.length];
        System.arraycopy(source, 0, dest, 0, source.length);
        return dest;
    }

    /**
     * multiplicates the vector with a scalar. The argument is modified.
     * 
     * @param ds
     * @param d
     * @return
     */
    public static double[] mult(double[] ds, double d) {
        for (int i = 0; i < ds.length; i++) {
            ds[i] *= d;
        }
        return ds;

    }

    /**
     * multiplicates the vector with a vector (inner product). The argument is
     * not modified.
     * 
     * @param ds
     * @param d
     * @return
     */
    public static double mult(double[] ds, double[] dt) {
        if (ds.length != dt.length)
            throw new IllegalArgumentException("Vector dimensions must agree.");
        double s = 0;
        for (int i = 0; i < ds.length; i++) {
            s += ds[i] * dt[i];
        }
        return s;
    }

    /**
     * transpose the matrix
     * 
     * @param mat
     * @return
     */
    public static double[][] transpose(double[][] mat) {
        double[][] a = new double[mat[0].length][mat.length];
        for (int i = 0; i < mat[0].length; i++) {
            for (int j = 0; j < mat.length; j++) {
                a[i][j] = mat[j][i];
            }
        }
        return a;
    }

    /**
     * transpose the matrix
     * 
     * @param mat
     * @return
     */
    public static int[][] transpose(int[][] mat) {
        int[][] a = new int[mat[0].length][mat.length];
        for (int i = 0; i < mat[0].length; i++) {
            for (int j = 0; j < mat.length; j++) {
                a[i][j] = mat[j][i];
            }
        }
        return a;
    }
}
