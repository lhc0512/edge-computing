package cn.edu.scut.util;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ArrayUtils {
    public static float[] toFloatArray(List<Float> list) {
        var res = new float[list.size()];
        for (int i = 0; i < list.size(); i++) {
            res[i] = list.get(i);
        }
        return res;
    }

    public static double[] toDoubleArray(List<Double> list) {
        var res = new double[list.size()];
        for (int i = 0; i < list.size(); i++) {
            res[i] = list.get(i);
        }
        return res;
    }

    public static String arrayToString(int[] array) {
        return Arrays.stream(array).mapToObj(Integer::toString).collect(Collectors.joining(","));
    }

    public static double[] floatToDoubleArray(float[] x) {
        double[] ret = new double[x.length];
        for (int i = 0; i < x.length; i++) {
            ret[i] = x[i];
        }
        return ret;
    }
}
