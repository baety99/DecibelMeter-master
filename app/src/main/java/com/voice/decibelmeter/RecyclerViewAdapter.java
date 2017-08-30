/** RecyclerViewAdapter
 * MainActivity의 RecyclerView에서 사용될 RecyclerViewAdapter
 * @author Kyujin Cho
 * @version 1.0
 * @see com.voice.decibelmeter.MainActivity
 */

package com.voice.decibelmeter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.RecyclerViewHolder> {
    Context mContext;
    ArrayList<Item> mItem;
    LayoutInflater mInflater;

    /**
     * 클래스 생성자. Activity의 Context와 RecyclerView에서 사용될 데이터 집합체를 전달받음
     * @param mContext Activity Context
     * @param mItem 데이터 셋
     */
    public RecyclerViewAdapter(Context mContext, ArrayList<Item> mItem) {
        this.mContext = mContext; // Context를 전달받아 클래스 전역변수에 저장
        this.mItem = mItem; // Data Set을 전달받아 클래스 전역변수에 저장
        mInflater = LayoutInflater.from(mContext); // 전달받은 context로 LayoutInflater 생성
    }

    /**
     * onCreateViewHoler를 통해 inflate된 viewholder의 UI Component들에 text를 설정하고, onClickListener를 달아주는 메소드
     * @param holder UI Component를 수정할 ViewHolder
     * @param position 해당 View가 List의 몇 번째 위치에 있는지 알려주는 위치 변수
     */
    @Override
    public void onBindViewHolder(RecyclerViewHolder holder, int position) {
        SimpleDateFormat sdf = new SimpleDateFormat("MM월 dd일 kk시 mm분", Locale.KOREA);
        holder.timeView.setText(sdf.format(new Date(mItem.get(position).getTime()))); // ViewHolder에서 항목의 이름을 보여줄 UI Component에 텍스트를 설정해줌
        holder.contentView.setText(Double.toString(Math.round(mItem.get(position).getValue() * 10000) / 10000.0) + "dB"); // ViewHolder에서 데시벨 값을 보여줄 UI Component에 텍스트를 설정해줌
        holder.durationView.setText(Integer.toString(mItem.get(position).getDuration())); // ViewHolder에서 캡쳐 시간 간격을 보여줄 UI Component에 텍스트를 설정해줌
    }

    /**
     * XML Layout을 가져와 ViewHolder 형태로 inflate 시켜서 돌려주는 메소드
     * @param parent ViewGroup
     * @param viewType ViewType
     * @return Inflate된 ViewHolder
     */
    @Override
    public RecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new RecyclerViewHolder(mInflater.inflate(R.layout.decibel_card, parent, false));
    }

    /**
     * RecyclerView에서 사용할 Data Set의 크기를 전달해주는 메소드
     * @return mItem의 크기
     */
    @Override
    public int getItemCount() {
        return mItem.size();
    }


    /**
     * RecyclerViewHolder
     * RecyclerView에서 표시될 Element의 View를 정의하는 Class
     */
    class RecyclerViewHolder extends RecyclerView.ViewHolder {
        TextView timeView;
        TextView contentView;
        TextView durationView;

        public RecyclerViewHolder(View itemView) {
            super(itemView);
            timeView = (TextView) itemView.findViewById(R.id.timeView);
            contentView = (TextView) itemView.findViewById(R.id.decibelView);
            durationView = (TextView) itemView.findViewById(R.id.durationView);

        }
    }
}
