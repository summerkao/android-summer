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
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class MyOrders extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {

    /* Views */
    SwipeRefreshLayout refreshControl;


    /* Variables */
    List<ParseObject> myOrdersArray;
    ParseUser currUser = ParseUser.getCurrentUser();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.my_orders);
        super.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // Set back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        // Set Title of the ActionBar
        getSupportActionBar().setTitle("My Orders");



        // Init a refreshControl
        refreshControl = (SwipeRefreshLayout) findViewById(R.id.swiperefresh);
        refreshControl.setOnRefreshListener(this);
        refreshControl.post(new Runnable() {
            @Override
            public void run() {
                queryMyOrders();
        }});




    }//end onCreate()



    // MARK: - QUERY MY ORDERS ------------------------------------------------------------
    void queryMyOrders() {
        Configs.showPD("Loading ...", MyOrders.this);

        ParseQuery query = ParseQuery.getQuery(Configs.ORDERS_CLASS_NAME);
        query.whereEqualTo(Configs.ORDERS_USER_POINTER, currUser);
        query.orderByDescending(Configs.ORDERS_CREATED_AT);
        query.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> objects, ParseException error) {
                if (error == null) {
                    myOrdersArray = objects;
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
                                cell = inflater.inflate(R.layout.cell_my_orders, null);
                            }

                            // Get Parse object
                            final ParseObject oObj = myOrdersArray.get(position);

                            // Get userPointer
                            final View finalCell = cell;
                            oObj.getParseObject(Configs.ORDERS_PRODUCT_POINTER).fetchIfNeededInBackground(new GetCallback<ParseObject>() {
                                public void done(ParseObject prodPointer, ParseException e) {


                                    // Get product name
                                    TextView pNameTxt = (TextView) finalCell.findViewById(R.id.cmyoProdNameTxt);
                                    pNameTxt.setText(prodPointer.getString(Configs.PRODUCTS_NAME));

                                    // Get qty
                                    TextView qtyTxt = (TextView) finalCell.findViewById(R.id.cmyoQtyTxt);
                                    int qty = (int) oObj.getNumber(Configs.ORDERS_PRODUCT_QTY);
                                    qtyTxt.setText("Qty: " + String.valueOf(qty));

                                    // Get date
                                    TextView dateTxt = (TextView) finalCell.findViewById(R.id.cmyoDatetxt);
                                    Date aDate = oObj.getCreatedAt();
                                    SimpleDateFormat df = new SimpleDateFormat("MMM dd yyyy | hh:mm a");
                                    dateTxt.setText(df.format(aDate));

                                    // Get Image
                                    final ImageView anImage = (ImageView) finalCell.findViewById(R.id.cmyoProdImage);
                                    ParseFile fileObject = (ParseFile)prodPointer.get(Configs.PRODUCTS_IMAGE1);
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

                        @Override public int getCount() { return myOrdersArray.size(); }
                        @Override public Object getItem(int position) { return myOrdersArray.get(position); }
                        @Override public long getItemId(int position) { return position; }
                    }


                    // Init ListView and set its adapter
                    ListView aList = (ListView) findViewById(R.id.myoOrdersListView);
                    aList.setAdapter(new ListAdapter(MyOrders.this, myOrdersArray));


                // Error in query
                } else {
                    Configs.hidePD();
                    Configs.simpleAlert(error.getMessage(), MyOrders.this);
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



    @Override
    public void onRefresh() {
        queryMyOrders();
        refreshControl.setRefreshing(false);
    }

}//@end
