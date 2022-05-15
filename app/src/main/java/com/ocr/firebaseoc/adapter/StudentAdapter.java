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
import com.ocr.firebaseoc.model.Student;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class StudentAdapter extends RecyclerView.Adapter<StudentAdapter.StudentHolder> {

    ArrayList<Student> students;
    private RequestManager glide;
    private Boolean isMentor;

    // 1 - Create interface for callback
    public interface Listener {
        void onClickDeleteButton(int position);
    }

    // 2 - Declaring callback
    private final Listener callback;

    public StudentAdapter(ArrayList<Student> students, RequestManager glide, Listener callback,Boolean isMentor) {
        this.students = students;
        this.glide = glide;
        this.callback = callback;
        this.isMentor = isMentor;
    }

    @NonNull
    @Override
    public StudentAdapter.StudentHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user,parent,false);
        return new StudentHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StudentHolder holder, int position) {
        Student student = students.get(position);

        holder.username.setText(student.getUsername());
        holder.cne.setText("CNE : "+student.getCne());
        glide.load(student.getUrlPicture()).apply(RequestOptions.circleCropTransform()).into(holder.userPhoto);

        if(isMentor)
            holder.deleteImage.setVisibility(View.VISIBLE);

    }

    @Override
    public int getItemCount() {
        return students.size();
    }

    public Student getStudent(int position){
        return this.students.get(position);
    }

    public class StudentHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView username,cne;
        ImageView userPhoto,deleteImage;

        // Weak reference to our Callback
        private WeakReference<Listener> callbackWeakRef;

        public StudentHolder(@NonNull View itemView) {
            super(itemView);
            username =  itemView.findViewById(R.id.fragment_main_item_title);
            cne =  itemView.findViewById(R.id.fragment_main_item_website);
            userPhoto = itemView.findViewById(R.id.fragment_main_item_image);
            deleteImage = itemView.findViewById(R.id.fragment_main_item_delete);

            deleteImage.setOnClickListener(this);
            callbackWeakRef = new WeakReference<Listener>(callback);
        }

        @Override
        public void onClick(View view) {
            // When a click happens, we fire our listener.
            Listener callback = callbackWeakRef.get();
            if (callback != null) callback.onClickDeleteButton(getBindingAdapterPosition());
        }
    }
}
