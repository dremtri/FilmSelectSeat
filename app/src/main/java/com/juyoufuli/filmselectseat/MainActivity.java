package com.juyoufuli.filmselectseat;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private String[] textDefault = {"标签", "标签", "标签", "标签", "标签", "标签", "标签", "标签", "标签", "撒上", "撒上"
            , "我是", "标签", "标签", "标签", "标签"};

    private int[][] seatList;
    private SelectSeatView searchSeat;
    private TextView tvResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        searchSeat = findViewById(R.id.search_seat);
        tvResult = findViewById(R.id.tv_result);

//外层数组，这里是，默认座位状态。0等于空白位置；1等于未选择座位；2等于已经选择座位
        seatList = new int[10][];
        for (int i = 0; i < 10; i++) {
            int[] indes = new int[14];
            for (int x = 0; x < 14; x++) {
                if (i == 4) {
                    if (x < 3 || x > 9) {
                        indes[x] = 0;
                    } else if (x == 6) {
                        indes[x] = 2;
                    } else {
                        indes[x] = 1;
                    }
                } else {
                    indes[x] = 1;
                }
            }
            seatList[i] = indes;
        }
        searchSeat.setSeatList(seatList);

        searchSeat.setChildSelectListener(new ChildSelectListener() {
            @Override
            public void onChildSelect(List<SelectRectBean> stringList) {
                StringBuffer stringBuffer = new StringBuffer();

                for (int i = 0; i < stringList.size(); i++) {
                    SelectRectBean selectRectBean = stringList.get(i);

                    stringBuffer.append(selectRectBean.getRow() + "排 ");
                    stringBuffer.append(selectRectBean.getColumn() + "列\n");
                }

                tvResult.setText(stringBuffer.toString());
            }


        });

    }
}
