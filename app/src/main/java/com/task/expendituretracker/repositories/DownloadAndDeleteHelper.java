package com.task.expendituretracker.repositories;

import android.content.Context;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.task.expendituretracker.models.Entry;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

public class DownloadAndDeleteHelper {
    Context context;

    public DownloadAndDeleteHelper(Context context) {
        this.context = context;
    }

    public void deleteEverything(){
        DatabaseReference databaseReference= FirebaseDatabase.getInstance().getReference();
        FirebaseUser firebaseUser=FirebaseAuth.getInstance().getCurrentUser();
        databaseReference.child("users").child(firebaseUser.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(context.getApplicationContext(),"Successfully deleted",Toast.LENGTH_LONG).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context.getApplicationContext(),e.getMessage(),Toast.LENGTH_LONG).show();
            }
        });
    }

    public void downloadFromOnlineDB(){

    }
}
