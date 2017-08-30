package com.voice.decibelmeter;

/**
 * 소음 관련 데이터를 담을 클래스
 */
public class Item {
    int _id;
    long time;
    double value;
    int duration;

    public Item(int _id, long time, double value, int duration) {
        this._id = _id;
        this.time = time;
        this.value = value;
        this.duration = duration;
    }

    /**
     * Getter / Setter
     * @return 리턴할 값
     */
    public int get_id() {
        return _id;
    }

    public void set_id(int _id) {
        this._id = _id;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }
}
