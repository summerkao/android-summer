package domain.com.shoppy;

/*-----------------------------------

    - Shoppy -

    Created by cubycode @2017
    All Rights reserved

--------------------------------------*/

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.List;

public class ProdDetails extends AppCompatActivity {

    /* Views */
    Button addToCartButt;
    ImageView img1, img2, img3, img4;
    TextView prodNameTxt;
    TextView prodPriceTxt;
    TextView descriptionTxt;


    /* Variables */
    ParseObject prodObj;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.prod_details);
        super.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // Set back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        // Set Title of the ActionBar
        getSupportActionBar().setTitle("Product Details");


        // Init views
        addToCartButt = (Button)findViewById(R.id.pdAddToCartButt);
        img1 = (ImageView)findViewById(R.id.pdImage1);
        img2 = (ImageView)findViewById(R.id.pdImage2);
        img3 = (ImageView)findViewById(R.id.pdImage3);
        img4 = (ImageView)findViewById(R.id.pdImage4);
        prodNameTxt = (TextView)findViewById(R.id.pdProdNameTxt);
        prodPriceTxt = (TextView)findViewById(R.id.pdProdPriceTxt);
        descriptionTxt = (TextView)findViewById(R.id.pdDescTxt);




        // Get objectID from previous .java
        Bundle extras = getIntent().getExtras();
        String objectID = extras.getString("objectID");
        prodObj = ParseObject.createWithoutData(Configs.PRODUCTS_CLASS_NAME, objectID);
        try { prodObj.fetchIfNeeded().getParseObject(Configs.PRODUCTS_CLASS_NAME);

            // Call queries
            checkProduct();
            showProductDetails();



            // MARK: - ADD TO CART BUTTON -------------------------------------------------------------
            addToCartButt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    // USER IS LOGGED IN
                    if (ParseUser.getCurrentUser().getUsername() != null) {
                        Configs.showPD("Adding to Cart", ProdDetails.this);

                        ParseObject cartObj = new ParseObject(Configs.CART_CLASS_NAME);
                        ParseUser currentUser = ParseUser.getCurrentUser().getCurrentUser();

                        cartObj.put(Configs.CART_USER_POINTER, currentUser);
                        cartObj.put(Configs.CART_PRODUCT_POINTER, prodObj);
                        cartObj.put(Configs.CART_PRODUCT_QTY, 1);
                        cartObj.put(Configs.CART_TOTAL_AMOUNT, prodObj.getNumber(Configs.PRODUCTS_FINAL_PRICE) );

                        // Saving block
                        cartObj.saveInBackground(new SaveCallback() {
                             @Override
                            public void done(ParseException error) {
                                if (error == null) {
                                    Configs.hidePD();

                                    // Call query to check product
                                    checkProduct();

                                    Configs.simpleAlert("You've added a " + prodObj.getString(Configs.PRODUCTS_NAME) +  " to your Cart!", ProdDetails.this);

                               // error
                               } else {
                                    Configs.hidePD();
                                    Configs.simpleAlert(error.getMessage(), ProdDetails.this);
                         }}});




                    // USER IS NOT LOGGED IN
                    } else {

                        AlertDialog.Builder alert = new AlertDialog.Builder(ProdDetails.this);
                        alert.setMessage("You must login into your Account to add products in your Cart!")
                                .setTitle(R.string.app_name)
                                .setPositiveButton("Login", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        // Go to Login activity
                                        startActivity(new Intent(ProdDetails.this, Login.class));
                                    }})
                                .setNegativeButton("Cancel", null)
                                .setIcon(R.drawable.logo);
                        alert.create().show();
                    }


                }});


        } catch (ParseException e) { e.printStackTrace(); }


    }//end onCreate()







    // MARK: - CHECK PRODUCT ------------------------------------------------------------
    void checkProduct() {

        // Check if User is logged in
        if (ParseUser.getCurrentUser().getUsername() == null) {
            addToCartButt.setText("Add to cart");
            addToCartButt.setEnabled(true);

        // Check if this Product is already in your Cart
        } else {
            ParseQuery query = ParseQuery.getQuery(Configs.CART_CLASS_NAME);
            query.whereEqualTo(Configs.CART_USER_POINTER, ParseUser.getCurrentUser());
            query.whereEqualTo(Configs.CART_PRODUCT_POINTER, prodObj);
            query.findInBackground(new FindCallback<ParseObject>() {
                public void done(List<ParseObject> objects, ParseException error) {
                    if (error == null) {
                        if (objects.size() != 0) {
                            addToCartButt.setText("This product is in your cart");
                            addToCartButt.setEnabled(false);
                        } else {
                            addToCartButt.setText("Add to cart");
                            addToCartButt.setEnabled(true);
                        }

                    // Error in query
                    } else {
                        Configs.simpleAlert(error.getMessage(), ProdDetails.this);
            }}});

        }
    }






    // MARK: - SHOW PRODUCT DETAILS ------------------------------------------------------------
    void showProductDetails() {

        // Get price, name and description
        prodPriceTxt.setText("$ " + String.valueOf(prodObj.getNumber(Configs.PRODUCTS_FINAL_PRICE)) );
        prodNameTxt.setText(prodObj.getString(Configs.PRODUCTS_NAME));
        descriptionTxt.setText(prodObj.getString(Configs.PRODUCTS_DESCRIPTION));


        // Get 1st Image
        ParseFile fileObject1 = (ParseFile)prodObj.get(Configs.PRODUCTS_IMAGE1);
        if (fileObject1 != null ) {
            fileObject1.getDataInBackground(new GetDataCallback() {
                public void done(byte[] data, ParseException error) {
                    if (error == null) {
                        Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
                        if (bmp != null) {
                            img1.setImageBitmap(bmp);
        }}}});}


        // Get 2nd Image
        ParseFile fileObject2 = (ParseFile)prodObj.get(Configs.PRODUCTS_IMAGE2);
        if (fileObject2 != null ) {
            fileObject2.getDataInBackground(new GetDataCallback() {
                public void done(byte[] data, ParseException error) {
                    if (error == null) {
                        Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
                        if (bmp != null) {
                            img2.setImageBitmap(bmp);
        }}}});}

        // Get 3rd Image
        ParseFile fileObject3 = (ParseFile)prodObj.get(Configs.PRODUCTS_IMAGE3);
        if (fileObject3 != null ) {
            fileObject3.getDataInBackground(new GetDataCallback() {
                public void done(byte[] data, ParseException error) {
                    if (error == null) {
                        Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
                        if (bmp != null) {
                            img3.setImageBitmap(bmp);
        }}}});}

        // Get 4th Image
        ParseFile fileObject4 = (ParseFile)prodObj.get(Configs.PRODUCTS_IMAGE4);
        if (fileObject4 != null ) {
            fileObject4.getDataInBackground(new GetDataCallback() {
                public void done(byte[] data, ParseException error) {
                    if (error == null) {
                        Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
                        if (bmp != null) {
                            img4.setImageBitmap(bmp);
        }}}});}


    }





    // MARK: - ADD PRODUCT TO YOUR WISHLIST --------------------------------------------------------
    void addProductToWishList() {

        // USER IS NOT LOGGED IN!
        if (ParseUser.getCurrentUser().getUsername() == null) {
            AlertDialog.Builder alert = new AlertDialog.Builder(ProdDetails.this);
            alert.setMessage("You must login into your Account to add products in your Cart!")
                    .setTitle(R.string.app_name)
                    .setPositiveButton("Login", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // Go to Login activity
                            startActivity(new Intent(ProdDetails.this, Login.class));
                        }})
                    .setNegativeButton("Cancel", null)
                    .setIcon(R.drawable.logo);
            alert.create().show();


        // USER IS LOGGED IN
        } else {
            Configs.showPD("Adding Product to Wishlist...", ProdDetails.this);

            ParseQuery query = ParseQuery.getQuery(Configs.WISHLIST_CLASS_NAME);
            query.whereEqualTo(Configs.WISHLIST_PRODUCT_POINTER, prodObj);
            query.whereEqualTo(Configs.WISHLIST_USER_POINTER, ParseUser.getCurrentUser());
            query.findInBackground(new FindCallback<ParseObject>() {
                public void done(List<ParseObject> objects, ParseException error) {
                    if (error == null) {

                        // ADD PRODUCT TO YOUR WISHLIST
                        if (objects.size() == 0) {
                            ParseObject wishObj = new ParseObject(Configs.WISHLIST_CLASS_NAME);
                            ParseUser currentUser = ParseUser.getCurrentUser().getCurrentUser();

                            wishObj.put(Configs.WISHLIST_USER_POINTER, currentUser);
                            wishObj.put(Configs.WISHLIST_PRODUCT_POINTER, prodObj);

                            // Saving block
                            wishObj.saveInBackground(new SaveCallback() {
                                @Override
                                public void done(ParseException error) {
                                    if (error == null) {
                                        Configs.hidePD();
                                        Configs.simpleAlert("You've added a " + prodObj.getString(Configs.PRODUCTS_NAME) +  " to your Wishlist!", ProdDetails.this);

                                    // error
                                    } else {
                                        Configs.hidePD();
                                        Configs.simpleAlert(error.getMessage(), ProdDetails.this);
                             }}});


                        // THIS PRODUCT IS ALREADY IN YOUR WISHLIST!
                        } else {
                            Configs.hidePD();
                            Configs.simpleAlert("This product is already in your Wishlist!", ProdDetails.this);
                        }


                    // Error in query
                    } else {
                        Configs.hidePD();
                        Configs.simpleAlert(error.getMessage(), ProdDetails.this);
            }}});

        }

    }








    // MENU BUTTON ON ACTION BAR ----------------------------------------------------------------------
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_prod_details, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {

            // DEFAULT BACK BUTTON
            case android.R.id.home:
                this.finish();
                return true;


            // Add to Wishlist button
            case R.id.addToWishlistButt:
                addProductToWishList();

                return true;

        }
        return (super.onOptionsItemSelected(menuItem));
    }



}//@end
