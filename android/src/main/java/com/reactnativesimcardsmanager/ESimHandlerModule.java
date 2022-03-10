package com.reactnativesimcardsmanager;
import static android.content.Context.EUICC_SERVICE;

import android.os.Build;
import android.telephony.euicc.EuiccManager;
import com.facebook.react.bridge.*;
import android.app.PendingIntent;
import android.content.Intent;
import android.telephony.euicc.DownloadableSubscription;
import android.content.IntentFilter;
import android.content.Context;
import android.content.BroadcastReceiver;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.module.annotations.ReactModule;

@ReactModule(name = ESimHandlerModule.NAME)
public class ESimHandlerModule extends ReactContextBaseJavaModule {
    public static final String NAME = "ESimHandler";
    private String ACTION_DOWNLOAD_SUBSCRIPTION = "download_subscription";
    private ReactContext mReactContext;

    @RequiresApi(api = Build.VERSION_CODES.P)
    private EuiccManager mgr = (EuiccManager)mReactContext.getSystemService(EUICC_SERVICE);

    public ESimHandlerModule(ReactApplicationContext reactContext) {
        super(reactContext);
        mReactContext = reactContext;
    }

    @Override
    @NonNull
    public String getName() {
        return NAME;
    }


  @RequiresApi(api = Build.VERSION_CODES.P)
  @ReactMethod
  public void isEsimSupported(Promise promise) {
    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && mgr != null ) {
      promise.resolve(mgr.isEnabled());
    } else {
      promise.resolve(false);
    }
    return;
  }

  @RequiresApi(api = Build.VERSION_CODES.P)
  @ReactMethod
  public void setupEsim(ReadableMap config, Promise promise) {
    if (mgr == null){
      // Could not get react context";
      promise.resolve(0);
      return;
    }

    BroadcastReceiver receiver = new BroadcastReceiver() {

      @Override
      public void onReceive(Context context, Intent intent) {
        if (!ACTION_DOWNLOAD_SUBSCRIPTION.equals(intent.getAction())) {
          promise.resolve(0);
          return;
        }
        int resultCode = getResultCode();
        if (resultCode == EuiccManager.EMBEDDED_SUBSCRIPTION_RESULT_OK){
          promise.resolve(2);
        } else if (resultCode == EuiccManager.EMBEDDED_SUBSCRIPTION_RESULT_ERROR){
          // Embedded Subscription Error
          promise.resolve(1);
        } else {
          // Unknown Error
          promise.resolve(0);
        }
      }
    };

    mReactContext.registerReceiver(
      receiver,
      new IntentFilter(ACTION_DOWNLOAD_SUBSCRIPTION),
      null,
      null);

    DownloadableSubscription sub = DownloadableSubscription.forActivationCode(
      /* Passed from react side */
      config.getString("confirmationCode")
    );

    PendingIntent callbackIntent = PendingIntent.getBroadcast(
      mReactContext,
      0,
      new Intent(ACTION_DOWNLOAD_SUBSCRIPTION),
      PendingIntent.FLAG_UPDATE_CURRENT);

    mgr.downloadSubscription(sub, true, callbackIntent);
  }
}
