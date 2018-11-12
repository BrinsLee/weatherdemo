package com.brins.weatherdemo.MyAdapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.brins.weatherdemo.R;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.MyViewHolder> {

    private Context mcontext;
    private List<DataBean> list=new ArrayList<>();

    public RecyclerViewAdapter(Context mcontext,List<DataBean> list) {
        this.mcontext = mcontext;
        this.list=list;

    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(mcontext).inflate(R.layout.detial,parent,false);
        MyViewHolder myViewHolder=new MyViewHolder(view);
        return myViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        DataBean dataBean=list.get(position);
        holder.iv.setImageResource(dataBean.getImageId());
        holder.tv_title.setText(dataBean.getTitle());
        holder.tv_data.setText(dataBean.getData());


    }

    @Override
    public int getItemCount() {
        return 3;
    }

    static  class MyViewHolder extends RecyclerView.ViewHolder{

        private ImageView iv;
        private TextView tv_title;
        private TextView tv_data;

        public MyViewHolder(View itemView) {
            super(itemView);
            iv=itemView.findViewById(R.id.icon);
            tv_title=itemView.findViewById(R.id.detail_name);
            tv_data=itemView.findViewById(R.id.detail_data);
        }
    }
}
