package com.voice.decibelmeter;

import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

/**
 * 수집한 데이터의 통계를 보여주는 StatisticsActivity
 */

public class StatisticsActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    RecyclerView.LayoutManager mLayoutManager;
    SoundLevelRecyclerViewAdapter mAdapter;
    SoundLevelDBManager soundLevelDBManager;
    DBManager manager;
    ArrayList<SoundLevelItem> items;
    double mostLoudDecibel = -1;
    long mostLoudDecibelTime = -1;
    double mostLoudLevel = -1;
    long mostLoudLevelTime = -1;
    int count = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);
        soundLevelDBManager = new SoundLevelDBManager(this, "soundLevel.db", null, 1); // 통계 데이터가 들어있는 soundLevel DB 열기
        manager = new DBManager(this, "values.db", null, 1); // 데시벨 데이터가 들어있는 values DB 열기
        items = soundLevelDBManager.getItems(); // 모든 통계 데이터 가져오기
        for(SoundLevelItem item : items) {
            if(item.getdBA() > mostLoudDecibel) { // 가장 높은 데시벨 찾기
                mostLoudDecibel = item.getdBA();
                mostLoudDecibelTime = item.getTime();
            }
            if(item.getLevel() > mostLoudLevel) { // 가장 높은 위험 수치 찾기
                mostLoudLevel = item.getLevel();
                mostLoudLevelTime = item.getTime();
            }
            if(item.getLevel() >= 1) // 위험 수치가 1을 넘을 경우
                count++; // 카운트 1 증가
        }
        recyclerView = (FixedRecyclerView) findViewById(R.id.soundLevelRecyclerView); // XML Layout에서 recyclerView를 찾아옴
        recyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this); // RecyclerView의 레이아웃 매니저 정의 (LinearLayout 사용)
        recyclerView.setLayoutManager(mLayoutManager); // recyclerView에 mLayoutManager 연결
        mAdapter = new SoundLevelRecyclerViewAdapter(this, items); // RecyclerViewAdapter 객체 정의
        recyclerView.setAdapter(mAdapter); // recyclerView에 Adapter 연결
        SimpleDateFormat sdf = new SimpleDateFormat("MM월 dd일 kk시 mm분", Locale.KOREA); // 시간을 이쁘게 표시하기 위한 SimpleDateFormat 정의
        if(mostLoudLevelTime == -1L) { // 소리 데이터가 없을 때
            ((TextView) findViewById(R.id.mostLoudDecibelText)).setText("아직 파악된 수치가 없습니다");
            ((TextView) findViewById(R.id.mostLoudDecibelTimeText)).setText("");
            ((TextView) findViewById(R.id.mostLoudLevelText)).setText("아직 파악된 수치가 없습니다");
            ((TextView) findViewById(R.id.mostLoudLevelTimeText)).setText("");
            ((TextView) findViewById(R.id.exceedCountView)).setText(Integer.toString(count));
        } else { // 소리 데이터가 있을 때
            ((TextView) findViewById(R.id.mostLoudDecibelText)).setText(Double.toString(Math.round(mostLoudDecibel * 1000) / 1000.0) + "dB(A)"); // 최고 데시벨 표시
            ((TextView) findViewById(R.id.mostLoudDecibelTimeText)).setText(sdf.format(new Date(mostLoudDecibelTime))); // 최고 데시벨이 기록된 시간 표시
            ((TextView) findViewById(R.id.mostLoudLevelText)).setText(Double.toString(Math.round(mostLoudLevel * 1000) / 1000.0)); // 최고 위험 수치 표시
            ((TextView) findViewById(R.id.mostLoudLevelTimeText)).setText(sdf.format(new Date(mostLoudLevelTime))); // 최고 위험 수치가 기록된 시간 표시
            ((TextView) findViewById(R.id.exceedCountView)).setText(Integer.toString(count)); // 위험 수치가 1이 넘은 횟수 표시
        }
        findViewById(R.id.clearDBButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { // 삭제 버튼을 눌렀을 때
                new AlertDialog.Builder(StatisticsActivity.this)
                .setMessage("정말 삭제하겠습니까?").setPositiveButton("네", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) { // 삭제 다이얼로그에서 '네' 를 눌렀을 때
                                manager.removeAll();
                                soundLevelDBManager.removeAll(); // 두 DB의 모든 내용물을 삭제
                                Toast.makeText(StatisticsActivity.this, "모든 수치가 삭제되었습니다.", Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        })
                        .setNegativeButton("아니오", new DialogInterface.OnClickListener() { // 아니오를 눌렀을 때
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) { // 아무것도 하지 않고 종료

                            }
                        }).show();

            }
        });
    }
}
