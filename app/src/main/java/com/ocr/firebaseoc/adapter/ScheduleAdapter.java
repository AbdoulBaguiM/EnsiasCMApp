package com.ocr.firebaseoc.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ocr.firebaseoc.R;
import com.ocr.firebaseoc.model.Schedule;

import java.lang.ref.WeakReference;
import java.util.LinkedList;

public class ScheduleAdapter extends RecyclerView.Adapter<ScheduleAdapter.ScheduleHolder> {

    private LinkedList<Schedule> schedules;

    // 1 - Create interface for callback
    public interface Listener {
        void onClickDeleteButton(int position);
    }

    // 2 - Declaring callback
    private final Listener callback;

    public ScheduleAdapter(LinkedList<Schedule> schedules, Listener callback) {
        this.schedules = schedules;
        this.callback = callback;
    }

    @NonNull
    @Override
    public ScheduleAdapter.ScheduleHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.schedule_item,parent,false);
        return new ScheduleAdapter.ScheduleHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ScheduleAdapter.ScheduleHolder holder, int position) {
        Schedule schedule = schedules.get(position);

        holder.timeSlot.setText(schedule.getTimeSlot());
        holder.lesson.setText("Cours : "+schedule.getLesson() + " ( "+schedule.getNiveau()+")");
        holder.classRoom.setText("Salle : "+schedule.getRoom());
    }

    @Override
    public int getItemCount() {
        return schedules.size();
    }

    public Schedule getSchedule(int position){
        return this.schedules.get(position);
    }

    public class ScheduleHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        TextView timeSlot,lesson,classRoom;
        ImageView deleteImage;

        // Weak reference to our Callback
        private WeakReference<ScheduleAdapter.Listener> callbackWeakRef;

        public ScheduleHolder(@NonNull View itemView) {
            super(itemView);
            timeSlot = itemView.findViewById(R.id.fragment_main_item_time_slot);
            lesson = itemView.findViewById(R.id.fragment_main_item_lesson);
            classRoom = itemView.findViewById(R.id.fragment_main_item_room);
            deleteImage = itemView.findViewById(R.id.fragment_main_item_delete);

            deleteImage.setOnClickListener(this);
            callbackWeakRef = new WeakReference<ScheduleAdapter.Listener>(callback);
        }

        @Override
        public void onClick(View view) {
            // When a click happens, we fire our listener.
            ScheduleAdapter.Listener callback = callbackWeakRef.get();
            if (callback != null) callback.onClickDeleteButton(getBindingAdapterPosition());
        }
    }
}
