package com.task.expendituretracker.alerts;

import androidx.appcompat.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;

import com.task.expendituretracker.R;

public class ProgressDialog {
    public static AlertDialog getprogressDialog(Context context){
        AlertDialog dialog=new AlertDialog.Builder(context).setCancelable(false).setView(LayoutInflater.from(context).inflate(R.layout.loading_box,null,false)).create();
        return dialog;
    }
}
