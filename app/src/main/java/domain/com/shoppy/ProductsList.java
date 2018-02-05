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
import android.graphics.Paint;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.List;

public class ProductsList extends AppCompatActivity {


    /* Variables */
    List<ParseObject>productsArray;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.products_list);
        super.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // Set back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);


        // Get string from Home.java
        Bundle extras = getIntent().getExtras();
        String catName = extras.getString("categoryName");

        // Set Title of the ActionBar
        getSupportActionBar().setTitle(catName.toUpperCase());



        // Call query
        queryProducts(catName);

    }//end onCreate()







    // MARK: - QUERY PRODUCTS ------------------------------------------------------------------------
    void queryProducts(String category) {
        Configs.showPD("Loading Products...", ProductsList.this);


        ParseQuery query = ParseQuery.getQuery(Configs.PRODUCTS_CLASS_NAME);
        query.whereEqualTo(Configs.PRODUCTS_CATEGORY, category);
        query.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> objects, ParseException error) {
                if (error == null) {
                    productsArray = objects;
                    Configs.hidePD();


                    // CUSTOM GRID ADAPTER
                    class GridAdapter extends BaseAdapter {
                        private Context context;

                        public GridAdapter(Context context, List<ParseObject> objects) {
                            super();
                            this.context = context;
                        }


                        // CONFIGURE CELL
                        @Override
                        public View getView(int position, View cell, ViewGroup parent) {
                            if (cell == null) {
                                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                                cell = inflater.inflate(R.layout.cell_products, null);
                            }

                            // Get Parse object
                            ParseObject pObj = productsArray.get(position);


                            // Get name
                            TextView nameTxt = (TextView) cell.findViewById(R.id.cpProdNameTxt);
                            nameTxt.setText(pObj.getString(Configs.PRODUCTS_NAME));

                            String currency = pObj.getString(Configs.PRODUCTS_CURRENCY);

                            // Get BIG price
                            TextView bigTxt = (TextView) cell.findViewById(R.id.cpBigPriceTxt);
                            if (pObj.getNumber(Configs.PRODUCTS_BIG_PRICE) != null) {
                                bigTxt.setText(currency + String.valueOf(pObj.getNumber(Configs.PRODUCTS_BIG_PRICE)));
                                bigTxt.setPaintFlags(bigTxt.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                            }

                            // Get FINAL price
                            TextView finalTxt = (TextView) cell.findViewById(R.id.cpFinalPricetxt);
                            finalTxt.setText(currency + String.valueOf(pObj.getNumber(Configs.PRODUCTS_FINAL_PRICE) ));

                            // Get Image
                            final ImageView aImage = (ImageView) cell.findViewById(R.id.cpProdImage);
                            ParseFile fileObject = (ParseFile)pObj.get(Configs.PRODUCTS_IMAGE1);
                            fileObject.getDataInBackground(new GetDataCallback() {
                                public void done(byte[] data, ParseException error) {
                                    if (error == null) {
                                        Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
                                        if (bmp != null) {
                                            aImage.setImageBitmap(bmp);
                            }}}});


                            return cell;
                        }
                        @Override public int getCount() { return productsArray.size(); }
                        @Override public Object getItem(int position) { return productsArray.get(position); }
                        @Override public long getItemId(int position) { return position; }
                    }


                    // Init GridView and set its adapter
                    GridView aGrid = (GridView) findViewById(R.id.productsGridView);
                    aGrid.setAdapter(new GridAdapter(ProductsList.this, productsArray));

                    // Set number of Columns accordingly to the device used
                    float scalefactor = getResources().getDisplayMetrics().density * 150; // 150 is the cell's width
                    int number = getWindowManager().getDefaultDisplay().getWidth();
                    int columns = (int) ((float) number / (float) scalefactor);
                    aGrid.setNumColumns(columns);

                    aGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                            ParseObject pObj = productsArray.get(position);
                            Intent i = new Intent(ProductsList.this, ProdDetails.class);
                            Bundle extras = new Bundle();
                            extras.putString("objectID", pObj.getObjectId());
                            i.putExtras(extras);
                            startActivity(i);

                    }});


                // Error in query
                } else {
                    Configs.hidePD();
                    Configs.simpleAlert(error.getMessage(), ProductsList.this);
        }}});


    }









    // BACK BUTTON --------------------------------------------
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
