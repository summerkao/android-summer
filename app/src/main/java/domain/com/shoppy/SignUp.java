package domain.com.shoppy;

/*-----------------------------------

    - Shoppy -

    Created by cubycode @2017
    All Rights reserved

--------------------------------------*/

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

public class SignUp extends AppCompatActivity {


        /* Views */
        EditText usernameTxt;
        EditText passwordTxt;
        EditText fullnameTxt;



        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.sign_up);
            super.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

            // Set Back button
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);

            // Set Title on the ActionBar
            getSupportActionBar().setTitle("Sign Up");


            // Init views
            usernameTxt = (EditText)findViewById(R.id.usernameTxt2);
            passwordTxt = (EditText)findViewById(R.id.passwordTxt2);
            fullnameTxt = (EditText)findViewById(R.id.fullnameTxt);




            // SIGN UP BUTTON
            Button signupButt = (Button)findViewById(R.id.signUpButt2);
            signupButt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (usernameTxt.getText().toString().matches("") || passwordTxt.getText().toString().matches("") ||
                            fullnameTxt.getText().toString().matches("")) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(SignUp.this);
                        builder.setMessage("You must fill all the fields to Sign Up!")
                                .setTitle(R.string.app_name)
                                .setPositiveButton("OK", null);
                        AlertDialog dialog = builder.create();
                        dialog.setIcon(R.drawable.logo);
                        dialog.show();


                    } else {
                        Configs.showPD("Please wait...", SignUp.this);
                        dismisskeyboard();

                        ParseUser user = new ParseUser();
                        user.setUsername(usernameTxt.getText().toString());
                        user.setPassword(passwordTxt.getText().toString());
                        user.setEmail(usernameTxt.getText().toString());
                        // Add FullName
                        user.put(Configs.USER_FULLNAME, fullnameTxt.getText().toString());

                        user.signUpInBackground(new SignUpCallback() {
                            public void done(ParseException error) {
                                if (error == null) {
                                    Configs.hidePD();
                                    startActivity(new Intent(SignUp.this, Home.class));
                                } else {
                                    Configs.hidePD();
                                    Configs.simpleAlert(error.getMessage(), SignUp.this);
                                }
                            }});
                    }

                }});




            // MARK: - TERMS OF USE BUTTON
            Button touButt = (Button)findViewById(R.id.touButt);
            touButt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(SignUp.this, TermsOfUse.class));
            }});

        }// end onCreate()



        // DISMISS KEYBOARD
        public void dismisskeyboard() {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(usernameTxt.getWindowToken(), 0);
            imm.hideSoftInputFromWindow(passwordTxt.getWindowToken(), 0);
            imm.hideSoftInputFromWindow(fullnameTxt.getWindowToken(), 0);
        }


        // MENU BUTTONS
        @Override
        public boolean onOptionsItemSelected(MenuItem menuItem) {
            switch (menuItem.getItemId()) {
                // DEFAULT BACK BUTTON
                case android.R.id.home:
                    this.finish();
                    return true;
            }
            return (super.onOptionsItemSelected(menuItem));
        }


    }//@end

