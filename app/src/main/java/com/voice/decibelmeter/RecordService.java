package com.voice.decibelmeter;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import org.jtransforms.fft.DoubleFFT_1D;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * 소리를 녹음하여 그 크기를 알아내고 위험 수치를 계산하는 서비스
 */
public class RecordService extends Service {
    float k;
    DBManager manager;
    SoundLevelDBManager soundLevelDBManager;
    long duration;
    RecordTask recordTask;
    AudioRecord audioRecord;

    private final int CHANNELCONFIG = AudioFormat.CHANNEL_CONFIGURATION_MONO;
    private final int AUDIOENCODING = AudioFormat.ENCODING_PCM_16BIT;
    int sampleRate = 44100;

    int bufferSize = (AudioRecord.getMinBufferSize(sampleRate, CHANNELCONFIG, AUDIOENCODING) <= 14112) ? 14112 : AudioRecord.getMinBufferSize(sampleRate, CHANNELCONFIG, AUDIOENCODING);
    int numberOfPoints = bufferSize;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        manager = new DBManager(this, "values.db", null, 1); // 소음 측정 데이터들이 저장될 values DB 열기
        soundLevelDBManager = new SoundLevelDBManager(this, "soundLevel.db", null, 1); // 소음 관련 통계치들이 저장될 soundLevel DB 열기
        k = getSharedPreferences("DecibelMeter", 0).getFloat("amplitude", 0); // 현재 저장된 보정 수치 가져오기
        duration = getSharedPreferences("DecibelMeter", 0).getLong("period", -1); // 녹음 시간 간격 가져오기
        recordTask = new RecordTask();
        recordTask.execute(); // 녹음 쓰레드 시작
        return START_NOT_STICKY;
    }

    /**
     * 서비스가 종료될 때의 행동을 정의하는 메서드
     */
    @Override
    public void onDestroy() {
        recordTask.stop(); // 녹음 쓰레드 종료
        recordTask.cancel(true); // 녹음 쓰레드 종료
        super.onDestroy();
    }

    /**
     * 쓰레드로 동작하며 소리를 녹음하는 클래스
     */

    protected class RecordTask extends AsyncTask<Void, Void, Void> {
        ArrayList<MyEntry<Double, Double>> data = new ArrayList<>();
        HashMap<Integer, Integer> accumulated = new HashMap<>();
        short[] audio;
        double[] fft;

        int[] indexes = {2, 4, 8, 16, 32, 64, 128, 256, 512};
        double[] aWeight = {-39,4, -26.2, -16.1, -8.6, -3.2, 0, 1.2, 1, -1.1};

        boolean isRunning = true;

        void stop() { isRunning = false; }

        /**
         * 소음 위험 수치를 계산하고 DB에 저장하는 메서드
         * @param values .
         */
        @Override
        protected void onProgressUpdate(Void... values) {

            double[] aWeighted = new double[9];
            for(int i = 0; i < 9; i++)
                aWeighted[i] = 20 * Math.log10(data.get(i).getValue() / (10 * Math.exp(k))) + aWeight[i]; // 측정한 특정 Octave Band의 값에 A factor를 더해 보정
            Log.i("RecordService", "A-Weighted dB : " + Arrays.toString(aWeighted));
            double dBA = 0;
            for(double a : aWeighted)
                dBA += Math.pow(10, a/10);
            dBA = 10 * Math.log10(dBA); // 떨어져 있는 데시벨들을 모두 더함
            manager.addItem(dBA, duration);
            Log.i("RecordService", "Got dBA : " + dBA);

            int round = (int) Math.round(dBA); // 데시벨을 5 단위로 반올림
            if(accumulated.keySet().contains(round)) { // 해당 데시벨의 소리를 들은 적이 있으면
                accumulated.put(round, accumulated.get(round) + 1); // 해당 데시벨 소리 들은 횟수 1 증가
            } else {
                accumulated.put(round, 1); // 해시맵에 새로운 값 정의
            }

            double sum = 0;
            for(Map.Entry<Integer, Integer> entry: accumulated.entrySet()) { // 해시맵의 모든 K-V에 대하여
                double a = (entry.getValue() / 240.0) / (8.0 / Math.pow(2, (entry.getKey() - 90) / 5.0)); // 데시벨과 들은 횟수를 이용하여 위험치 계산
                Log.i("RecordService", "Value of " + entry.getKey() + "dBA with " + entry.getValue() + " times: " + a);
                sum += a;
            }
            Log.i("RecordService", "Total sum: " +sum);
            if(sum >= 1) { // 위험 수치가 1을 넘으면
                NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                PendingIntent pendingIntent = PendingIntent.getActivity(RecordService.this, 0, new Intent(RecordService.this, MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);

                Notification.Builder mBuilder = new Notification.Builder(RecordService.this);
                mBuilder.setSmallIcon(R.mipmap.ic_launcher);
                mBuilder.setTicker("소리 알림");
                mBuilder.setWhen(System.currentTimeMillis());
                mBuilder.setContentTitle("소음 위험 알림");
                mBuilder.setContentText("당신의 귀 건강이 위험합니다! 즉시 조용한 곳으로 자리를 옮기세요!");
                mBuilder.setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE);
                mBuilder.setContentIntent(pendingIntent);
                mBuilder.setAutoCancel(true);

                mBuilder.setPriority(NotificationCompat.PRIORITY_MAX);

                nm.notify(111, mBuilder.build());
                // 위험하다는 알림을 사용자에게 표시
            }

            soundLevelDBManager.addItem(dBA, sum); // 통계 DB에 측정값을 추가


            sendBroadcast(new Intent("com.voice.decibelmeter.SEND_BROAD_CAST")); // 메인액티비티에 값이 추가되었음을 알림
        }

        /**
         * 백그라운드에서 녹음 작업을 담당하는 메서드
         * @param voids .
         * @return .
         */
        @Override
        protected Void doInBackground(Void... voids) {
            while(isRunning) {
                audio = new short[numberOfPoints]; // 녹음 값이 저장될 배열
                fft = new double[numberOfPoints * 2]; // Fourier Transform이 이루어진 real-imagine 한 쌍이 저장될 배열
                audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, sampleRate, CHANNELCONFIG, AUDIOENCODING, bufferSize); // 녹음기 정의
                data.clear();
                audioRecord.startRecording(); // 녹음 시작
                Log.i("RecordTask", "Starting recording...");
                try {
                    Thread.sleep(1000); // 1초간 녹음
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Log.i("RecordTask", "Waiting...");

                audioRecord.read(audio, 0, bufferSize); // 녹음기에서 데이터 읽어오기
                audioRecord.stop(); // 녹음기 종료
                for(int i = 0; i < audio.length; i++) {
                    fft[i] = audio[i]; // FFT를 위해 데이터 복사
                }

                DoubleFFT_1D fourier = new DoubleFFT_1D(numberOfPoints);
                fourier.complexForward(fft); // FFT 계산

                for(int i : indexes) {
                    double re = fft[2 * (5 * i)]; // 실수값 불러오기
                    double im = fft[2 * (5 * i) + 1];  // 허수값 불러오기
                    double magnitude = Math.sqrt(re * re + im * im); // 진폭 계산
                    Log.i("RecordTask", "i:" +  5 * i + ", Frequency: " + 44100.0 / numberOfPoints * (5 * i) + ", numberOfPoints:" + numberOfPoints);
                    data.add(new MyEntry<>((44100.0 / numberOfPoints * (5 * i)), magnitude)); // 주파수와 진폭 추가
                }
                publishProgress(); // DB에 추가 및 수치를 계산하는 onProgressUpdate 호출
                try {
                    Thread.sleep(duration - 1000); // 지정한 간격 - 1초간 쉬기 ( 녹음 작업에 1초를 소모했으므로 1초간 덜 쉼)
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }
    }
}