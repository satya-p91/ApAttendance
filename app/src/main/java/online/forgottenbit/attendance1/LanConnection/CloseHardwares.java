package online.forgottenbit.attendance1.LanConnection;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.net.wifi.WifiManager;

public class CloseHardwares {

    private BluetoothAdapter bluetoothAdapter;
    private WifiManager wifiManager;
    private Context context;


    public CloseHardwares(Context context) {
        this.context = context;
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        wifiManager = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
    }

    public boolean isWifiEnabled(){
        if(wifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLED){
            return true;
        }else{
            return false;
        }
    }

    public boolean isBluetoothEnabled(){
        if(bluetoothAdapter.isEnabled()){
            return true;
        }else{
            return false;
        }
    }

    public void closeWifi(){
        wifiManager.setWifiEnabled(false);
    }

    public void closeBluetooth(){
        bluetoothAdapter.disable();
    }

}
