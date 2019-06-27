package com.task.expendituretracker.alerts;

import android.app.AlertDialog;
import android.content.Context;

public class CancelableMessageDisplayerAlert {

    public static void showCancelableMessageDisplayerAlert(Context context,String title,String msg) {
        AlertDialog.Builder builder=new AlertDialog.Builder(context).setTitle(title).setMessage(msg).setCancelable(true);
        AlertDialog dialog=builder.create();
        dialog.show();
    }
}
