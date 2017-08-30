package com.voice.decibelmeter;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;

/**
 * 보정된 dBA 및 시간별 위험 수치를 저장하는 SQLite DB의 관리 클래스
 */
public class SoundLevelDBManager extends SQLiteOpenHelper {
    public SoundLevelDBManager(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    /**
     * 테이블을 생성하는 메서드
     * @param sqLiteDatabase 사용할 DB 객체
     */
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("CREATE TABLE soundLevel (_id INTEGER PRIMARY KEY AUTOINCREMENT, time INTEGER, dBA FLOAT, level FLOAT);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

    /**
     * SQL Insert 쿼리를 처리하는 메서드
     * @param _query 쿼리할 쿼리 문
     */
    public void insert(String _query) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL(_query);
        db.close();
    }

    /**
     * 테이블의 모든 아이템을 가져오는 메서드
     * @return SoundLevel 데이터 타입으로 이루어진 테이블의 모든 값들
     */
    ArrayList<SoundLevelItem> getItems() {
        ArrayList<SoundLevelItem> items = new ArrayList<>(); // 데이터를 담을 ArrayList 선언
        SQLiteDatabase db = getWritableDatabase(); // 데이터를 가져올 DB 불러오기


        Cursor cursor = db.rawQuery("SELECT * FROM soundLevel", null); // soundLevel 테이블에서 모든 데이터 가져옴
        while(cursor.moveToNext()) { // 모든 데이터의 끝까지
            int id = cursor.getInt(0); // 1번 row의 id 가져오기
            long time = cursor.getLong(1); // 2번 row의 time 가져오기
            double dBA = cursor.getDouble(2); // 3번 row의 dBA 값 가져오기
            double level = cursor.getDouble(3); // 4번 row의 위험 수치 가져오기
            Log.d("DBManager", "time:" + time + ", value:" + level);
            items.add(new SoundLevelItem(id, time, dBA, level)); // ArrayList에 아이템 추가
        }
        cursor.close(); // 커서 닫기
        return items;
    }

    /**
     * 테이블에 값을 추가하는 메서드
     * @param dBA 추가할 dbA 값
     * @param level 추가할 위험 수치
     */
    void addItem(double dBA, double level) {
        long time = System.currentTimeMillis(); // 테이블에 추가 당시의 현재 시간
        insert("INSERT INTO soundLevel(time, dBA, level) VALUES(" + time + ", " + dBA + ", " + level + ");"); // 테이블에 값 추가
    }

    /**
     * 테이블의 모든 값을 삭제하는 메서드
     */
    void removeAll() {
        getWritableDatabase().execSQL("DELETE FROM soundLevel");
    }
}
