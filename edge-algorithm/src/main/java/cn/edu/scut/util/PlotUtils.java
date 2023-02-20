package cn.edu.scut.util;

import tech.tablesaw.plotly.components.Axis;
import tech.tablesaw.plotly.components.Figure;
import tech.tablesaw.plotly.components.Layout;
import tech.tablesaw.plotly.traces.ScatterTrace;

public class PlotUtils {
    public static Figure plot(double[][] x, double[][] y, String[] traceLabels, String xLabel, String yLabel) {
        ScatterTrace[] traces = new ScatterTrace[x.length];
        for (int i = 0; i < traces.length; i++) {
            traces[i] =
                    ScatterTrace.builder(x[i], y[i])
                            .mode(ScatterTrace.Mode.LINE)
                            .name(traceLabels[i])
                            .build();
        }
        Layout layout =
                Layout.builder()
                        .showLegend(true)
                        .xAxis(Axis.builder().title(xLabel).build())
                        .yAxis(Axis.builder().title(yLabel).build())
                        .build();
        return new Figure(layout, traces);
    }
}
