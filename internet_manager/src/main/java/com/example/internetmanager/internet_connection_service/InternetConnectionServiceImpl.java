package com.example.internetmanager.internet_connection_service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import io.reactivex.Observable;
import io.reactivex.subjects.BehaviorSubject;

public class InternetConnectionServiceImpl implements InternetConnectionService {
    private static final String TAG = "InternetConnectionServiceImpl";
    private final BehaviorSubject<Boolean> connectionSubject;
    private final Context context;
    private BroadcastReceiver internetConnectionBroadcast = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getExtras() != null) {
                NetworkInfo networkInfo = (NetworkInfo) intent.getExtras().get(ConnectivityManager.EXTRA_NETWORK_INFO);
                if (networkInfo != null && networkInfo.getState() == NetworkInfo.State.CONNECTED) {
                    connectionSubject.onNext(true);
                } else if (intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, Boolean.FALSE)) {
                    connectionSubject.onNext(false);
                }
            }
        }
    };

    public InternetConnectionServiceImpl(Context context) {
        this.context = context;
        connectionSubject = BehaviorSubject.createDefault(checkConnection(context));
        registerInternetBroadcast(context);
    }

    private void registerInternetBroadcast(Context context) {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        intentFilter.addAction("android.net.wifi.WIFI_STATE_CHANGED");
        context.registerReceiver(internetConnectionBroadcast, intentFilter);
    }

    private boolean checkConnection(Context context) {
        ConnectivityManager connMgr =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        boolean isWifiConn = networkInfo.isConnected();
        networkInfo = connMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        boolean isMobileConn = networkInfo.isConnected();

        return isMobileConn || isWifiConn;
    }

    @Override
    public Observable<Boolean> hasConnection() {
        return connectionSubject;
    }

    @Override
    public void destroy() {
        context.unregisterReceiver(internetConnectionBroadcast);
    }
}
