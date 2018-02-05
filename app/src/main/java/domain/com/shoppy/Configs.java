package domain.com.shoppy;

/*-----------------------------------

    - Shoppy -

    Created by cubycode @2017
    All Rigths reserved

--------------------------------------*/

import android.app.AlertDialog;
import android.app.Application;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.parse.Parse;
import com.parse.ParseFacebookUtils;
import com.parse.ParseUser;


public class Configs extends Application {


    // IMPORTANT: Change the red string below with the path where you've stored the php files to send emails (in this case we've stored it into a directory in our website called "shoppy")
    public static String PATH_TO_PHP_FILE = "http://cubycode.com/shoppy/";



    // IMPORTANT: REPLACE THE KEYS BELOW WITH YOUR OWN ONES YOU'LL GET ON YOUR PAYPAL DEVELOPER DASHBOARD AT: https://developer.paypal.com/developer/applications/
    public static String PAYPAL_ENVIRONMENT_SANDBOX_KEY = "AcJS37_ZNSZHJbwPt16XTbiaucamy9_MI-BSBLJLyhVCiPo9zxhONd5XiOYbMdS4isw0oWV2Gj3KesyW";
    public static String PAYPAL_ENVIRONMENT_LIVE_KEY =  "AVKj_dqHAwdJdf7auXv_r7cIErQIaBTvldSE07SVlQ3MEc5xOy_XaEwkDRuc3PKIdIZU1Fs4jT-m89Z5";



    // IF YOUR STORE IS NOT LOCATED IN THE U.S. AND YOU'LL USE ANOTHER CURRENCY, REPLACE "USD" WITH YOUR OWN CURRENCY CODE
    // (this is only needed for PayPal SDK)
    public static String MY_CURRENCY_CODE = "USD";


    // REPLACE THE NAME BELOW WITH THE ONE OF YOUR STORE/COMPANY
    public static String MERCHANT_NAME = "Shoppy Inc.";


    // IMPORTANT: Replace the email address below with the one you'll use to receive Order emails from buyers
    public static String ADMIN_EMAIL_ADDRESS = "orders@gmydomain.com";


    // IMPORTANT: Replace the email address below with the one you'll dedicate to users to contact you
    public static String CONTACT_US_EMAIL_ADDRESS = "info@shoppy.com";




    // PARSE KEYS ----------------------------------------------------------------------------
    public static String PARSE_APP_KEY = "bJNa7YyMBfCQt4u6tcRahzJrpyRRtYXW6yH522ME";
    public static String PARSE_CLIENT_KEY = "36d3DWaQ6mxxhWwPU2JMWGPlwr6uRbDQnXyGCzMq";





    // YOU CAN EDIT THE HEX VALUES OF ALL THESE COLORS AS YOU WISH
    public static String[] colorsArray = {
            "#ed5564",   // HATS category
            "#fa6e52",   // BAGS category
            "#ffcf55",   // EYEWEAR category
            "#a0d468",   // T-SHIRTS category
            "#48cfae",   // SHOES category
            "#4fc0e8",   // JEWELS category
            "#5d9bec",
            "#ac92ed",
            "#ec88c0",
            "#da4553",
            "#ed5564",
            "#fa6e52",
            "#ffcf55",
            "#a0d468",
            "#48cfae",
    };












    /************** DO NOT EDIT THE CODE BELOW! **************/

    /* USER CLASS */
    public static String USER_CLASS_NAME = "User";
    public static String USER_USERNAME = "username";
    public static String USER_FULLNAME = "fullName";
    public static String USER_EMAIL = "email";
    public static String USER_SHIPPING_ADDRESS = "shippingAddress";

    public static String CATEGORIES_CLASS_NAME = "Categories";
    public static String CATEGORIES_CATEGORY = "category";
    public static String CATEGORIES_IMAGE = "image";

    public static String PRODUCTS_CLASS_NAME = "Products";
    public static String PRODUCTS_CATEGORY = "category";
    public static String PRODUCTS_NAME = "name";
    public static String PRODUCTS_IMAGE1 = "image1";
    public static String PRODUCTS_IMAGE2 = "image2";
    public static String PRODUCTS_IMAGE3 = "image3";
    public static String PRODUCTS_IMAGE4 = "image4";
    public static String PRODUCTS_FINAL_PRICE = "finalPrice";
    public static String PRODUCTS_BIG_PRICE = "bigPrice";
    public static String PRODUCTS_CURRENCY = "currency";
    public static String PRODUCTS_DESCRIPTION = "description";

    public static String CART_CLASS_NAME = "Cart";
    public static String CART_TOTAL_AMOUNT = "totalAmount";
    public static String CART_PRODUCT_POINTER = "productPointer";
    public static String CART_PRODUCT_QTY = "qty";
    public static String CART_USER_POINTER = "userPointer";
    public static String CART_NOTES = "notes";

    public static String WISHLIST_CLASS_NAME = "Wishlist";
    public static String WISHLIST_USER_POINTER = "userPointer";
    public static String WISHLIST_PRODUCT_POINTER = "prodPointer";

    public static String ORDERS_CLASS_NAME = "Orders";
    public static String ORDERS_USER_EMAIL = "userEmail";
    public static String ORDERS_USER_POINTER = "userPointer";
    public static String ORDERS_USER_USERNAME = "userUsername";
    public static String ORDERS_PAYMENT_PROOF = "paymentProof";
    public static String ORDERS_PRODUCT_POINTER = "prodPointer";
    public static String ORDERS_PRODUCT_QTY = "qty";
    public static String ORDERS_NOTES = "notes";
    public static String ORDERS_CREATED_AT = "createdAt";




    boolean isParseInitialized = false;

    public void onCreate() {
        super.onCreate();

        if (!isParseInitialized) {
            Parse.initialize(new Parse.Configuration.Builder(this)
                    .applicationId(String.valueOf(PARSE_APP_KEY))
                    .clientKey(String.valueOf(PARSE_CLIENT_KEY))
                    .server("https://parseapi.back4app.com")
                    .build()
            );
            Parse.setLogLevel(Parse.LOG_LEVEL_VERBOSE);
            ParseUser.enableAutomaticUser();
            isParseInitialized = true;

            // Init Facebook Utils
            ParseFacebookUtils.initialize(this);

        }


    }// end onCreate()



    // MARK: - CUSTOM PROGRESS DIALOG -----------
    public static AlertDialog pd;
    public static void showPD(String mess, Context ctx) {
        AlertDialog.Builder db = new AlertDialog.Builder(ctx);
        LayoutInflater inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View dialogView = inflater.inflate(R.layout.pd, null);
        TextView messTxt = dialogView.findViewById(R.id.pdMessTxt);
        messTxt.setText(mess);
        db.setView(dialogView);
        db.setCancelable(true);
        pd = db.create();
        pd.show();
    }

    public static void hidePD(){ pd.dismiss(); }




    // SIMPLE ALERT
    public static  void simpleAlert(String mess, Context ctx) {
        AlertDialog.Builder alert = new AlertDialog.Builder(ctx);
        alert.setMessage(mess)
            .setTitle(R.string.app_name)
            .setPositiveButton("OK", null)
            .setIcon(R.drawable.logo);
        alert.create().show();
    }



}//@end
