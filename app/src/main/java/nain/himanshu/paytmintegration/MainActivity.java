package nain.himanshu.paytmintegration;

import android.Manifest;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.paytm.pgsdk.PaytmOrder;
import com.paytm.pgsdk.PaytmPGService;
import com.paytm.pgsdk.PaytmPaymentTransactionCallback;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_SMS, Manifest.permission.RECEIVE_SMS}, 101);
        }
        initPaytmPG();
    }

    private void initPaytmPG()
    {

        PaytmPGService Service = PaytmPGService.getStagingService();

        //production
        //PaytmPGService Service = PaytmPGService.getProductionService();

        //ORDER ID GENERATE SELF
        String orderId = "order1";

        //SAME AS USER ID FOR OUR APP
        String userId = "ownvwevbvn";

        //Test
        String CallbackUrl = "https://securegw-stage.paytm.in/theia/paytmCallback?ORDER_ID="+orderId;

        //Production
        //String CallbackUrl = "https://securegw.paytm.in/theia/paytmCallback?ORDER_ID="+orderId;

        String amount = "100";

        // GENERATE THIS ON SERVER SIDE
        String paytmChecksum = "w2QDRMgp1234567JEAPCIOmNgQvsi+BhpqijfM9KvFfRiPmGSt3Ddzw+oTaGCLneJwxFFq5mqTMwJXdQE2EzK4px2xruDqKZjHupz9yXev4=";

        HashMap<String, String> paramMap = new HashMap<>();

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
        paramMap.put( "CHECKSUMHASH" , paytmChecksum);

        PaytmOrder Order = new PaytmOrder(paramMap);

        Service.initialize(Order, null);
        Service.startPaymentTransaction(this, true, true, new PaytmPaymentTransactionCallback() {
            @Override
            public void onTransactionResponse(Bundle inResponse) {

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
