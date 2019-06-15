package com.task.expendituretracker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.task.expendituretracker.adapters.MyRVAdapter;
import com.task.expendituretracker.models.Entry;
import com.task.expendituretracker.repositories.EntryRepository;
import com.task.expendituretracker.viewmodels.EntryViewModel;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    public FloatingActionButton fab;
    LinearLayout llForOptions,billTotal,billIndiv;
    Button save,cancel;
    RecyclerView recyclerView;
    EntryViewModel entryViewModel;
    TextView totalReceive,totalPay,totalReceive_bill,totalPay_bill;
    List<Entry> list;
    EditText payeeOrPayer,name,amt;
    RadioButton borrowedRB,lentRB;
    RadioGroup radioGroup;
    Context context;
    SearchView searchView;
    List<Entry> tempList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        billIndiv=findViewById(R.id.search_result_bill);
        billTotal=findViewById(R.id.search_result_total);
        searchView=findViewById(R.id.search);
        context=this;
        radioGroup=findViewById(R.id.radioGroup);
        payeeOrPayer=findViewById(R.id.payeeorpayer_et);
        name=findViewById(R.id.name_et);
        amt=findViewById(R.id.amt_et);
        borrowedRB=findViewById(R.id.borrowed_rb);
        lentRB=findViewById(R.id.lent_rb);
        totalPay=findViewById(R.id.totalPaid_tv);
        totalReceive=findViewById(R.id.totalReceived_tv);
        totalPay_bill=findViewById(R.id.totalPaid_tv_bill);
        totalReceive_bill=findViewById(R.id.totalReceived_tv_bill);
        fab=findViewById(R.id.fab);
        llForOptions=findViewById(R.id.llforoptions);
        save=findViewById(R.id.savebtn);
        cancel=findViewById(R.id.cancelbtn);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                llForOptions.setVisibility(View.GONE);
            }
        });

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                llForOptions.setVisibility(View.VISIBLE);
            }
        });
        recyclerView=findViewById(R.id.rv);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        list=new ArrayList<>();
        final MyRVAdapter myRVAdapter=new MyRVAdapter(this,list){
            @Override
            public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
                super.onBindViewHolder(holder, position);
                final MyRVAdapter.MyViewHolder myViewHolder=(MyRVAdapter.MyViewHolder)holder;
                myViewHolder.delete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AlertDialog dialog=entryDeleteHelperMethod();
                        dialog.setButton(AlertDialog.BUTTON_POSITIVE, "Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                entryViewModel.deleteEntry(getEntryAtPosition(position));
                                if(fromSearch){
                                    removeEntryFromListAtPosition(position);
                                    Toast.makeText(getApplicationContext(),"removed",Toast.LENGTH_SHORT).show();
                                    notifyDataSetChanged();
                                    setTotalValues(totalReceive_bill,totalPay_bill,getEntireCurrentList());
                                }
                                dialog.dismiss();
                            }
                        });
                        dialog.show();

                    }
                });
                myViewHolder.save.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Entry e=getEntryAtPosition(position);
                        e.setToOrFrom(myViewHolder.updatePayeeOrPayer.getText().toString());
                        e.setName(myViewHolder.updateName.getText().toString());
                        e.setAmount(Float.parseFloat(myViewHolder.updateAmt.getText().toString()));
                        if(myViewHolder.updateBorrowedRB.isChecked()) e.setTo(1);
                        else e.setTo(0);
                        entryViewModel.updateEntry(e);
                        myViewHolder.cancel.setVisibility(View.GONE);
                        myViewHolder.updateFields.setVisibility(View.GONE);
                        myViewHolder.save.setVisibility(View.GONE);
                        myViewHolder.clearAll();

                    }
                });

            }
        };
        recyclerView.setAdapter(myRVAdapter);

        entryViewModel= ViewModelProviders.of(this).get(EntryViewModel.class);
        entryViewModel.getAllEntries().observe(this, new Observer<List<Entry>>() {
            @Override
            public void onChanged(List<Entry> entries) {
                list.clear();
                list.addAll(entries);
                myRVAdapter.notifyDataSetChanged();
                setTotalValues(totalReceive, totalPay, list);
            }
        });
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String payeeOrPayerName=payeeOrPayer.getText().toString();
                String reasonName=name.getText().toString();
                String amountStr=amt.getText().toString();
                int to=-1;
                if(borrowedRB.isChecked()) to=1;
                else if(lentRB.isChecked()) to=0;
                if(payeeOrPayerName.equals("")){
                    payeeOrPayer.setError("Enter this");
                    return;
                }
                if(reasonName.equals("")){
                    name.setError("Enter this");
                    return;
                }
                if(amountStr.equals("")){
                    amt.setError("Enter this");
                    return;
                }
                if(to==-1){
                    Toast.makeText(getApplicationContext(),"Select a category!",Toast.LENGTH_SHORT).show();
                    return;
                }
                float amount=Float.parseFloat(amountStr);
                entryViewModel.insertEntry(new Entry(payeeOrPayerName,reasonName,amount,to));
                payeeOrPayer.setText("");
                name.setText("");
                amt.setText("");
                radioGroup.clearCheck();
            }
        });

        tempList=new ArrayList<>();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if(newText.equals("")){
                    myRVAdapter.fromSearch=false;
                    myRVAdapter.setData(list);
                    myRVAdapter.notifyDataSetChanged();
                    fab.show();
                    billTotal.setVisibility(View.VISIBLE);
                    billIndiv.setVisibility(View.GONE);
                    totalPay_bill.setText("0.0");
                    totalReceive_bill.setText("0.0");
                }
                else {
                    myRVAdapter.fromSearch=true;
                    fab.hide();
                    tempList.clear();
                    for(Entry i:list){
                        String s=i.getToOrFrom().toLowerCase();
                        if(s.contains(newText.toLowerCase())){
                            tempList.add(i);
                        }
                    }
                    myRVAdapter.setData(tempList);
                    myRVAdapter.notifyDataSetChanged();
                    setTotalValues(totalReceive_bill,totalPay_bill,tempList);
                    billTotal.setVisibility(View.GONE);
                    billIndiv.setVisibility(View.VISIBLE);
                }

                return true;
            }
        });

    }

    private void setTotalValues(TextView totalReceive,TextView totalPay,List<Entry> list){
        float moneyFrom=0;
        float moneyTo=0;
        for(Entry e:list) {
            if (e.getTo() == 0) moneyFrom += e.getAmount();
            else if (e.getTo() == 1) moneyTo += e.getAmount();
        }
        totalReceive.setText(moneyFrom+"");
        totalPay.setText(moneyTo+"");
    }
    public AlertDialog entryDeleteHelperMethod(){
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setTitle("Are you sure?").setMessage("Do you want to delete this entry ?").setCancelable(false).setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog dialog=builder.create();
        return dialog;

    }
    private void deleteAllEntriesHelper(){
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setTitle("Delete everything ?").setMessage(R.string.warning_delete_all_db)
                .setCancelable(true)
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setPositiveButton("CLEAR ENTIRE DATABASE", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        entryViewModel.deleteAll();
                        dialog.dismiss();
                    }
                });
        final AlertDialog dialog=builder.create();
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface d) {
                dialog.getButton(DialogInterface.BUTTON_POSITIVE).setBackgroundResource(R.drawable.delete_btn_bg);
                dialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(getResources().getColor(android.R.color.white));

                dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.success_green));


            }
        });
        dialog.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater=getMenuInflater();
        menuInflater.inflate(R.menu.main_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.deleteAllMenuItem:{
                deleteAllEntriesHelper();
                return true;
            }
            case R.id.save_all_data_as_pdf:{
                downloadPdf();
                return true;
            }
            default:{
                return super.onOptionsItemSelected(item);
            }
        }
    }
    Boolean permission;
    public void downloadPdf(){
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        permission=false;
        builder.setTitle("Save report as PDF?")
                .setCancelable(false)
                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        askForPermissions();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        builder.create().show();
    }

    public void askForPermissions(){
        String requiredPermissions[]=new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE};
        ArrayList<String> permissionList=new ArrayList<>();
        if(ContextCompat.checkSelfPermission(this,requiredPermissions[0])!= PackageManager.PERMISSION_GRANTED){
            permissionList.add(requiredPermissions[0]);
        }
        if(ContextCompat.checkSelfPermission(this,requiredPermissions[1])!=PackageManager.PERMISSION_GRANTED){
            permissionList.add(requiredPermissions[1]);
        }
        if(permissionList.isEmpty()){
            completeDownloadTask();
        }
        else{
            ActivityCompat.requestPermissions(this,permissionList.toArray(new String[permissionList.size()]),1001);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==1001){
            if(grantResults[0]==PackageManager.PERMISSION_GRANTED && grantResults[1]==PackageManager.PERMISSION_GRANTED){
                completeDownloadTask();
            }
            else{
                Toast.makeText(getApplicationContext(),"Could not get the required permissions to download file",Toast.LENGTH_SHORT).show();
            }
        }
    }

    List<Entry> listerer=new ArrayList<>();
    public void completeDownloadTask(){

        AsyncTask<Void,Void,Integer> task=new AsyncTask<Void, Void, Integer>() {
            @Override
            protected Integer doInBackground(Void... voids) {
                List<Entry> listForDownload=entryViewModel.getAllEntriesNonLiveDataList();
                int result=saveAsPdfTask(listForDownload);
                return result;
            }

            @Override
            protected void onPostExecute(Integer integer) {
                super.onPostExecute(integer);
                switch (integer){
                    case RESULT_NOT_PERFORMED:{
                        Toast.makeText(getApplicationContext(),"Task could not be performed",Toast.LENGTH_SHORT).show();
                        break;
                    }
                    case RESULT_EMPTY:{
                        Toast.makeText(getApplicationContext(),"Nothing to save",Toast.LENGTH_SHORT).show();
                        break;
                    }
                    case RESULT_COULD_NOT_CREATE_DIRECTORY:{
                        Toast.makeText(getApplicationContext(),"Directory 'Debt Manager' could not be created",Toast.LENGTH_SHORT).show();
                        break;
                    }
                    case RESULT_EXCEPTION:{
                        Toast.makeText(getApplicationContext(),"Oops...some error occurred while saving the file",Toast.LENGTH_SHORT).show();
                        break;
                    }
                    case RESULT_SUCCESSFUL:{
                        Toast.makeText(getApplicationContext(),"File saved to /Debt Tracker",Toast.LENGTH_SHORT).show();
                        break;
                    }
                }

            }
        };
        task.execute();
    }


    final int RESULT_NOT_PERFORMED=1;
    final int RESULT_SUCCESSFUL=2;
    final int RESULT_EMPTY=3;
    final int RESULT_COULD_NOT_CREATE_DIRECTORY=4;
    final int RESULT_EXCEPTION=5;

    private int saveAsPdfTask(List<Entry> entries){
        int result=RESULT_NOT_PERFORMED;

        if(entries.isEmpty()) return RESULT_EMPTY;


        Font font14Bold=new Font(Font.FontFamily.HELVETICA,14,Font.BOLD);
        Font font16=new Font(Font.FontFamily.TIMES_ROMAN,16);
        Font font18Bold=new Font(Font.FontFamily.TIMES_ROMAN,18,Font.BOLD);
        Font font18BoldUnderline=new Font(Font.FontFamily.TIMES_ROMAN,18,Font.UNDERLINE|Font.BOLD);
        Font font20=new Font(Font.FontFamily.TIMES_ROMAN,20);
        Font font24Bold=new Font(Font.FontFamily.TIMES_ROMAN,24,Font.BOLD);

        PdfPTable table_BORROWED=new PdfPTable(4);

        table_BORROWED.addCell(new PdfPCell(new Phrase("Serial No",font14Bold)));
        table_BORROWED.addCell(new PdfPCell(new Phrase("Borrowed from",font14Bold)));
        table_BORROWED.addCell(new PdfPCell(new Phrase("Purpose",font14Bold)));
        table_BORROWED.addCell(new PdfPCell(new Phrase("Amount(Rs.)",font14Bold)));

        PdfPTable table_LENT=new PdfPTable(4);
        table_LENT.addCell(new PdfPCell(new Phrase("Serial No",font14Bold)));
        table_LENT.addCell(new PdfPCell(new Phrase("Lent to",font14Bold)));
        table_LENT.addCell(new PdfPCell(new Phrase("Purpose",font14Bold)));
        table_LENT.addCell(new PdfPCell(new Phrase("Amount(Rs.)",font14Bold)));

        int borrowedSL=1,lentSL=1;
        for(Entry e:entries){
            if(e.getTo()==1){
                //borrowed
                table_BORROWED.addCell(borrowedSL+"");
                table_BORROWED.addCell(e.getToOrFrom());
                table_BORROWED.addCell(e.getName());
                table_BORROWED.addCell(e.getAmount()+"");
                borrowedSL++;
            }
            else if(e.getTo()==0){
                //lent
                table_LENT.addCell(lentSL+"");
                table_LENT.addCell(e.getToOrFrom());
                table_LENT.addCell(e.getName());
                table_LENT.addCell(e.getAmount()+"");
                lentSL++;
            }
        }

        //
        Calendar calendar=Calendar.getInstance();
        SimpleDateFormat simpleDateFormat=new SimpleDateFormat("dd-MMM-yyy E hh:mm:ss a \n(z)");

        String date=simpleDateFormat.format(calendar.getTime());

        //headings
        Paragraph heading1=new Paragraph("Debt Tracker Report\n",font24Bold);
        heading1.setAlignment(Paragraph.ALIGN_CENTER);

        Paragraph heading2=new Paragraph("Report as on "+date+"\n\n\n",font20);
        heading2.setAlignment(Paragraph.ALIGN_CENTER);

        Paragraph heading_BORROWED=new Paragraph("Borrowed Table\n",font18BoldUnderline);

        Paragraph subtitle_BORROWED=new Paragraph("The entries in the table show the amount of money that YOU OWE TO THE CORRESPONDING PERSON\n\n",font16);

        Paragraph heading_LENT=new Paragraph("Lent Table\n",font18BoldUnderline);

        Paragraph subtitle_LENT=new Paragraph("The entries in the table show the amount of money that THE CORRESPONDING PERSON OWES TO YOU\n\n",font16);




        String absolutePath=Environment.getExternalStorageDirectory().getAbsolutePath();
        File directoryRoute=new File(absolutePath+"/Debt Tracker");
        if(!directoryRoute.exists()){
            if(!directoryRoute.mkdir()){
                //Toast.makeText(getApplicationContext(),"Could not create directory",Toast.LENGTH_SHORT);
                return RESULT_COULD_NOT_CREATE_DIRECTORY;
            }
        }
        String fileNameSuffix=((Calendar.getInstance().getTimeInMillis()))+"-"+Calendar.getInstance().get(Calendar.DAY_OF_MONTH)+"."+(Calendar.getInstance().get(Calendar.MONTH)+1)+"."+Calendar.getInstance().get(Calendar.YEAR);
        File actualFile=new File(directoryRoute.getAbsolutePath()+"/Debt_Tracker_Report-"+fileNameSuffix+".pdf");

        Document document=new Document();

        try{
            FileOutputStream fileOutputStream=new FileOutputStream(actualFile);
            PdfWriter.getInstance(document,fileOutputStream);

            document.open();

            document.add(heading1);
            document.add(heading2);

            document.add(heading_BORROWED);
            document.add(subtitle_BORROWED);
            document.add(table_BORROWED);

            document.add(new Paragraph("\n\n"));

            document.add(heading_LENT);
            document.add(subtitle_LENT);
            document.add(table_LENT);



            document.close();
            result=RESULT_SUCCESSFUL;

        }
        catch (IOException e){
            result=RESULT_EXCEPTION;
        }
        catch (DocumentException e){
            result=RESULT_EXCEPTION;
        }
        return result;

    }
}
