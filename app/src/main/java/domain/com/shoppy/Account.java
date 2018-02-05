package domain.com.shoppy;

/*-----------------------------------

    - Shoppy -

    Created by cubycode @2017
    All Rigths reserved

--------------------------------------*/

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.parse.LogOutCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SaveCallback;

public class Account extends AppCompatActivity {

    /* Views */
    EditText fullnameTxt;
    EditText emailTxt;
    EditText shAddrTxt;


    /* Variables */
    ParseUser currUser = ParseUser.getCurrentUser();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.account);
        super.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // Set back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);




        // Init views
        fullnameTxt = (EditText)findViewById(R.id.aFullNameTxt);
        emailTxt = (EditText)findViewById(R.id.aEmailTxt);
        shAddrTxt = (EditText)findViewById(R.id.aShippingTxt);


        // MARK: - GET USER'S DETAILS
        getSupportActionBar().setTitle(currUser.getUsername());
        emailTxt.setText(currUser.getString(Configs.USER_EMAIL));
        if (currUser.getString(Configs.USER_FULLNAME) != null) { fullnameTxt.setText(currUser.getString(Configs.USER_FULLNAME)); }
        if (currUser.getString(Configs.USER_SHIPPING_ADDRESS) != null) { shAddrTxt.setText(currUser.getString(Configs.USER_SHIPPING_ADDRESS)); }



        // MARK: - UPDATE PROFILE BUTTON --------------------------------------------
        Button updateProfButt = (Button)findViewById(R.id.aUpdatePorfileButt);
        updateProfButt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (fullnameTxt.getText().toString().matches("") || emailTxt.getText().toString().matches("") || shAddrTxt.getText().toString().matches("")
                        ) {
                    Configs.simpleAlert("You must enter your full name, email and shipping address!", Account.this);

                // UPDATE PROFILE
                } else {
                    Configs.showPD("Updating profile...", Account.this);

                    currUser.put(Configs.USER_FULLNAME, fullnameTxt.getText().toString());
                    currUser.put(Configs.USER_EMAIL, emailTxt.getText().toString());
                    currUser.put(Configs.USER_SHIPPING_ADDRESS, shAddrTxt.getText().toString());

                    // Saving block
                    currUser.saveInBackground(new SaveCallback() {
                         @Override
                         public void done(ParseException error) {
                            if (error == null) {
                                Configs.hidePD();
                                Configs.simpleAlert("Your profile has been updated!", Account.this);

                            // error
                            } else {
                                Configs.hidePD();
                                Configs.simpleAlert(error.getMessage(), Account.this);
                    }}});

                }
         }});





        // MARK: - MY ORDERS BUTTON --------------------------------------------------------
        Button myOrdersButt = (Button)findViewById(R.id.aMyOrdersButt);
        myOrdersButt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Account.this, MyOrders.class));
        }});



        // MARK: - TERMS OF USE BUTTON --------------------------------------------------------
        Button touButt = (Button)findViewById(R.id.aTOUButt);
        touButt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Account.this, TermsOfUse.class));
            }});



    }//end onCreate()









    // MENU BUTTON ON ACTION BAR ----------------------------------------------------------------------
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_account, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            // DEFAULT BACK BUTTON
            case android.R.id.home:
                this.finish();
                return true;


            // Logout button
            case R.id.logoutButt:

                AlertDialog.Builder alert = new AlertDialog.Builder(Account.this);
                alert.setMessage("Are you sure you want to logout?")
                    .setTitle(R.string.app_name)
                    .setPositiveButton("Logout", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            Configs.showPD("Logging out...", Account.this);

                            ParseUser.logOutInBackground(new LogOutCallback() {
                                @Override
                                public void done(ParseException e) {
                                    Configs.hidePD();
                                    finish();
                            }});
                    }})
                        .setNegativeButton("Cancel", null)
                    .setIcon(R.drawable.logo);
                alert.create().show();


                return true;

        }
        return (super.onOptionsItemSelected(menuItem));
    }

}//@end
