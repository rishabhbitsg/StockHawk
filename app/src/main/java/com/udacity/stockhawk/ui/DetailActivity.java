package com.udacity.stockhawk.ui;


import android.content.Intent;
import android.database.Cursor;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.IFillFormatter;
import com.github.mikephil.charting.formatter.LargeValueFormatter;
import com.github.mikephil.charting.interfaces.dataprovider.LineDataProvider;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.EntryXComparator;
import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

import static android.R.attr.id;
import static android.R.attr.y;


public class DetailActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{
    public static final String STOCK_LABEL = "label";
    @BindView(R.id.chart)
    LineChart mLineChart;
    @BindView(R.id.label)
    TextView mLabelView;
    @BindView(R.id.date)
    TextView mDateView;
    @BindView(R.id.price)
    TextView mPriceView;
    @BindView(R.id.percent_change)
    TextView mPercentChangeView;
    @BindView(R.id.value_change)
    TextView mValueChangeView;
    private String mLabel;

    private final int LOADER_ID = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        ButterKnife.bind(this);

        Intent intent = getIntent();

        if (intent.hasExtra(STOCK_LABEL)) {
            mLabel = intent.getStringExtra(STOCK_LABEL);
        } else {
            mLabel = intent.getData().getLastPathSegment();
        }

        getLoaderManager().initLoader(LOADER_ID, null, this);

    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this,
                Contract.Quote.makeUriForStock(mLabel),
                Contract.Quote.QUOTE_COLUMNS,
                null, null, Contract.Quote.COLUMN_SYMBOL);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Timber.d("Cursor size: " + data.getCount());
        List<Entry> entries = new ArrayList<Entry>();
        if (data.moveToFirst()) {

            mLabelView.setText(mLabel);
            Calendar calendar = Calendar.getInstance();
            mDateView.setText(calendar.get(Calendar.DAY_OF_MONTH) + "-" + calendar.get(Calendar.MONTH) + " "
                + calendar.get(Calendar.HOUR_OF_DAY) + ":" + calendar.get(Calendar.MINUTE));
            mPriceView.setText("$" + data.getString(Contract.Quote.POSITION_PRICE));

            float percentChange = data.getFloat(Contract.Quote.POSITION_PERCENTAGE_CHANGE);
            float absChange = data.getFloat(Contract.Quote.POSITION_ABSOLUTE_CHANGE);

            if (absChange <= 0) {
                mPercentChangeView.setText("- " + Math.abs(percentChange) + "%");
                mValueChangeView.setText(("- $") + Math.abs(absChange));
                mPercentChangeView.setTextColor(getResources().getColor(R.color.material_red_700));
                mValueChangeView.setTextColor(getResources().getColor(R.color.material_red_700));
            } else {
                mPercentChangeView.setText("+ " + Math.abs(percentChange) + "%");
                mValueChangeView.setText(("+ $") + Math.abs(absChange));
                mPercentChangeView.setTextColor(getResources().getColor(R.color.material_green_700));
                mValueChangeView.setTextColor(getResources().getColor(R.color.material_green_700));
            }




            String[] history = data.getString(Contract.Quote.POSITION_HISTORY).split("\n");
            String[] dataPair;
            for(int i = 0; i < history.length; i += 4) {
                // turn your data into Entry objects
                dataPair = history[i].split(",");
                Timber.d(dataPair[0] + " " + dataPair[1]);
                entries.add(new Entry(Long.parseLong(dataPair[0]), Float.parseFloat(dataPair[1])));
//                if (i == 10) break;
            }
            Collections.sort(entries, new EntryXComparator());



            LineDataSet dataSet = new LineDataSet(entries, mLabel);
            dataSet.setFillDrawable(getResources().getDrawable(R.drawable.gradient_drawable));

            dataSet.setColors(getResources().getColor(R.color.gradient_start), getResources().getColor(R.color.gradient_end));
            dataSet.setCircleColors(getResources().getColor(R.color.gradient_start), getResources().getColor(R.color.gradient_end));
            dataSet.setCircleRadius(4);
            dataSet.setCircleHoleRadius(2);
            dataSet.setDrawHighlightIndicators(false);
            dataSet.setDrawFilled(true);
            dataSet.setDrawValues(false);
            dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);

            LineData lineData = new LineData(dataSet);

            YAxis yAxis = mLineChart.getAxisLeft();
            XAxis xAxis = mLineChart.getXAxis();

            xAxis.setDrawLabels(false);
            xAxis.setDrawGridLines(false);
            xAxis.setPosition(XAxis.XAxisPosition.BOTH_SIDED);
//            mLineChart.getAxisLeft().setAxisMinimum(0f);
//            mLineChart.getAxisLeft().setAxisMaximum(150f);
            mLineChart.getAxisRight().setDrawLabels(false);
            mLineChart.getAxisRight().setDrawGridLines(false);

            yAxis.setTextColor(getResources().getColor(android.R.color.white));

            mLineChart.setData(lineData);
            Description description = new Description();
            description.setEnabled(false);
            mLineChart.getLegend().setEnabled(false);
            mLineChart.setDescription(description);
            mLineChart.invalidate();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mLineChart.setData(null);
        mLineChart.invalidate();
    }
}
