package domain.com.shoppy;

/*-----------------------------------

    - Shoppy -

    Created by cubycode @2017
    All Rights reserved

--------------------------------------*/

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.List;

public class Wishlist extends AppCompatActivity {


    /* Variables */
    List<ParseObject> wishArray;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wishlist);
        super.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // Set Title of the ActionBar
        getSupportActionBar().setTitle("Wishlist");




        // Init TabBar buttons
        Button tab_one = (Button)findViewById(R.id.tab_home);
        Button tab_two = (Button)findViewById(R.id.tab_cart);
        Button tab_three = (Button)findViewById(R.id.tab_contact);

        tab_one.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Wishlist.this, Home.class));
            }});

        tab_two.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Wishlist.this, Cart.class));
            }});

        tab_three.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Wishlist.this, ContactUs.class));
            }});



        // Call query
        queryWishlist();


    }// end onCreate()





    // MARK: - QUERY WISHLIST ---------------------------------------------------------------
    void queryWishlist() {

        // USER IS NOT LOGGED IN!
        if (ParseUser.getCurrentUser().getUsername() == null) {
            AlertDialog.Builder alert = new AlertDialog.Builder(Wishlist.this);
            alert.setMessage("You must login into your Account to add/SEE products in your Wishlist!")
                    .setTitle(R.string.app_name)
                    .setPositiveButton("Login", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // Go to Login activity
                            startActivity(new Intent(Wishlist.this, Login.class));
                        }})
                    .setNegativeButton("Cancel", null)
                    .setIcon(R.drawable.logo);
            alert.create().show();



        // USER IS LOGGED IN
        } else {
            Configs.showPD("Loading Wishlist...", Wishlist.this);

            ParseQuery query = ParseQuery.getQuery(Configs.WISHLIST_CLASS_NAME);
            query.whereEqualTo(Configs.WISHLIST_USER_POINTER, ParseUser.getCurrentUser());
            query.findInBackground(new FindCallback<ParseObject>() {
                public void done(List<ParseObject> objects, ParseException error) {
                    if (error == null) {
                        wishArray = objects;
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
                                    cell = inflater.inflate(R.layout.cell_wishlist, null);
                                }

                                // Get Parse object
                                ParseObject wObj = wishArray.get(position);


                                // Get Product Pointer
                                final View finalCell = cell;
                                wObj.getParseObject(Configs.WISHLIST_PRODUCT_POINTER).fetchIfNeededInBackground(new GetCallback<ParseObject>() {
                                    public void done(ParseObject prodPointer, ParseException e) {

                                        // Get product name
                                        TextView nameTxt = (TextView) finalCell.findViewById(R.id.cwProdNameTxt);
                                        nameTxt.setText(prodPointer.getString(Configs.PRODUCTS_NAME));

                                        // Get final price
                                        TextView fpTxt = (TextView) finalCell.findViewById(R.id.cwPriceTxt);
                                        String currency = prodPointer.getString(Configs.PRODUCTS_CURRENCY);
                                        fpTxt.setText(currency + " " + String.valueOf(prodPointer.getNumber(Configs.PRODUCTS_FINAL_PRICE)) );

                                        // Get Image
                                        final ImageView anImage = (ImageView) finalCell.findViewById(R.id.cwProdImage);
                                        ParseFile fileObject = (ParseFile) prodPointer.get(Configs.PRODUCTS_IMAGE1);
                                        fileObject.getDataInBackground(new GetDataCallback() {
                                            public void done(byte[] data, ParseException error) {
                                                if (error == null) {
                                                    Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
                                                    if (bmp != null) {
                                                        anImage.setImageBitmap(bmp);
                                        }}}});

                                }});


                                return cell;
                            }

                            @Override public int getCount() { return wishArray.size(); }
                            @Override public Object getItem(int position) { return wishArray.get(position); }
                            @Override public long getItemId(int position) { return position; }
                        }


                        // Init ListView and set its adapter
                        ListView aList = (ListView) findViewById(R.id.wWishlistListView);
                        aList.setAdapter(new ListAdapter(Wishlist.this, wishArray));
                        aList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {

                                ParseObject wObj = wishArray.get(position);

                                // Get Product pointer
                                wObj.getParseObject(Configs.WISHLIST_PRODUCT_POINTER).fetchIfNeededInBackground(new GetCallback<ParseObject>() {
                                    public void done(ParseObject prodPointer, ParseException e) {
                                        Intent i = new Intent(Wishlist.this, ProdDetails.class);
                                        Bundle extras = new Bundle();
                                        extras.putString("objectID", prodPointer.getObjectId());
                                        i.putExtras(extras);
                                        startActivity(i);
                                 }});

                        }});



                        // MARK: - LONG TAP ON A CELL TO DELETE ITEM
                        aList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                            @Override
                            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                                final ParseObject wObj = wishArray.get(position);

                                AlertDialog.Builder alert = new AlertDialog.Builder(Wishlist.this);
                                alert.setMessage("Are you sure you want to remove this product from your Wishlist?")
                                    .setTitle(R.string.app_name)
                                    .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {

                                            wObj.deleteInBackground(new DeleteCallback() {
                                                @Override
                                                public void done(ParseException error) {
                                                    if (error == null) {
                                                        // Recall query
                                                        queryWishlist();
                                                    // error
                                                    } else {
                                                        Configs.simpleAlert(error.getMessage(), Wishlist.this);
                                            }}});

                                        }})
                                        .setNegativeButton("Cancel", null)
                                    .setIcon(R.drawable.logo);
                                alert.create().show();

                                return true;
                        }});


                    // Error in query
                    } else {
                        Configs.hidePD();
                        Configs.simpleAlert(error.getMessage(), Wishlist.this);
            }}});


        }// end IF

    }








    // MENU BUTTON ON ACTION BAR ----------------------------------------------------------------------
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_wishlist, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {

            // Refresh Button
            case R.id.refreshButt:
                // Call query
                queryWishlist();
                return true;

        }
        return (super.onOptionsItemSelected(menuItem));
    }


}//@end
