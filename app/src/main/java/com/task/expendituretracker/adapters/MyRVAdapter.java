package com.task.expendituretracker.adapters;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.task.expendituretracker.R;
import com.task.expendituretracker.models.Entry;
import com.task.expendituretracker.viewmodels.EntryViewModel;

import java.util.List;

public class MyRVAdapter extends RecyclerView.Adapter {

    Context context;
    List<Entry> data;
    public boolean fromSearch=false;

    public MyRVAdapter(Context context, List<Entry> data) {
        this.context = context;
        this.data = data;
    }
    public void setData(List<Entry> data){
        this.data=data;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(context).inflate(R.layout.rv_layout,parent,false);
        MyViewHolder holder=new MyViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
        String toOrFrom=data.get(position).getToOrFrom();
        String name=data.get(position).getName();
        float amt=data.get(position).getAmount();
        final MyViewHolder myViewHolder=(MyViewHolder)holder;

        myViewHolder.name.setText(name);
        myViewHolder.amt.setText(amt+"");
        if(data.get(position).getTo()==1){
            myViewHolder.toOrFrom.setText("Pay To-"+toOrFrom+" : ");
            myViewHolder.amt.setTextColor(context.getResources().getColor(android.R.color.holo_red_light));

        }
        else {
            myViewHolder.toOrFrom.setText("Receive From-"+toOrFrom+" : ");
            myViewHolder.amt.setTextColor(context.getResources().getColor(android.R.color.holo_blue_light));
        }

        myViewHolder.edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myViewHolder.cancel.setVisibility(View.VISIBLE);
                myViewHolder.updateFields.setVisibility(View.VISIBLE);
                myViewHolder.updatePayeeOrPayer.setText(data.get(position).getToOrFrom());
                myViewHolder.updateName.setText(data.get(position).getName());
                myViewHolder.updateAmt.setText(data.get(position).getAmount()+"");
                if(data.get(position).getTo()==0) myViewHolder.updateLentRB.setChecked(true);
                else myViewHolder.updateBorrowedRB.setChecked(true);
            }
        });


    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public Entry getEntryAtPosition(int pos){
        return data.get(pos);
    }

    public void removeEntryFromListAtPosition(int position){
        data.remove(position);
    }
    public List<Entry> getEntireCurrentList(){
        return this.data;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{
        public TextView toOrFrom,name,amt;
        public ImageButton edit,cancel;
        public ImageButton delete,save;
        public LinearLayout updateFields;
        public EditText updatePayeeOrPayer,updateName,updateAmt;
        public RadioButton updateBorrowedRB,updateLentRB;
        public RadioGroup updateRadioGroup;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            toOrFrom = itemView.findViewById(R.id.toOrFrom);
            name = itemView.findViewById(R.id.name);
            amt = itemView.findViewById(R.id.amt);
            edit = itemView.findViewById(R.id.edit_btn_in_rv_layout);
            cancel = itemView.findViewById(R.id.cancel_btn_in_rv_layout);
            delete = itemView.findViewById(R.id.delete_btn_in_rv_layout);
            updateFields = itemView.findViewById(R.id.ll_in_rv_layout);
            updatePayeeOrPayer = itemView.findViewById(R.id.update_payeeOrPayer_et);
            updateName = itemView.findViewById(R.id.update_name_et);
            updateAmt = itemView.findViewById(R.id.update_amt_et);
            updateBorrowedRB = itemView.findViewById(R.id.update_borrowed_rb);
            updateLentRB = itemView.findViewById(R.id.update_lent_rb);
            save = itemView.findViewById(R.id.save_btn_in_rv_layout);
            updateRadioGroup=itemView.findViewById(R.id.update_radiogroup);



            cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    cancel.setVisibility(View.GONE);
                    updateFields.setVisibility(View.GONE);
                    save.setVisibility(View.GONE);
                    clearAll();
                }
            });


            save.setVisibility(View.GONE);
            setListenersToEachField();

        }
        private void setListenersToEachField(){
            updatePayeeOrPayer.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    if(isEveryFieldValid())save.setVisibility(View.VISIBLE);
                    else save.setVisibility(View.GONE);
                }
            });
            updateName.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    if(isEveryFieldValid())save.setVisibility(View.VISIBLE);
                    else save.setVisibility(View.GONE);
                }
            });
            updateAmt.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    if(isEveryFieldValid())save.setVisibility(View.VISIBLE);
                    else save.setVisibility(View.GONE);
                }
            });
            updateRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {
                    if(isEveryFieldValid())save.setVisibility(View.VISIBLE);
                    else save.setVisibility(View.GONE);
                }
            });

        }
        private boolean isEveryFieldValid(){
            boolean yes=true;
            if(updatePayeeOrPayer.getText().toString().equals("")) yes=false;
            if(updateName.getText().toString().equals("")) yes=false;
            if(updateAmt.getText().toString().equals("")) yes=false;
            if(!updateBorrowedRB.isChecked() && !updateLentRB.isChecked()) yes=false;
            return yes;
        }
        public void clearAll(){
            updateRadioGroup.clearCheck();
            updatePayeeOrPayer.setText("");
            updateName.setText("");
            updateAmt.setText("");
        }
    }
}
