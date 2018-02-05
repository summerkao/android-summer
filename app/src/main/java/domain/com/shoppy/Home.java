package domain.com.shoppy;

/*-----------------------------------

    - Shoppy -

    Created by cubycode @2017
    All Rights reserved

--------------------------------------*/


import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.ByteArrayOutputStream;
import java.util.List;

public class Home extends AppCompatActivity {


    /* Variables */
    List<ParseObject>categoriesArray;




    // ON START() ------------------------------------------------------------------------------
    @Override
    protected void onStart() {
        super.onStart();


        if (ParseUser.getCurrentUser().getUsername() != null) {
            // Register GCM Sender ID in Installation class
            ParseInstallation installation = ParseInstallation.getCurrentInstallation();


            // IMPORTANT: REPLACE "170755635456" WITH YOUR OWN GCM SENDER ID
            installation.put("GCMSenderId", "170755635456");


            installation.put("userID", ParseUser.getCurrentUser().getObjectId());
            installation.put("username", ParseUser.getCurrentUser().getUsername());
            installation.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    Log.i("log-", "REGISTERED FOR PUSH NOTIFICATIONS!");
            }});
        }
    }






    // ON CREATE() -----------------------------------------------------------------------------------
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);
        super.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // Set Title of the ActionBar
        getSupportActionBar().setTitle("Shoppy");


        // Init TabBar buttons
        Button tab_one = (Button)findViewById(R.id.tab_wishlist);
        Button tab_two = (Button)findViewById(R.id.tab_cart);
        Button tab_three = (Button)findViewById(R.id.tab_contact);

        tab_one.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Home.this, Wishlist.class));
        }});

        tab_two.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Home.this, Cart.class));
        }});

        tab_three.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Home.this, ContactUs.class));
            }});



        // IMPORTANT: COMMENT (OR REMOVE) THIS LINE AFTER RUNNING THE APP FOR THE FIRST TIME!
        createClassesAndColumnsThenRemoveThisMethod();



        // UNCOMMENT THE LINE BELOW AFTER YOU'VE RAN THE APP FOR THE FIRST TIME AND STOPPED IT
        // queryCategories();



    } // end onCreate()









    // MARK: - QUERY CATEGORIES ------------------------------------------------------------------
    void queryCategories() {
        Configs.showPD("Please wait...", Home.this);

        ParseQuery query = ParseQuery.getQuery(Configs.CATEGORIES_CLASS_NAME);
        query.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> objects, ParseException error) {
                if (error == null) {
                    categoriesArray = objects;
                    Configs.hidePD();

                    // CUSTOM LIST ADAPTER
                    class ListAdapter extends BaseAdapter {
                        private Context context;
                        public ListAdapter(Context context, List<ParseObject> objects) {
                            super();
                            this.context = context;
                        }


                        // CONFIGURE CELL
                        @Override
                        public View getView(int position, View cell, ViewGroup parent) {
                            if (cell == null) {
                                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                                cell = inflater.inflate(R.layout.cell_home, null);
                            }

                            // Get Parse object
                            ParseObject catObj = categoriesArray.get(position);

                            // Get category name
                            TextView distTxt = (TextView) cell.findViewById(R.id.cCatNameTxt);
                            distTxt.setText(catObj.getString(Configs.CATEGORIES_CATEGORY).toUpperCase());

                            // Get Image
                            final ImageView anImage = (ImageView) cell.findViewById(R.id.cCatImg);
                            ParseFile fileObject = catObj.getParseFile(Configs.CATEGORIES_IMAGE);
                            fileObject.getDataInBackground(new GetDataCallback() {
                                public void done(byte[] data, ParseException error) {
                                    if (error == null) {
                                        Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
                                        if (bmp != null) {
                                            anImage.setImageBitmap(bmp);
                            }}}});

                            // Set background color
                            ImageView colorimg = (ImageView)cell.findViewById(R.id.cColorImg);
                            colorimg.setBackgroundColor(Color.parseColor(Configs.colorsArray[position]));


                            return cell;
                        }

                        @Override public int getCount() { return categoriesArray.size(); }
                        @Override public Object getItem(int position) { return categoriesArray.get(position); }
                        @Override public long getItemId(int position) { return position; }
                    }


                    // Init ListView and set its adapter
                    ListView aList = (ListView) findViewById(R.id.categoryListView);
                    aList.setAdapter(new ListAdapter(Home.this, categoriesArray));
                    aList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        public void onItemClick(AdapterView<?> parent, View v, int position, long id) {

                            ParseObject catObj = categoriesArray.get(position);
                            Log.i("log-", "CAT TAPPED: " + catObj.getString(Configs.CATEGORIES_CATEGORY));

                            Intent i = new Intent(Home.this, ProductsList.class);
                            Bundle extras = new Bundle();
                            extras.putString("categoryName", catObj.getString(Configs.CATEGORIES_CATEGORY));
                            i.putExtras(extras);
                            startActivity(i);


                    }});


                // error
                } else {
                    Configs.hidePD();
                    Configs.simpleAlert(error.getMessage(), Home.this);
        }}});

    }









    // THIS METHOD MUST BE RAN ONLY AT FIRST STARTUP, SO AFTER RUNNING YOUR APP FOR THE FIRST TIME,
    // STOP THE APP, COMMENT createClassesAndColumnsThenRemoveThisMethod();
    // AND UNCOMMENT queryCategories();
    void createClassesAndColumnsThenRemoveThisMethod() {
        Configs.showPD("PLEASE WAIT, Creating Classes and Columns for your app...", Home.this);

        // Create Categories Class
        ParseObject cClass = new ParseObject(Configs.CATEGORIES_CLASS_NAME);
        cClass.put(Configs.CATEGORIES_CATEGORY, "EDIT ME!");

        // Save DEMO images
        ImageView evImage = (ImageView) findViewById(R.id.demoImg);
        Bitmap bitmap = ((BitmapDrawable) evImage.getDrawable()).getBitmap();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 10, stream);
        byte[] byteArray = stream.toByteArray();
        final ParseFile imageFile = new ParseFile("image.jpg", byteArray);
        cClass.put(Configs.CATEGORIES_IMAGE, imageFile);

        // Saving block
        cClass.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException error) {
                if (error == null) {


                    // Create Products Class
                    ParseObject pClass = new ParseObject(Configs.PRODUCTS_CLASS_NAME);
                    pClass.put(Configs.PRODUCTS_CATEGORY, "EDIT ME!");
                    pClass.put(Configs.PRODUCTS_NAME, "EDIT ME!");
                    pClass.put(Configs.PRODUCTS_FINAL_PRICE, 0);
                    pClass.put(Configs.PRODUCTS_BIG_PRICE, 0);
                    pClass.put(Configs.PRODUCTS_CURRENCY, "EDIT ME!");
                    pClass.put(Configs.PRODUCTS_IMAGE1, imageFile);
                    pClass.put(Configs.PRODUCTS_IMAGE2, imageFile);
                    pClass.put(Configs.PRODUCTS_IMAGE3, imageFile);
                    pClass.put(Configs.PRODUCTS_IMAGE4, imageFile);
                    pClass.put(Configs.PRODUCTS_DESCRIPTION, "EDIT ME!");

                    // Saving block
                    pClass.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException error) {
                            if (error == null) {
                                Configs.hidePD();

                                Configs.simpleAlert("Products and Category classes have been created in your Parse Dashboard on back{4}app, now you can stop the app, remove the code in Home.swift, uncomment 'queryCategories()' and start inserting Products and Categories in your database", Home.this);

                    }}});


                // error
                } else {
                    Configs.hidePD();
                    Configs.simpleAlert(error.getMessage(), Home.this);
        }}});

    }
    // END  -------------------------------------------------------------------









    // MENU BUTTON ON ACTION BAR ----------------------------------------------------------------------
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {

            // Account Button
            case R.id.accountButt:

                // USER IS LOGGED IN
                if (ParseUser.getCurrentUser().getUsername() != null) {
                    startActivity(new Intent(Home.this, Account.class));

                // USER IS NOT LOGGED IN
                } else {
                    startActivity(new Intent(Home.this, Login.class));
                }
            return true;

        }
        return (super.onOptionsItemSelected(menuItem));
    }


}//@end
