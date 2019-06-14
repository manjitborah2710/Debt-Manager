package com.task.expendituretracker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
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
import com.task.expendituretracker.adapters.MyRVAdapter;
import com.task.expendituretracker.models.Entry;
import com.task.expendituretracker.viewmodels.EntryViewModel;

import java.util.ArrayList;
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
        builder.setTitle("Are you sure you want to delete all the entries?").setMessage("This will clear all the entries. Please be careful before you proceed")
                .setCancelable(false)
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        entryViewModel.deleteAll();
                        dialog.dismiss();
                    }
                });
        AlertDialog dialog=builder.create();
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
            }
            default:{
                return super.onOptionsItemSelected(item);
            }
        }

    }
}
