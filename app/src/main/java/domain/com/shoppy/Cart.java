package domain.com.shoppy;

/*-----------------------------------

    - Shoppy -

    Created by cubycode @2017
    All Rigths reserved

--------------------------------------*/

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.StrictMode;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
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
import com.parse.SaveCallback;
import com.paypal.android.sdk.payments.PayPalConfiguration;
import com.paypal.android.sdk.payments.PayPalPayment;
import com.paypal.android.sdk.payments.PayPalService;
import com.paypal.android.sdk.payments.PaymentActivity;
import com.paypal.android.sdk.payments.PaymentConfirmation;

import org.json.JSONException;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class Cart extends AppCompatActivity {

    /* Views */
    RelativeLayout notesLayout;
    Button checkoutButt;
    TextView totalAmountTxt;
    TextView noteTxt;
    TextView currencyTxt;


    /* Variables */
    List<ParseObject>cartArray;
    ArrayList<Double> totalAmountArray = new ArrayList<Double>();
    double totalAmount;
    int noteIndex;

    private String paymentAmount;
    public static final int PAYPAL_REQUEST_CODE = 123;
    ArrayList<String> itemsOrdered = new ArrayList<String>();



    // MARK: - PAYPAL ENVIRONMERNT CONFIGURATION ---------------------------------------------------
    private static PayPalConfiguration config = new PayPalConfiguration()
            /*
            IMPORTANT:
            Use ENVIRONMENT_SANDBOX to test the app with fake payments wiht your Sandbox username and password
            When you'll be ready to publish your app to the Play Store, switch to 'ENVIRONMENT_PRODUCTION' and 'PAYPAL_LIVE_ID'
            */

            .environment(PayPalConfiguration.ENVIRONMENT_SANDBOX  /* PayPalConfiguration.ENVIRONMENT_PRODUCTION */ )
            .clientId(PayPalConfig.PAYPAL_SANDBOX_ID   /* PayPalConfig.PAYPAL_LIVE_ID */  );

    // -----------------------------------------------------------------------------------------------





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cart);
        super.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // Set Title of the ActionBar
        getSupportActionBar().setTitle("Cart");

        // Hide notes layout
        hideNotesView();

        // Init views
        checkoutButt = (Button)findViewById(R.id.cartCheckoutButt);
        totalAmountTxt = (TextView)findViewById(R.id.cartTotalAmountTxt);
        noteTxt = (TextView)findViewById(R.id.cartNoteTxt);
        currencyTxt = (TextView)findViewById(R.id.cartCurrencyTxt2);



        // Init TabBar buttons
        Button tab_one = (Button)findViewById(R.id.tab_home);
        Button tab_two = (Button)findViewById(R.id.tab_wishlist);
        Button tab_three = (Button)findViewById(R.id.tab_contact);

        tab_one.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Cart.this, Home.class));
            }});

        tab_two.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Cart.this, Wishlist.class));
            }});

        tab_three.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Cart.this, ContactUs.class));
            }});





        // MARK: - SAVE NOTE BUTTON -----------------------------------------------------------------
        Button saveNoteButt = (Button)findViewById(R.id.cartSaveNoteButt);
        saveNoteButt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("log-", "NOTE INDEX: " + noteIndex);

                ParseObject cartObj = cartArray.get(noteIndex);
                cartObj.put(Configs.CART_NOTES, noteTxt.getText().toString());
                cartObj.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e == null) {
                            Log.i("log-", "Note Saved for product at index: " + noteIndex);

                            dismisskeyboard();
                            hideNotesView();
                        }
                }});

        }});





        // MARK: - CHECKOUT BUTTON ---------------------------------------------------------------

        // Intent for PayPal service
        Intent intent = new Intent(this, PayPalService.class);
        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config);
        startService(intent);

        checkoutButt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Getting the amount from editText
                paymentAmount = totalAmountTxt.getText().toString();

                //Creating a PayPal payment
                PayPalPayment payment = new PayPalPayment(new BigDecimal(String.valueOf(paymentAmount)), Configs.MY_CURRENCY_CODE, Configs.MERCHANT_NAME + " purchase",
                        PayPalPayment.PAYMENT_INTENT_SALE);

                // Creating Paypal Payment activity intent
                Intent intent = new Intent(Cart.this, PaymentActivity.class);

                // Putting the Paypal configuration to the intent
                intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config);

                // Putting PayPal payment to the intent
                intent.putExtra(PaymentActivity.EXTRA_PAYMENT, payment);

                //Starting the intent activity for result, the request code will be used on the method onActivityResult()
                startActivityForResult(intent, PAYPAL_REQUEST_CODE);

        }});



        // Call query
        queryCart();


    }// end onCreate()







    // MARK: - QUERY CART ---------------------------------------------------------------------------
    void queryCart() {

        // Reset totalAmountArray
        totalAmountArray = new ArrayList<Double>();


        // USER IS NOT LOGGED IN
        if(ParseUser.getCurrentUser().getUsername() == null) {
            AlertDialog.Builder alert = new AlertDialog.Builder(Cart.this);
            alert.setMessage("You must login into your Account to add/see products in your Cart!")
                .setTitle(R.string.app_name)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startActivity(new Intent(Cart.this, Login.class));
                    }})
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                }})
                .setIcon(R.drawable.logo);
            alert.create().show();


        // USER IS LOGED IN!
        } else {
            Configs.showPD("Updating Cart...", Cart.this);

            ParseQuery query = ParseQuery.getQuery(Configs.CART_CLASS_NAME);
            query.whereEqualTo(Configs.CART_USER_POINTER, ParseUser.getCurrentUser());
            query.findInBackground(new FindCallback<ParseObject>() {
                public void done(List<ParseObject> objects, ParseException error) {
                    if (error == null) {
                        cartArray = objects;
                        Configs.hidePD();


                        // CALCULATE TOTAL AMOUNT
                        if (cartArray.size() != 0) {
                            calculateTotalAmount();

                            // Reset Checkout button
                            checkoutButt.setEnabled(true);
                            checkoutButt.setText("Checkout");

                        } else {
                            totalAmountTxt.setText("0.0");
                            totalAmount = 0.0;

                            checkoutButt.setText("No products in your cart");
                            checkoutButt.setEnabled(false);

                            Log.i("log-", "CART ARRAY: " + cartArray.size());
                        }



                        // CUSTOM LIST ADAPTER
                        class ListAdapter extends BaseAdapter {
                            private Context context;
                            public ListAdapter(Context context, List<ParseObject> objects) {
                                super();
                                this.context = context;
                            }


                            // CONFIGURE CELL
                            @Override
                            public View getView(final int position, View cell, ViewGroup parent) {
                                if (cell == null) {
                                    LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                                    cell = inflater.inflate(R.layout.cell_cart, null);
                                }

                                // Get Parse object
                                final ParseObject cartObj = cartArray.get(position);

                                // Get Product pointer
                                final View finalCell = cell;
                                cartObj.getParseObject(Configs.CART_PRODUCT_POINTER).fetchIfNeededInBackground(new GetCallback<ParseObject>() {
                                    public void done(ParseObject prodPointer, ParseException e) {

                                        // Get product name
                                        TextView nameTxt = (TextView) finalCell.findViewById(R.id.cartProdName);
                                        nameTxt.setText(prodPointer.getString(Configs.PRODUCTS_NAME));

                                        // Get currency
                                        TextView currTxt = (TextView) finalCell.findViewById(R.id.cartCurrencyTxt);
                                        currTxt.setText(prodPointer.getString(Configs.PRODUCTS_CURRENCY));

                                        // Get final price
                                        TextView priceTxt = (TextView) finalCell.findViewById(R.id.cartFinalPriceTxt);
                                        priceTxt.setText(String.valueOf(prodPointer.getNumber(Configs.PRODUCTS_FINAL_PRICE)) );

                                        // Get quantity
                                        final TextView qtyTxt = (TextView) finalCell.findViewById(R.id.cartQtyTxt);
                                        qtyTxt.setText(String.valueOf(cartObj.getNumber(Configs.CART_PRODUCT_QTY)) );


                                        // Get Image
                                        final ImageView anImage = (ImageView) finalCell.findViewById(R.id.cartProdImage);
                                        ParseFile fileObject = (ParseFile)prodPointer.get(Configs.PRODUCTS_IMAGE1);
                                        fileObject.getDataInBackground(new GetDataCallback() {
                                            public void done(byte[] data, ParseException error) {
                                                if (error == null) {
                                                    Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
                                                    if (bmp != null) {
                                                        anImage.setImageBitmap(bmp);
                                        }}}});







                                        // MARK: - MINUS BUTTON --------------
                                        Button minusButt = (Button)finalCell.findViewById(R.id.cartMinusButt);
                                        minusButt.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                int qtyNr = (int) cartObj.getNumber(Configs.CART_PRODUCT_QTY);

                                                // Prevent going under 1 qty
                                                if ( qtyNr != 1) {
                                                    qtyNr = qtyNr - 1;
                                                    qtyTxt.setText(String.valueOf(qtyNr));

                                                    // Reset the Checkout button
                                                    checkoutButt.setEnabled(false);
                                                    checkoutButt.setText("Hit refresh button to update Total");

                                                    // Update product Qty in Parse
                                                    cartObj.put(Configs.CART_PRODUCT_QTY, qtyNr);
                                                    cartObj.saveInBackground(new SaveCallback() {
                                                        @Override
                                                        public void done(ParseException e) {
                                                            if (e != null) {
                                                                Configs.simpleAlert(e.getMessage(), Cart.this);
                                                    }}});

                                                }// end IF

                                        }});



                                        // MARK: - PLUS BUTTON --------------
                                        Button plusButt = (Button)finalCell.findViewById(R.id.cartPlusButt);
                                        plusButt.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                int qtyNr = (int) cartObj.getNumber(Configs.CART_PRODUCT_QTY);
                                                qtyNr = qtyNr + 1;
                                                qtyTxt.setText(String.valueOf(qtyNr));

                                                // Reset the Checkout button
                                                checkoutButt.setEnabled(false);
                                                checkoutButt.setText("Hit refresh button to update Total");

                                                // Update product Qty in Parse
                                                cartObj.put(Configs.CART_PRODUCT_QTY, qtyNr);
                                                cartObj.saveInBackground(new SaveCallback() {
                                                    @Override
                                                    public void done(ParseException e) {
                                                        if (e != null) { Configs.simpleAlert(e.getMessage(), Cart.this);
                                                }}});

                                            }});




                                        // MARK: - ADD NOTE BUTTON --------------------------------------------
                                        final Button addNoteButt = (Button)finalCell.findViewById(R.id.cartAddNoteButt);
                                        addNoteButt.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                // Assing tag to this button
                                                noteIndex = position;

                                                // Show notes View
                                                showNotesView();
                                        }});


                                    }});//end prodPointer


                                 return cell;
                            }

                            @Override public int getCount() { return cartArray.size(); }
                            @Override public Object getItem(int position) { return cartArray.get(position); }
                            @Override public long getItemId(int position) { return position; }
                        }


                        // Init ListView and set its adapter
                        ListView aList = (ListView) findViewById(R.id.cartCartListView);
                        aList.setAdapter(new ListAdapter(Cart.this, cartArray));



                        // MARK: - LONG TAP TO REMOVE PRODUCT FROM CART ---------------------------
                        aList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                            @Override
                            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                                Configs.showPD("Removing Product...", Cart.this);

                                ParseObject cartObj = cartArray.get(position);
                                cartObj.deleteInBackground(new DeleteCallback() {
                                    @Override
                                    public void done(ParseException error) {
                                        if (error == null) {
                                            Configs.hidePD();

                                            // Recall query
                                            queryCart();

                                        // error
                                        } else {
                                            Configs.hidePD();
                                            Configs.simpleAlert(error.getMessage(), Cart.this);
                                }}});


                                return true;
                        }});



                    // Error in query
                    } else {
                        Configs.hidePD();
                        Configs.simpleAlert(error.getMessage(), Cart.this);
            }}});

        }


    }





    // MARK: - CALCULATE TOTAL AMOUNT ------------------------------------------------------------
    void calculateTotalAmount() {

        for (int i = 0; i<cartArray.size(); i++) {
            // Get Parse object
            final ParseObject cartObj = cartArray.get(i);

            // Get Product pointer
            cartObj.getParseObject(Configs.CART_PRODUCT_POINTER).fetchIfNeededInBackground(new GetCallback<ParseObject>() {
                public void done(ParseObject prodPointer, ParseException e) {

                    double totalSingleProdPrice = prodPointer.getDouble(Configs.PRODUCTS_FINAL_PRICE);
                    int qtyToMultiply = (int) cartObj.getNumber(Configs.CART_PRODUCT_QTY);
                    double multiplyPrice = totalSingleProdPrice * qtyToMultiply;
                    totalAmountArray.add(multiplyPrice);

                    // Format double number to 2 digits
                    DecimalFormat formattedDouble = new DecimalFormat("0.00");
                    totalAmountTxt.setText(formattedDouble.format(getTotalAmount()));
                    totalAmount = Double.parseDouble(totalAmountTxt.getText().toString());

                    currencyTxt.setText(prodPointer.getString(Configs.PRODUCTS_CURRENCY));

                    // CONSOLE LOG
                    Log.i("log-", "SINGLE PRICE: " + totalSingleProdPrice +
                            "\nQTY TO MULTIPLY: " + qtyToMultiply +
                            "\nMULTUPLY PRICE: " + multiplyPrice +
                            "\nTOTAL AMOUNT ARRAY: " + totalAmountArray +
                            "\n TOTAL AMOUNT: " + totalAmount);
                }});

        }// end FOR loop

    }




    // MARK: - GET TOTAL AMOUNT (double)
    public double getTotalAmount() {
        double sum = 0;
        for(int i = 0; i < totalAmountArray.size(); i++)  {
            sum = sum + totalAmountArray.get(i);
        }
        return sum;
    }







    // MARK: - RESULT FOR PAYMENT ----------------------------------------------------------------------
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PAYPAL_REQUEST_CODE) {

            //If the result is OK i.e. user has not canceled the payment
            if (resultCode == Activity.RESULT_OK) {
                // Getting the payment confirmation
                PaymentConfirmation confirm = data.getParcelableExtra(PaymentActivity.EXTRA_RESULT_CONFIRMATION);

                // Confirmation is not null
                if (confirm != null) {
                    try {
                        // Getting the payment details
                        final String proofOfPayment = confirm.toJSONObject().toString(4);
                        Log.i("PAYMENT PROOF: ", proofOfPayment);


                        // Show Alert
                        AlertDialog.Builder alert = new AlertDialog.Builder(Cart.this);
                        alert.setMessage("You've successfully paid your order with PayPal!\nWe'll ship your order asap!")
                                .setTitle(R.string.app_name)
                                .setCancelable(false)
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                        // Reset Cart
                                        queryCart();
                                    }})
                                .setIcon(R.drawable.logo);
                        alert.create().show();



                        // Empty itemsOrdered
                        itemsOrdered = new ArrayList<String>();

                        for (int i = 0; i < cartArray.size(); i++) {
                            final ParseObject cartObj = cartArray.get(i);

                            // Get product pointer
                            cartObj.getParseObject(Configs.CART_PRODUCT_POINTER).fetchIfNeededInBackground(new GetCallback<ParseObject>() {
                                public void done(final ParseObject prodPointer, ParseException e) {

                                    // Save Order
                                    ParseObject orderObj = new ParseObject(Configs.ORDERS_CLASS_NAME);
                                    orderObj.put(Configs.ORDERS_USER_POINTER, ParseUser.getCurrentUser());
                                    orderObj.put(Configs.ORDERS_USER_EMAIL, ParseUser.getCurrentUser().getEmail());
                                    orderObj.put(Configs.ORDERS_USER_USERNAME, ParseUser.getCurrentUser().getUsername());
                                    orderObj.put(Configs.ORDERS_PAYMENT_PROOF, proofOfPayment);
                                    orderObj.put(Configs.ORDERS_PRODUCT_POINTER, prodPointer);
                                    if (cartObj.getString(Configs.CART_NOTES) != null) {
                                        orderObj.put(Configs.ORDERS_NOTES, cartObj.getString(Configs.CART_NOTES));
                                    }
                                    final int qty = (int) cartObj.getNumber(Configs.CART_PRODUCT_QTY);
                                    orderObj.put(Configs.ORDERS_PRODUCT_QTY, qty);
                                    orderObj.saveInBackground(new SaveCallback() {
                                        @Override
                                        public void done(ParseException e) {
                                            if (e == null) {
                                                Log.i("log-", "ORDER SAVED!");

                                                // Compose string for the Product details
                                                String aProduct = "";
                                                if (cartObj.getString(Configs.CART_NOTES) != null) {
                                                    aProduct = prodPointer.getString(Configs.PRODUCTS_NAME) +
                                                            "  QTY: " + String.valueOf(qty) +
                                                            " | NOTE: " + cartObj.getString(Configs.CART_NOTES) +
                                                            "\n\n||||";
                                                } else {
                                                    aProduct = prodPointer.getString(Configs.PRODUCTS_NAME) +
                                                            "  QTY: " + String.valueOf(qty) +
                                                            "\n\n||||";
                                                }

                                                // Add item to itemsOrdered array
                                                itemsOrdered.add(aProduct);


                                                // Remove Product from the Cart
                                                cartObj.deleteInBackground(new DeleteCallback() {
                                                    @Override
                                                    public void done(ParseException e) {
                                                        if (e == null) {
                                                            cartArray = null;
                                                            Log.i("log-", "Product removed from Cart!");

                                                        // error in removing product from Cart
                                                        } else {
                                                            Configs.hidePD();
                                                            Configs.simpleAlert(e.getMessage(), Cart.this);
                                                        }
                                                    }
                                                });


                                            // error in Saving Order
                                            } else {
                                                Configs.hidePD();
                                                Configs.showPD(e.getMessage(), Cart.this);
                                    }}});


                                }});// end prodPointer


                        } // end for loop



                        // Call method to send an email to Buyer
                        new Timer().schedule(new TimerTask() {
                            @Override
                            public void run() {
                                sendEmailToBuyer();
                            }
                        }, 3000); // 3 seconds



                    } catch (JSONException e) {
                        Log.i("paymentExample", "an extremely unlikely failure occurred: ", e);
                    }
                }

            } else if (resultCode == Activity.RESULT_CANCELED) {
                Log.i("paymentExample", "The user canceled.");
            } else if (resultCode == PaymentActivity.RESULT_EXTRAS_INVALID) {
                Log.i("paymentExample", "An invalid Payment or PayPalConfiguration was submitted. Please see the docs.");
            }
        }
    }





    // MARK: - SEND EMAIL TO ADMIN ------------------------------------------------------------------
    void sendEmailToAdmin() {

        // Make String out of the itemsOrderes array
        StringBuilder forMessage = new StringBuilder();
        for (String s:itemsOrdered) {
            forMessage.append(s + "\n");
        }
        String itemsStr = forMessage.toString();

        String message = "PRODUCTS ORDERED:\n" + itemsStr + "\n\nTOTAL: " + currencyTxt.getText().toString() + String.valueOf(totalAmount);
        String receiverEmail = ParseUser.getCurrentUser().getEmail();

        String userShippingAddress = ParseUser.getCurrentUser().getString(Configs.USER_SHIPPING_ADDRESS);
        if (userShippingAddress == null) { userShippingAddress = "N/A"; }

        String strURL =
                Configs.PATH_TO_PHP_FILE + "adminMessage.php?name="
                        + ParseUser.getCurrentUser().getUsername()
                        + "&fromEmail=" + receiverEmail
                        + "&messageBody=" + message
                        + "&receiverEmail=" +  Configs.ADMIN_EMAIL_ADDRESS
                        + "&storeName=" + Configs.MERCHANT_NAME
                        + "&shippingAddress=" + userShippingAddress;

        strURL = strURL.replace(" ", "%20");
        strURL = strURL.replace("\n", "%20");
        Log.d("PHP STRING - ADMIN: ", "\n" + strURL + "\n" );

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        try {
            URL url;
            url = new URL(strURL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            if( conn.getResponseCode() == HttpURLConnection.HTTP_OK ){
                InputStream is = conn.getInputStream();

                Log.i("log-", "EMAIL TO ADMIN SENT!");

            } else {
                InputStream err = conn.getErrorStream();
                // error may have useful information.. but could be null see javadocs for more information
            }
        } catch (IOException e) {e.printStackTrace(); }

    }






    // MARK: - SEND EMAIL TO THE BUYER ------------------------------------------------------------------
    void sendEmailToBuyer() {

        // Make String out of the itemsOrderes array
        StringBuilder forMessage = new StringBuilder();
        for (String s:itemsOrdered) {
            forMessage.append(s + "\n");
        }
        String itemsStr = forMessage.toString();

        String message = "PRODUCTS ORDERED:\n" + itemsStr + "\n\nTOTAL: " + currencyTxt.getText().toString() + String.valueOf(totalAmount);
        String receiverEmail = ParseUser.getCurrentUser().getEmail();
        Log.i("log-", "MESSAGE: \n" + message);

        String userShippingAddress = ParseUser.getCurrentUser().getString(Configs.USER_SHIPPING_ADDRESS);
        if (userShippingAddress == null) { userShippingAddress = "N/A"; }

        String strURL =
                Configs.PATH_TO_PHP_FILE + "buyerMessage.php?name="
                        + ParseUser.getCurrentUser().getUsername()
                        + "&fromEmail=" + Configs.ADMIN_EMAIL_ADDRESS
                        + "&messageBody=" + message
                        + "&receiverEmail=" +  receiverEmail
                        + "&storeName=" + Configs.MERCHANT_NAME
                        + "&shippingAddress=" + userShippingAddress;

        strURL = strURL.replace(" ", "%20");
        strURL = strURL.replace("\n", "%20");
        Log.i("log-", "PHP STRING - BUYER: " + "\n" + strURL + "\n" );


        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        try {
            URL url;
            url = new URL(strURL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            if( conn.getResponseCode() == HttpURLConnection.HTTP_OK ){
                InputStream is = conn.getInputStream();

                Log.i("log-", "EMAIL TO BUYER SENT!");
                sendEmailToAdmin();

            } else {
                InputStream err = conn.getErrorStream();
                // error may have useful information.. but could be null see javadocs for more information
            }
        } catch (IOException e) {e.printStackTrace(); }


    }



    // onDestroy()
    @Override
    public void onDestroy() {
        stopService(new Intent(this, PayPalService.class));
        super.onDestroy();
    }






    // MARK: - SHOW/HIDE NOTES VIEW ---------------------------------------------------------------
    void showNotesView() {
        noteTxt.setText("");
        notesLayout = (RelativeLayout) findViewById(R.id.cartNotesLayout);
        ViewGroup.MarginLayoutParams marginParams = new ViewGroup.MarginLayoutParams(notesLayout.getLayoutParams());
        marginParams.setMargins(0, 0, 0, 0);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(marginParams);
        notesLayout.setLayoutParams(layoutParams);
    }

    void hideNotesView() {
        notesLayout = (RelativeLayout) findViewById(R.id.cartNotesLayout);
        ViewGroup.MarginLayoutParams marginParams = new ViewGroup.MarginLayoutParams(notesLayout.getLayoutParams());
        marginParams.setMargins(0, 2000, 0, 0);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(marginParams);
        notesLayout.setLayoutParams(layoutParams);
    }





    // MARK: - DISMISS KEYBOARD
    public void dismisskeyboard() {
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(noteTxt.getWindowToken(), 0);
    }


    // MENU BUTTON ON ACTION BAR --------------------------------------------------------------------
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
                queryCart();
                return true;

        }
        return (super.onOptionsItemSelected(menuItem));
    }

}//@end
