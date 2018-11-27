package nain.himanshu.paytmintegration;

import android.Manifest;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.paytm.pgsdk.PaytmOrder;
import com.paytm.pgsdk.PaytmPGService;
import com.paytm.pgsdk.PaytmPaymentTransactionCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        AndroidNetworking.initialize(this);

        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_SMS, Manifest.permission.RECEIVE_SMS}, 101);
        }
        preparePaymentOptions();
    }

    private void preparePaymentOptions()
    {
        //ORDER ID GENERATE SELF
        String orderId = "order2";

        //SAME AS USER ID FOR OUR APP
        String userId = "ownvwevbvn";

        //Test
        String CallbackUrl = "https://securegw-stage.paytm.in/theia/paytmCallback?ORDER_ID="+orderId;

        //Production
        //String CallbackUrl = "https://securegw.paytm.in/theia/paytmCallback?ORDER_ID="+orderId;

        String amount = "100";

        final HashMap<String, String> paramMap = new HashMap<>();

        paramMap.put( "MID" , getString(R.string.paytm_test_MID));
        paramMap.put( "ORDER_ID" , orderId);
        paramMap.put( "CUST_ID" , userId);

        //Customer phone no. (OPTIONAL) Only for faster login
        //paramMap.put( "MOBILE_NO" , "7042856750");

        //Customer email (OPTIONAL) Only for faster login
        //paramMap.put( "EMAIL" , "username@emailprovider.com");

        paramMap.put( "CHANNEL_ID" , "WAP");
        paramMap.put( "TXN_AMOUNT" , amount);
        paramMap.put( "WEBSITE" , "WEBSTAGING");
        paramMap.put( "INDUSTRY_TYPE_ID" , "Retail");
        paramMap.put( "CALLBACK_URL", CallbackUrl);

        AndroidNetworking.post(Config.CHECKSUM_GENERATION)
                .setPriority(Priority.MEDIUM)
                .addJSONObjectBody(new JSONObject(paramMap))
                .setTag("CHECKSUM GENERATION")
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            if(response.getBoolean("success")){

                                JSONObject cObj = response.getJSONObject("checksum");
                                paramMap.put( "CHECKSUMHASH" , cObj.getString("CHECKSUMHASH"));

                                startOrder(paramMap);

                            }else {

                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        Toast.makeText(getApplicationContext(), anError.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void startOrder(HashMap<String,String> paramMap) {
        PaytmPGService Service = PaytmPGService.getStagingService();

        //production
        //PaytmPGService Service = PaytmPGService.getProductionService();

        PaytmOrder Order = new PaytmOrder(paramMap);

        Service.initialize(Order, null);
        Service.startPaymentTransaction(this, true, true, new PaytmPaymentTransactionCallback() {
            @Override
            public void onTransactionResponse(Bundle inResponse) {


                Log.i("TRANSACTION MESSAGE", inResponse.toString());
                Toast.makeText(getApplicationContext(), "Payment Transaction response " + inResponse.toString(), Toast.LENGTH_LONG).show();

            }

            @Override
            public void networkNotAvailable() {
                Toast.makeText(getApplicationContext(), "Network connection error: Check your internet connectivity", Toast.LENGTH_LONG).show();
            }

            @Override
            public void clientAuthenticationFailed(String inErrorMessage) {
                Toast.makeText(getApplicationContext(), "Authentication failed: Server error" + inErrorMessage, Toast.LENGTH_LONG).show();
            }

            @Override
            public void someUIErrorOccurred(String inErrorMessage) {
                Toast.makeText(getApplicationContext(), "UI Error " + inErrorMessage , Toast.LENGTH_LONG).show();
            }

            @Override
            public void onErrorLoadingWebPage(int iniErrorCode, String inErrorMessage, String inFailingUrl) {
                Toast.makeText(getApplicationContext(), "Unable to load webpage " + inErrorMessage, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onBackPressedCancelTransaction() {
                Toast.makeText(getApplicationContext(), "Transaction cancelled" , Toast.LENGTH_LONG).show();
            }

            @Override
            public void onTransactionCancel(String inErrorMessage, Bundle inResponse) {
                Toast.makeText(getApplicationContext(), "Transaction Cancelled" + inResponse.toString(), Toast.LENGTH_LONG).show();
            }
        });

    }

}
