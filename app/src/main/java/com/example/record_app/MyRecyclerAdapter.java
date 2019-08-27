package com.example.record_app;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class MyRecyclerAdapter extends RecyclerView.Adapter<MyRecyclerAdapter.MyViewHolder> {
    private Context context;
    private ArrayList<String> arrayList;

    public MyRecyclerAdapter(Context context, ArrayList<String> arrayList) {
        this.context = context;
        this.arrayList = arrayList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View inflate = LayoutInflater.from(context).inflate(R.layout.item_path, null);
        return new MyViewHolder(inflate);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.tvPath.setText(arrayList.get(position));
        holder.tvPath.setOnClickListener(
                listeners -> {
                    if (listener != null) {
                        listener.onItemClick(arrayList.get(position));
                    }
                });

    }

    @Override
    public int getItemCount() {
        return arrayList == null ? 0 : arrayList.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {

        private final TextView tvPath;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPath = itemView.findViewById(R.id.tv_path);
        }
    }

    OnItemClickListener listener;

    public void setListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    interface OnItemClickListener {
        void onItemClick(String path);
    }
}
