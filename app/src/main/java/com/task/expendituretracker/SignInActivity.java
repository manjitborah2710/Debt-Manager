package com.task.expendituretracker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.task.expendituretracker.alerts.ProgressDialog;
import com.task.expendituretracker.repositories.DownloadAndDeleteHelper;


public class SignInActivity extends AppCompatActivity implements View.OnClickListener {
    EditText email,pwd;
    Button signIn,register,logout,verify,deleteAccount;
    LinearLayout layout;
    RelativeLayout verificationRL;
    ImageView verificationIV;
    TextView verificationTV;
    LinearLayout emailAndPwdFields;
    FirebaseUser firebaseUser;
    FirebaseAuth firebaseAuth;
    TextView resetPwd;
    TextView forgotPwd;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);


        email=findViewById(R.id.email_et);
        pwd=findViewById(R.id.pwd_et);
        signIn=findViewById(R.id.sign_in_btn);
        register=findViewById(R.id.register_btn);

        signIn.setOnClickListener(this);
        register.setOnClickListener(this);

        layout=findViewById(R.id.linearLayoutSignIn);

        verificationRL=findViewById(R.id.verification_relative_layout);
        verificationIV=findViewById(R.id.verification_icon_iv);
        verificationTV=findViewById(R.id.verification_textview);
        emailAndPwdFields=findViewById(R.id.emailAndPwdFields_LinearLayout);

        logout=findViewById(R.id.logout_btn);
        logout.setOnClickListener(this);
        verify=findViewById(R.id.verify_btn);
        verify.setOnClickListener(this);

        deleteAccount=findViewById(R.id.delete_account_btn);
        deleteAccount.setOnClickListener(this);

        resetPwd=findViewById(R.id.reset_pwd_tv);
        resetPwd.setOnClickListener(this);

        forgotPwd=findViewById(R.id.forgot_pwd_tv);
        forgotPwd.setOnClickListener(this);


        verificationRL.setVisibility(View.GONE);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        checkForSignIns();


    }


    @Override
    public void onClick(View v){
        switch (v.getId()){
            case R.id.sign_in_btn:{
                hideKeyboard(this);
                signIn();
                break;
            }
            case R.id.register_btn:{
//                hideKeyboard(this);
                register();
                break;
            }
            case R.id.logout_btn:{
                signOut();
                break;
            }
            case R.id.verify_btn:{
                verifyEmail();
                break;
            }
            case R.id.delete_account_btn:{
                deleteEntireAccount();
                break;
            }
            case R.id.reset_pwd_tv:{
                resetPassword();
                break;
            }
            case R.id.forgot_pwd_tv:{
                resetPassword();
                break;
            }
        }
    }

    private void resetPassword(){
        final AlertDialog dialog=ProgressDialog.getprogressDialog(this);
        firebaseAuth=FirebaseAuth.getInstance();
        firebaseUser=firebaseAuth.getCurrentUser();
        if(firebaseUser==null){
            if(email.getText()!=null){
                if(!email.getText().toString().trim().equals("")){
                    dialog.show();
                    firebaseAuth.sendPasswordResetEmail(email.getText().toString().trim()).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            dialog.dismiss();
                            Snackbar.make(layout,"Password reset email sent to"+email.getText().toString().trim(),Snackbar.LENGTH_LONG).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            dialog.dismiss();
                            Snackbar.make(layout,e.getMessage(),Snackbar.LENGTH_LONG).show();
                        }
                    });
                }
                else{
                    Toast.makeText(getApplicationContext(),"Enter an email",Toast.LENGTH_SHORT).show();
                }
            }
        }
        else{
            dialog.show();
            firebaseAuth.sendPasswordResetEmail(firebaseUser.getEmail()).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    dialog.dismiss();
                    Snackbar.make(layout,"Password reset email sent to "+firebaseUser.getEmail(),Snackbar.LENGTH_LONG).show();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    dialog.dismiss();
                    Snackbar.make(layout,e.getMessage(),Snackbar.LENGTH_LONG).show();
                }
            });
        }
    }

    private void deleteEntireAccount(){
        firebaseAuth=FirebaseAuth.getInstance();
        firebaseUser=firebaseAuth.getCurrentUser();
        final AlertDialog dialog=ProgressDialog.getprogressDialog(this);
        dialog.show();
        if(firebaseUser!=null){
            if(firebaseUser.isEmailVerified()){
                DatabaseReference databaseReference= FirebaseDatabase.getInstance().getReference();
                databaseReference.child("users").child(firebaseUser.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        completeDeletion(dialog);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Snackbar.make(layout,e.getMessage(),Snackbar.LENGTH_LONG).show();
                        dialog.dismiss();
                    }
                });
            }
            else{
                completeDeletion(dialog);
            }

        }
    }

    public void completeDeletion(final AlertDialog dialog){
        firebaseUser.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                dialog.dismiss();
                checkForSignIns();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                dialog.dismiss();
                Snackbar.make(layout,e.getMessage(),Snackbar.LENGTH_LONG).show();
            }
        });
    }

    private void verifyEmail(){
        firebaseAuth=FirebaseAuth.getInstance();
        firebaseUser=firebaseAuth.getCurrentUser();
        if(firebaseUser==null){
            signOut();
            return;
        }
        final AlertDialog dialog=ProgressDialog.getprogressDialog(this);
        dialog.show();
        firebaseUser.sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Snackbar.make(layout,"Email sent to : "+firebaseUser.getEmail(),Snackbar.LENGTH_LONG).show();
                dialog.dismiss();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                dialog.dismiss();
                Snackbar.make(layout,e.getMessage(),Snackbar.LENGTH_LONG).show();
            }
        });
    }


    private void signOut(){
        firebaseAuth=FirebaseAuth.getInstance();
        firebaseAuth.signOut();
        checkForSignIns();
    }

    private void signIn(){

        if(email.getText()==null || pwd.getText()==null) {
            Snackbar snackbar=Snackbar.make(layout,"Something's wrong",Snackbar.LENGTH_SHORT);
            snackbar.show();
            return;
        }
        if(email.getText().toString().trim().equals("") || pwd.getText().toString().equals("")){
            Snackbar snackbar=Snackbar.make(layout,"You have to fill both the fields",Snackbar.LENGTH_SHORT);
            snackbar.show();
            return;
        }

        final AlertDialog dialog=ProgressDialog.getprogressDialog(this);
        dialog.show();

        firebaseAuth=FirebaseAuth.getInstance();
        firebaseAuth.signInWithEmailAndPassword(email.getText().toString(),pwd.getText().toString()).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
                dialog.dismiss();
                checkForSignIns();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                dialog.dismiss();
                Snackbar.make(layout,e.getMessage(),Snackbar.LENGTH_LONG).show();
            }
        });
    }
    private void register(){

        if(email.getText()==null || pwd.getText()==null) {
            Snackbar snackbar=Snackbar.make(layout,"Something's wrong",Snackbar.LENGTH_SHORT);
            snackbar.show();
            return;
        }
        if(email.getText().toString().trim().equals("") || pwd.getText().toString().equals("")){
            Snackbar snackbar=Snackbar.make(layout,"You have to fill both the fields",Snackbar.LENGTH_SHORT);
            snackbar.show();
            return;
        }

        View view= LayoutInflater.from(this).inflate(R.layout.confirm_pwd_dialog_layout,null,false);
        final EditText cpwd=view.findViewById(R.id.confirm_pwd_et);
        AlertDialog.Builder builder=new AlertDialog.Builder(this).setCancelable(false);
        builder.setView(view).setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(cpwd.getText()!=null){
                    hideKeyboard(cpwd);
                    if(!cpwd.getText().toString().trim().equals("")){
                        cpwd.setError(null);
                        if(cpwd.getText().toString().equals(pwd.getText().toString())){
                            proceedToRegistration(email.getText().toString(),pwd.getText().toString());
                            dialog.dismiss();
                        }
                        else{
                            dialog.dismiss();
                            Toast.makeText(getApplicationContext(),"Passwords do not match",Toast.LENGTH_SHORT).show();
                        }
                    }
                    else{
                        dialog.dismiss();
                        Toast.makeText(getApplicationContext(),"Enter something",Toast.LENGTH_SHORT).show();
                    }

                }
                else{
                    Toast.makeText(getApplicationContext(),"Something's wrong",Toast.LENGTH_SHORT).show();
                }
            }
        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog dialog=builder.create();
        dialog.show();
    }

    private void proceedToRegistration(String email, String pwd){
        final AlertDialog dialog=ProgressDialog.getprogressDialog(this);
        dialog.show();
        FirebaseAuth auth=FirebaseAuth.getInstance();
        auth.createUserWithEmailAndPassword(email,pwd).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
                dialog.dismiss();
                checkForSignIns();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                dialog.dismiss();
                Snackbar.make(layout,e.getMessage(),Snackbar.LENGTH_LONG).show();
            }
        });
    }

    private static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        View view = activity.getCurrentFocus();
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
    private void hideKeyboard(View view) {
        InputMethodManager im = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        im.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public void checkForSignIns(){
        final AlertDialog dialog= ProgressDialog.getprogressDialog(this);
        dialog.show();
        firebaseAuth=FirebaseAuth.getInstance();
        firebaseUser=firebaseAuth.getCurrentUser();
        if(firebaseUser!=null){
            firebaseUser.reload().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    dialog.dismiss();
                    proceedCheckForSignIns();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    dialog.dismiss();
                    signOut();
                    verificationRL.setVisibility(View.GONE);
                    emailAndPwdFields.setVisibility(View.VISIBLE);
                    Snackbar.make(layout,e.getMessage(),Snackbar.LENGTH_SHORT).show();
                }
            });
        }
        else{
            verificationRL.setVisibility(View.GONE);
            emailAndPwdFields.setVisibility(View.VISIBLE);
            dialog.dismiss();
        }
    }

    public void proceedCheckForSignIns(){

        AlertDialog dialog= ProgressDialog.getprogressDialog(this);
        dialog.show();
        firebaseAuth=FirebaseAuth.getInstance();
        firebaseUser=firebaseAuth.getCurrentUser();
        if(firebaseUser==null){
           verificationRL.setVisibility(View.GONE);
           emailAndPwdFields.setVisibility(View.VISIBLE);
           dialog.dismiss();
        }
        else{
           if(firebaseUser.isEmailVerified()){
               verificationIV.setImageResource(R.drawable.verified_tick);
               verificationTV.setText("VERIFIED");
               verificationTV.setTextColor(getResources().getColor(R.color.verified_color));
           }
           else{
               verificationIV.setImageResource(R.drawable.unverified_tick);
               verificationTV.setText("NOT VERIFIED");
               verificationTV.setTextColor(getResources().getColor(R.color.red));

           }
           dialog.dismiss();
           emailAndPwdFields.setVisibility(View.GONE);
           verificationRL.setVisibility(View.VISIBLE);

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.sign_in_activity_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.refresh:{
                checkForSignIns();
                return true;
            }
            case android.R.id.home:{
                finish();
                return true;
            }
            default:{
                return super.onOptionsItemSelected(item);
            }
        }

    }

    @Override
    public void onBackPressed() {
        finish();
    }
}
