package com.blautic.pikkuacademyfull.view;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.StringRes;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import com.blautic.pikkuacademyfull.R;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import timber.log.Timber;

public class SensorLinealChart extends ConstraintLayout implements View.OnClickListener {

    private TextView labelSensorName;
    private LineChart chart;
    private ImageButton buttonX;
    private ImageButton buttonY;
    private ImageButton buttonZ;
    private Axis enableAxis = Axis.X;
    private float scale = 4;

    public SensorLinealChart(Context context) {
        super(context);
        init(null, 0);
    }

    public SensorLinealChart(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public SensorLinealChart(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
        // Load attributes
        LayoutInflater.from(getContext()).inflate(R.layout.sensor_lineal_chart, this);
        labelSensorName = findViewById(R.id.label_sensor);
        chart = findViewById(R.id.chartSensor);
        buttonX = findViewById(R.id.button_x);
        buttonY = findViewById(R.id.button_y);
        buttonZ = findViewById(R.id.button_z);
        buttonX.setOnClickListener(this);
        buttonY.setOnClickListener(this);
        buttonZ.setOnClickListener(this);

        initLineChar();
     //   feedMultiple();
    }

    public void clean(){
        this.chart.clear();
        this.chart.invalidate();
    }
    public void setScale(float scale){
        this.scale = scale;
        initLineChar();
    }
    public void setLabelSensorName(String name) {
        labelSensorName.setText(name);
    }

    public void setLabelSensorName(@StringRes int id) {
        labelSensorName.setText(id);
    }

    public void addEntryLineChart(float x, float y, float z) {
        switch (enableAxis) {
            case X:
                addEntryLineChart(x);
                break;
            case Y:
                addEntryLineChart(y);
                break;
            case Z:
                addEntryLineChart(z);
                break;
        }
    }

    private void addEntryLineChart(float entry) {
        LineData data = chart.getData();
        if (data != null) {
            ILineDataSet set = data.getDataSetByIndex(0);
            if (set == null) {
                set = createSetLineChart();
                data.addDataSet(set);
                for(int i = 1; i < 130 ; i++){
                    data.addEntry(new Entry(set.getEntryCount(), 0), 0);
                }

            }
            data.addEntry(new Entry(set.getEntryCount(), entry), 0);
            data.notifyDataChanged();
            // let the chart know it's data has changed
            chart.notifyDataSetChanged();
            // limit the number of visible entries
            chart.setVisibleXRangeMaximum(130);

            //  chart.setVisibleYRange(30,30, YAxis.AxisDependency.LEFT);
            // move to the latest entry
            chart.moveViewToX(data.getEntryCount());
        }
    }

    private void initLineChar() {
        // disable description text
        chart.getDescription().setEnabled(false);
        // enable touch gestures
        //chart.setTouchEnabled(true);
        // enable scaling and dragging
        chart.setDragEnabled(true);
        chart.setScaleEnabled(true);
        chart.setDrawGridBackground(false);
        // if disabled, scaling can be done on x- and y-axis separately
        chart.setPinchZoom(true);
        // set background color TRANSPARENT
        chart.setBackgroundColor(Color.TRANSPARENT);
        LineData data = new LineData();
        data.setValueTextColor(Color.WHITE);
        // add empty data
        chart.setData(data);
        // disable legend (only possible after setting data)
        chart.getLegend().setEnabled(false);
        chart.getXAxis().setEnabled(false);
        YAxis leftAxis = chart.getAxisLeft();
        //  leftAxis.setTypeface(tfLight);
        leftAxis.setTextColor(Color.WHITE);
        leftAxis.setAxisMaximum(scale);
        leftAxis.setAxisMinimum(-(scale));
        leftAxis.setDrawGridLines(true);
        //disable axis right
        chart.getAxisRight().setEnabled(false);
    }


    private LineDataSet createSetLineChart() {
        LineDataSet set = new LineDataSet(null, "");
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setLineWidth(2f);
        set.setHighlightEnabled(false);
        set.setColor(ContextCompat.getColor(getContext(), R.color.pikku_green));
        set.setDrawValues(false);
        set.setDrawCircles(false);
        return set;
    }

    @Override
    public void onClick(View v) {
        //chart.clearValues();
        buttonX.setImageResource(R.drawable.ic_x_off);
        buttonY.setImageResource(R.drawable.ic_y_off);
        buttonZ.setImageResource(R.drawable.ic_z_off);
        switch (v.getId()) {
            case R.id.button_x: {
                enableAxis = Axis.X;
                buttonX.setImageResource(R.drawable.ic_x_on);
                break;
            }
            case R.id.button_y: {
                enableAxis = Axis.Y;
                buttonY.setImageResource(R.drawable.ic_y_on);
                break;
            }
            case R.id.button_z: {
                enableAxis = Axis.Z;
                buttonZ.setImageResource(R.drawable.ic_z_on);
                break;
            }
        }
    }

    enum Axis {
        X, Y, Z
    }
}