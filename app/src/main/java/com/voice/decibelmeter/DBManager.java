package com.voice.decibelmeter;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;

/**
 * 측정한 데시벨 정보를 저장하는 SQLite DB의 관리 클래스
 */
public class DBManager extends SQLiteOpenHelper {

    public DBManager(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    /**
     * 테이블을 생성하는 메서드
     * @param db 사용할 DB 객체
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE datas (_id INTEGER PRIMARY KEY AUTOINCREMENT, time INTEGER, duration INTEGER, value FLOAT);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    /**
     * SQL Insert 쿼리를 처리하는 메서드
     * @param _query 쿼리할 쿼리 문
     */
    public void insert(String _query) {
        SQLiteDatabase db = getWritableDatabase(); // 쿼리할 DB 불러오기
        db.execSQL(_query); // 쿼리 실행
        db.close(); // DB 닫기
    }

    /**
     * 테이블의 모든 아이템을 가져오는 메서드
     * @return Item 데이터 타입으로 이루어진 테이블의 모든 값들
     */
    ArrayList<Item> getItems() {
        ArrayList<Item> items = new ArrayList<>(); // 데이터를 담을 ArrayList 선언
        SQLiteDatabase db = getWritableDatabase(); // 데이터를 가져올 DB 불러오기

        Cursor cursor = db.rawQuery("SELECT * FROM datas", null); // datas 테이블에서 모든 데이터 가져옴
        while(cursor.moveToNext()) { // 모든 데이터의 끝까지
            int id = cursor.getInt(0); // 1번 row의 id 가져오기
            long time = cursor.getLong(1); // 2번 row의 time 가져오기
            int duration = cursor.getInt(2); // 3번 row의 duration 가져오기
            double value = cursor.getDouble(3); // 4번 row의 dB Value 가져오기
            Log.d("DBManager", "time:" + time + ", value:" + value + ", duration: " + duration);
            items.add(new Item(id, time, value, duration)); // ArrayList에 아이템 추가
        }
        cursor.close(); // 커서 닫기

        return items;
    }

    /**
     * 테이블에 값을 추가하는 메서드
     * @param value 추가할 값
     * @param duration 소음 측정 텀
     */
    void addItem(double value, long duration) {
        long time = System.currentTimeMillis(); // 테이블에 추가 당시의 현재 시간
        insert("INSERT INTO datas(time, duration, value) VALUES(" + time + ", " + duration + ", " + value + ");"); // 테이블에 값 추가
    }

    /**
     * 테이블의 모든 값을 삭제하는 메서드
     */
    void removeAll() {
        getWritableDatabase().execSQL("DELETE FROM datas");
    }
}
