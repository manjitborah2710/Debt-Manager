package com.task.expendituretracker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;


import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.task.expendituretracker.alerts.ProgressDialog;
import com.task.expendituretracker.models.Entry;
import com.task.expendituretracker.repositories.DownloadAndDeleteHelper;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class OnlineActivity extends AppCompatActivity implements View.OnClickListener{

    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;

    Button download,delete;

    final static int REQUEST_EXTERNAL_STORAGE_PERMISSIONS=2012;

    DownloadAndDeleteHelper downloadAndDeleteHelper;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_online);

        downloadAndDeleteHelper=new DownloadAndDeleteHelper(this);

        download=findViewById(R.id.download_from_online_db);
        download.setOnClickListener(this);
        delete=findViewById(R.id.delete_from_online_db);
        delete.setOnClickListener(this);

        firebaseAuth=FirebaseAuth.getInstance();
        firebaseUser=firebaseAuth.getCurrentUser();

        if(firebaseUser==null){
            AlertDialog.Builder builder=new AlertDialog.Builder(this).setCancelable(false).setTitle("Not Signed in").setMessage("You are not authorized to use these features. Please sign in.")
                    .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            finish();
                        }
                    });
            AlertDialog dialog=builder.create();
            dialog.show();
        }else{
            if(!firebaseUser.isEmailVerified()){
                AlertDialog.Builder builder=new AlertDialog.Builder(this).setCancelable(false).setTitle("Email not verified").setMessage("Please verify your email before enabling these functions.")
                        .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                finish();
                            }
                        });
                AlertDialog dialog=builder.create();
                dialog.show();
            }
        }



    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.download_from_online_db:{
                downloadFromOnlineDB();
                break;
            }
            case R.id.delete_from_online_db:{
                deleteEverything();
                break;
            }
        }
    }

    private void downloadFromOnlineDB() {
        ArrayList<String> permissions=new ArrayList<>();
        if(ContextCompat.checkSelfPermission(this,Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
            permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        if(ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
            permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if(!permissions.isEmpty()){
            ActivityCompat.requestPermissions(this,permissions.toArray(new String[permissions.size()]),REQUEST_EXTERNAL_STORAGE_PERMISSIONS);
        }
        else{
                completeDownload();
        }

    }

    private void completeDownload() {

        final AlertDialog dialog= ProgressDialog.getprogressDialog(this);

        final Font font14Bold=new Font(Font.FontFamily.HELVETICA,14,Font.BOLD);
        Font font16=new Font(Font.FontFamily.TIMES_ROMAN,16);
        final Font font18Bold=new Font(Font.FontFamily.TIMES_ROMAN,18,Font.BOLD);
        final Font font18BoldUnderline=new Font(Font.FontFamily.TIMES_ROMAN,18,Font.UNDERLINE|Font.BOLD);
        Font font20=new Font(Font.FontFamily.TIMES_ROMAN,20);
        Font font24Bold=new Font(Font.FontFamily.TIMES_ROMAN,24,Font.BOLD);

        Calendar calendar=Calendar.getInstance();
        SimpleDateFormat simpleDateFormat=new SimpleDateFormat("dd-MMM-yyy E hh:mm:ss a \n(z)");

        String date=simpleDateFormat.format(calendar.getTime());

        //headings
        final Paragraph heading1=new Paragraph("Debt Tracker Report(Online)\n",font24Bold);
        heading1.setAlignment(Paragraph.ALIGN_CENTER);

        final Paragraph heading2=new Paragraph("Report as on "+date+"\n\n\n",font20);
        heading2.setAlignment(Paragraph.ALIGN_CENTER);


        String absolutePath=Environment.getExternalStorageDirectory().getAbsolutePath();
        File directoryRoute=new File(absolutePath+"/Debt Tracker");
        if(!directoryRoute.exists()){
            if(!directoryRoute.mkdir()){
                Toast.makeText(getApplicationContext(),"Could not create directory",Toast.LENGTH_SHORT);
                return;
            }
        }
        String fileNameSuffix="-Online_Report-"+((Calendar.getInstance().getTimeInMillis()))+"-"+Calendar.getInstance().get(Calendar.DAY_OF_MONTH)+"."+(Calendar.getInstance().get(Calendar.MONTH)+1)+"."+Calendar.getInstance().get(Calendar.YEAR);
        final File actualFile=new File(directoryRoute.getAbsolutePath()+"/Debt_Tracker_Report-"+fileNameSuffix+".pdf");
        try {
            final FileOutputStream fileOutputStream = new FileOutputStream(actualFile);
            final Document document = new Document();
            PdfWriter.getInstance(document, fileOutputStream);


            firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference reference = database.getReference();

            dialog.show();
            reference.child("users").child(firebaseUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    document.open();
                    try {
                        document.add(heading1);
                        document.add(heading2);
                    } catch (DocumentException e) {
                        e.printStackTrace();
                        actualFile.delete();
                        dialog.dismiss();
                    }


                    Iterable<DataSnapshot> dataSnapshotsDateLevel = dataSnapshot.getChildren();
                    for (DataSnapshot dsDateLevel : dataSnapshotsDateLevel) {
                        String d = dsDateLevel.getKey();
                        Paragraph paragraph = new Paragraph(d , font18BoldUnderline);
                        try {
                            document.add(paragraph);
                            document.add(new Paragraph("\n\n"));
                        } catch (DocumentException e) {
                            e.printStackTrace();
                            actualFile.delete();
                            dialog.dismiss();
                        }

                        PdfPTable table = new PdfPTable(4);

                        table.addCell(new PdfPCell(new Phrase("Name",font14Bold)));
                        table.addCell(new PdfPCell(new Phrase("Purpose",font14Bold)));
                        table.addCell(new PdfPCell(new Phrase("Amount",font14Bold)));
                        table.addCell(new PdfPCell(new Phrase("Borrowed/Lent",font14Bold)));


                        Iterable<DataSnapshot> dataSnapshotsEntryLevel = dsDateLevel.getChildren();
                        for (DataSnapshot dsEntryLevel : dataSnapshotsEntryLevel) {
                            Entry entry = dsEntryLevel.getValue(Entry.class);
                            table.addCell(entry.getToOrFrom());
                            table.addCell(entry.getName());
                            table.addCell(entry.getAmount() + "");
                            if (entry.getTo() == 1) table.addCell("Borrowed");
                            else if (entry.getTo() == 0) table.addCell("Lent");
                        }
                        try {
                            document.add(table);
                            document.add(new Paragraph("\n\n"));
                        } catch (DocumentException e) {
                            e.printStackTrace();
                            actualFile.delete();
                            dialog.dismiss();
                        }
                    }
                    Toast.makeText(getApplicationContext(), "Saved to /Debt Tracker", Toast.LENGTH_SHORT).show();
                    document.close();
                    dialog.dismiss();

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(getApplicationContext(), databaseError.getMessage(), Toast.LENGTH_LONG).show();
                    actualFile.delete();
                    dialog.dismiss();
                }
            });
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
            actualFile.delete();
            dialog.dismiss();

        } catch (DocumentException e) {
            e.printStackTrace();
            actualFile.delete();
            dialog.dismiss();
        }


    }

    public void deleteEverything(){
        final AlertDialog dialog=ProgressDialog.getprogressDialog(this);
        DatabaseReference databaseReference= FirebaseDatabase.getInstance().getReference();
        FirebaseUser firebaseUser=FirebaseAuth.getInstance().getCurrentUser();
        dialog.show();
        databaseReference.child("users").child(firebaseUser.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(getApplicationContext(),"Successfully deleted",Toast.LENGTH_LONG).show();
                dialog.dismiss();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_LONG).show();
                dialog.dismiss();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode==REQUEST_EXTERNAL_STORAGE_PERMISSIONS){
            if(grantResults.length==2){
                if(grantResults[0]==PackageManager.PERMISSION_GRANTED && grantResults[1]==PackageManager.PERMISSION_GRANTED) {
                    downloadFromOnlineDB();
                }
            }
        }
    }

}
