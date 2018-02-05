package domain.com.shoppy;

/*-----------------------------------

    - Shoppy -

    Created by cubycode @2017
    All Rigths reserved

--------------------------------------*/

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class ContactUs extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.contact_us);
        super.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // Set Title of the ActionBar
        getSupportActionBar().setTitle("Contact Us");


        // Init TabBar buttons
        Button tab_one = (Button)findViewById(R.id.tab_home);
        Button tab_two = (Button)findViewById(R.id.tab_wishlist);
        Button tab_three = (Button)findViewById(R.id.tab_cart);

        tab_one.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ContactUs.this, Home.class));
            }});

        tab_two.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ContactUs.this, Wishlist.class));
            }});

        tab_three.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ContactUs.this, Cart.class));
            }});




        // MARK: - ADDRESS BUTTON -> OPEN ADDRESS IN GOOGLE MAPS ------------------------------------------------------------
        Button addressButt = (Button) findViewById(R.id.contAddressButt);
        final String addressStr = "http://maps.google.co.in/maps?q=" + addressButt.getText().toString();
        addressButt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(addressStr)));
        }});



        // MARK: - PHONE BUTTON -> MAKE A PHONE CALL  ------------------------------------------------------------
        Button phoneButt = (Button) findViewById(R.id.contPhoneButt);
        final String phoneNr = phoneButt.getText().toString();
        phoneButt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phoneNr)));
        }});



        // MARK: - EMAIL BUTTON -> OPEN EMAIL SERVICE ------------------------------------------------------------
        Button emailButt = (Button) findViewById(R.id.contEmailButt);
        emailButt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent emailIntent = new Intent(Intent.ACTION_SEND);
                emailIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                emailIntent.setType("message/rfc822");
                emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[] { Configs.CONTACT_US_EMAIL_ADDRESS });
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Contact request");
                emailIntent.putExtra(Intent.EXTRA_TEXT, "Hello,\n");
                startActivity(Intent.createChooser(emailIntent, "Send email using..."));
        }});


    }//end onCreate()




}//@end
