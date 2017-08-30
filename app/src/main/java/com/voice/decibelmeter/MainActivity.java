package com.voice.decibelmeter;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.View;
import android.widget.ToggleButton;

import java.util.ArrayList;

/**
 * 측정된 소음을 확인하고, 소음 측정 작업을 시작하는 MainActivity
 */

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    ToggleButton toggleButton;
    DBManager dbManager;
    ArrayList<Item> items;
    FixedRecyclerView recyclerView;
    FixedRecyclerView.Adapter mAdapter;
    FixedRecyclerView.LayoutManager mLayoutManager;
    BroadcastReceiver mReceiver;
    IntentFilter intentFilter;
    boolean isEnabled = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbManager = new DBManager(this, "values.db", null, 1); // 소음 측정 데이터들이 저장된 values DB 열기
        items = dbManager.getItems(); // values DB에서 모든 아이템 가져오기

        recyclerView = (FixedRecyclerView) findViewById(R.id.recyclerView); // XML Layout에서 recyclerView를 찾아옴
        recyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this); // RecyclerView의 레이아웃 매니저 정의 (LinearLayout 사용)
        recyclerView.setLayoutManager(mLayoutManager); // recyclerView에 mLayoutManager 연결
        mAdapter = new RecyclerViewAdapter(this, items); // RecyclerViewAdapter 객체 정의
        recyclerView.setAdapter(mAdapter); // recyclerView에 Adapter 연결

        toggleButton = (ToggleButton) findViewById(R.id.changeRecordStatusButton);
        toggleButton.setOnClickListener(this);
        findViewById(R.id.enterCalibrationButton).setOnClickListener(this);
        isEnabled = getSharedPreferences("DecibelMeter", 0).getBoolean("isEnabled", false); // 녹음중인지 여부를 가져옴

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) { // Android Version이 6.0 이상일 때
            if(checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) // 권한이 없을 때
                requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, 8888); //권한 요청
            else { // 권한이 있을 때
                toggleButton.setEnabled(true); // 녹음 버튼 활성화
            }
        }
        if(isEnabled) { // 녹음중이면
            toggleButton.setChecked(true); // 토글버튼을 녹음중 모드로 변경
        }
        intentFilter = new IntentFilter(); // 녹음 서비스에서 데이터를 받아올 인텐트필터 정의
        intentFilter.addAction("com.voice.decibelmeter.SEND_BROAD_CAST"); // 인텐트필터에 브로드캐스트 수신 정의

        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) { // 서비스에서 데이터 수집이 완료되었다는 브로드캐스트를 받았을 때
                items.clear();
                for(Item i : dbManager.getItems())
                    items.add(i);
                mAdapter.notifyDataSetChanged(); // RecyclerView에 아이템들 모두 지운 후 새로 받음
            }
        };
        registerReceiver(mReceiver, intentFilter); // 브로드캐스트 리시버 등록

        findViewById(R.id.enterStatisticsButton).setOnClickListener(this);
    }

    /**
     * 액티비티가 중지되었을 때 할 일을 정의하는 메서드
     */
    @Override
    protected void onPause() {
        super.onPause();
        if(isEnabled) // 녹음 상태일 때
            unregisterReceiver(mReceiver); // 리시버를 해제(액티비티가 중지된 상태에서는 데이터를 받아서 실시간으로 새로고침 하지 않아도 되므로)
    }

    /**
     * 액티비티가 다시 시작되었을 때 할 일을 정의하는 메서드
     */
    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mReceiver, intentFilter); // 인텐트필터 재등록
        items.clear();
        for(Item i : dbManager.getItems())
            items.add(i); // RecyclerView에 아이템들 모두 지운 후 새로 받음
        mAdapter.notifyDataSetChanged();
    }

    /**
     * 데이터 수집 서비스를 시작하는 메서드
     */
    public void scheduleAlarm() {
        Intent intent = new Intent(getApplicationContext(), RecordService.class); // 데이터 수집 서비스를 가리키는 인텐트
        SharedPreferences.Editor prefs = getSharedPreferences("DecibelMeter", 0).edit();
        prefs.putBoolean("isEnabled", true); // 녹음 시작 여부를 참으로 변경
        prefs.putLong("period", 15 * 1000); // 녹음 간격을 15초로 정해줌
        isEnabled = true;
        prefs.apply();
        startService(intent); // 서비스 시작
        registerReceiver(mReceiver, intentFilter); // 브로드캐스트 리시버 등록
    }

    /**
     * 데이터 수집 서비스를 종료하는 메서드
     */
    public void cancelAlarm() {
        SharedPreferences.Editor prefs = getSharedPreferences("DecibelMeter", 0).edit();
        prefs.putBoolean("isEnabled", false); // 녹음 시작 여부를 거짓으로 변경
        isEnabled = false;
        prefs.apply();
        Intent intent = new Intent(getApplicationContext(), RecordService.class); // 데이터 수집 서비스를 가리키는 메서드
        stopService(intent); // 서비스를 중지
        unregisterReceiver(mReceiver); // 브로드캐스트 리시버 해제
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.changeRecordStatusButton:
                Log.i("MainActivity", "toggleButton.isChecked():" + toggleButton.isChecked());
                if(toggleButton.isChecked()) {
//                    scheduleAlarm(Long.parseLong(periodText.getText().toString()) * 1000 * 60);
                    Log.i("MainActivity", "Starting decibel capture with an interval of : " + 15 * 1000 + "ms");
                    scheduleAlarm();
                } else {
                    cancelAlarm();
                }
                break;
            case R.id.enterCalibrationButton:
                if(isEnabled) {
                    cancelAlarm();
                }
                startActivity(new Intent(this, CalibrateActivity.class));
                break;
            case R.id.enterStatisticsButton:
                startActivity(new Intent(MainActivity.this, StatisticsActivity.class));
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 8888:
                if(grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    toggleButton.setEnabled(false);
                } else {
                    toggleButton.setEnabled(true);
                }
        }
    }


}
