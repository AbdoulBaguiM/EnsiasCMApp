package com.ocr.firebaseoc.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.RequestOptions;
import com.ocr.firebaseoc.R;
import com.ocr.firebaseoc.model.Teacher;

import java.lang.ref.WeakReference;
import java.util.LinkedList;

public class TeacherAdapter extends RecyclerView.Adapter<TeacherAdapter.TeacherHolder>{

    LinkedList<Teacher> teachers;
    private RequestManager glide;
    private Boolean isMentor;

    // 1 - Create interface for callback
    public interface Listener {
        void onClickDeleteButton(int position);
    }

    // 2 - Declaring callback
    private final Listener callback;

    public TeacherAdapter(LinkedList<Teacher> teachers, RequestManager glide, Listener callback, Boolean isMentor) {
        this.teachers = teachers;
        this.glide = glide;
        this.callback = callback;
        this.isMentor = isMentor;
    }

    @NonNull
    @Override
    public TeacherAdapter.TeacherHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user,parent,false);
        return new TeacherAdapter.TeacherHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TeacherHolder holder, int position) {
        Teacher teacher = teachers.get(position);

        holder.username.setText(teacher.getUsername());
        holder.departement.setText("Département : "+teacher.getDepartement());
        glide.load(teacher.getUrlPicture()).apply(RequestOptions.circleCropTransform()).into(holder.userPhoto);

        if(isMentor)
            holder.deleteImage.setVisibility(View.VISIBLE);
    }

    @Override
    public int getItemCount() {
        return teachers.size();
    }

    public Teacher getTeacher(int position){
        return this.teachers.get(position);
    }

    public class TeacherHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        TextView username,departement;
        ImageView userPhoto,deleteImage;

        // Weak reference to the callback
        private WeakReference<Listener> callbackWeakRef;


        public TeacherHolder(@NonNull View itemView) {
            super(itemView);
            username =  itemView.findViewById(R.id.fragment_main_item_title);
            departement =  itemView.findViewById(R.id.fragment_main_item_website);
            userPhoto = itemView.findViewById(R.id.fragment_main_item_image);
            deleteImage = itemView.findViewById(R.id.fragment_main_item_delete);

            deleteImage.setOnClickListener(this);

            callbackWeakRef = new WeakReference<Listener>(callback);
        }


        @Override
        public void onClick(View view) {
            // Fire our Listener when a click happens
            Listener callback = callbackWeakRef.get();
            if(callback != null )
                callback.onClickDeleteButton(getBindingAdapterPosition());
        }
    }
}
