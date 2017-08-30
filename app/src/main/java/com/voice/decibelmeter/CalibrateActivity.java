package com.voice.decibelmeter;

import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.io.IOException;

/**
 * 소음 측정의 보정을 담당하는 액티비티
 */

public class CalibrateActivity extends AppCompatActivity implements View.OnClickListener {
    MediaRecorder mRecorder;
    float amp;
    TextView dbText;
    ToggleButton toggleButton;
    Button[] buttons;
    CalibrateTask task;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calibrate);
        // 레이아웃 컴포넌트 정의 및 OnClickListener 설정
        dbText = (TextView) findViewById(R.id.calibrateDbText);

        buttons = new Button[]{
                (Button)findViewById(R.id.smallMinusButton),
                (Button)findViewById(R.id.bigMinusButton),
                (Button)findViewById(R.id.smallPlusButton),
                (Button)findViewById(R.id.bigPlusButton)
        };
        for(Button b : buttons) {
            b.setEnabled(false);
            b.setOnClickListener(this);
        }
        toggleButton = (ToggleButton) findViewById(R.id.calibrateButton);

        toggleButton.setOnClickListener(this);
        // DB에서 현재 수치를 받아옴
        amp = getSharedPreferences("DecibelMeter", 0).getFloat("amplitude", 0);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.calibrateButton:
                if(toggleButton.isChecked()) { // 보정 작업 시작되었을 때
                    startRecorder(); // 마이크 시작

                    for(Button b : buttons)
                        b.setEnabled(true); // 모든 수치 조정 버튼을 활성화

                    task = new CalibrateTask();
                    task.execute(); // dB 측정 활성화

                } else { // 보정 작업이 끝날 때
                    task.cancel(true); // dB 측정 비활성화
                    stopRecorder(); // 마이크 종료
                    for(Button b : buttons)
                        b.setEnabled(false); // 모든 수치 조정 버튼을 비활성화
                }
                break;
            case R.id.smallMinusButton:
                amp -= 0.1; // 조정 수치 0.1 감소
                break;
            case R.id.smallPlusButton:
                amp += 0.1; // 조정 수치 0.1 증가
                break;
            case R.id.bigMinusButton:
                amp -= 1; // 조정 수치 1 감소
                break;
            case R.id.bigPlusButton:
                amp += 1; // 조정 수치 1 증가
                break;
        }
        Log.i("CalibrateActivity", "Changing amplitude value to "+ amp + "...");
    }

    @Override
    protected void onPause() { // 액티비티를 종료했을 때
        super.onPause();
        getSharedPreferences("DecibelMeter", 0).edit().putFloat("amplitude", amp).apply(); // 현재 기록된 보정 수치를 저장
        stopRecorder(); // 마이크 종료
        Toast.makeText(this, "보정이 완료되었습니다. 어플리케이션 재시작을 권장합니다.", Toast.LENGTH_SHORT).show(); // 알림 문구
    }

    void startRecorder() { // 마이크 시작하는 메서드
        if(mRecorder == null) {
            mRecorder = new MediaRecorder();
            mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mRecorder.setAudioSamplingRate(44100);
            mRecorder.setAudioEncodingBitRate(96000);
            mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            mRecorder.setOutputFile("/dev/null");
            try {
                mRecorder.prepare();
                mRecorder.start();
                mRecorder.getMaxAmplitude();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    void stopRecorder() { // 마이크 종료하는 메서드
        if(mRecorder != null) {
            mRecorder.stop();
            mRecorder.release();
            mRecorder = null;
        }
    }

    protected class CalibrateTask extends AsyncTask<Void, Double, Void> { // 데시벨 측정
        private double mEMA = 0.0;
        static final private double EMA_FILTER = 0.6;
        double dB;

        /**
         * 데시벨을 계산하는 메서드
         * @param ampl 데시벨을 계산할 때 사용할 보정 수치
         * @return 계산된 데시벨
         */
        public double soundDb(double ampl){
            return  20 * Math.log10(getAmplitudeEMA() / ampl);
        }
        public double getAmplitude() {
            if (mRecorder != null)
                return  (mRecorder.getMaxAmplitude());
            else
                return 0;

        }

        /**
         * 진폭을 가져오는 메서드
         * @return 소리의 진폭
         */
        public double getAmplitudeEMA() {
            double amp =  getAmplitude();
            mEMA = EMA_FILTER * amp + (1.0 - EMA_FILTER) * mEMA;
            return mEMA;
        }

        /**
         * 데시벨을 가져와서 이를 보여주는 메서드
         * @param voids .
         * @return .
         */
        @Override
        protected Void doInBackground(Void... voids) {
            while (true) {
                dB = soundDb(10 * Math.exp(amp)); // 현재 보정 수치로 데시벨을 계산함
                publishProgress(dB); // 화면에 업데이트
                try {
                    Thread.sleep(100); // 0.1초 쉼
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        /**
         * 화면을 갱신하는 메서드
         * @param values 측정한 데시벨
         */
        @Override
        protected void onProgressUpdate(Double... values) {
            dbText.setText(Double.toString(Math.floor(values[0])) + " dB");
        }
    }
}
