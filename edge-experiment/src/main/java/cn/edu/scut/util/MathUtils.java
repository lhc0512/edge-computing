package cn.edu.scut.util;

import java.util.ArrayList;

public class MathUtils {
    public static double avg(ArrayList<Double> data) {
        double sum = 0;
        for (Double val : data) {
            sum += val;
        }
        return sum / data.size();
    }

    public static double std(ArrayList<Double> data) {
        double res = 0;
        double avg = avg(data);
        for (Double val : data) {
            res += Math.pow((val - avg), 2);
        }
        res /= data.size() - 1;
        res = Math.sqrt(res);
        return res;
    }
}
