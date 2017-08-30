package com.voice.decibelmeter;

/**
 * 시간별 보정된 dBA 및 위험 수치 데이터를 담을 클래스
 */
public class SoundLevelItem {
    int id;
    long time;
    double dBA;
    double level;

    public SoundLevelItem(int id, long time, double dBA, double level) {
        this.id = id;
        this.time = time;
        this.dBA = dBA;
        this.level = level;
    }

    /**
     * Getter / Setter
     * @return 리턴할 값
     */
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public double getdBA() {
        return dBA;
    }

    public void setdBA(double dBA) {
        this.dBA = dBA;
    }

    public double getLevel() {
        return level;
    }

    public void setLevel(double level) {
        this.level = level;
    }
}
