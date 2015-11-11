package victor.smartlockapp;

import android.app.Application;

public class MyApplication extends Application{

    private String DEVICE_ADDRESS_1 = null;
    private String DEVICE_ADDRESS_2 = null;
    private String DEVICE_ADDRESS_3 = null;

    /******** Global manipulation of device address 1 ********/
    public String getDEVICE_ADDRESS_1() {
        return DEVICE_ADDRESS_1;
    }

    public void setDEVICE_ADDRESS_1(String newAddress) {
        this.DEVICE_ADDRESS_1 = newAddress;
    }

    /******** Global manipulation of device address 2 ********/
    public String getDEVICE_ADDRESS_2() {
        return DEVICE_ADDRESS_2;
    }

    public void setDEVICE_ADDRESS_2(String newAddress) {
        this.DEVICE_ADDRESS_2 = newAddress;
    }

    /******** Global manipulation of device address 3 ********/
    public String getDEVICE_ADDRESS_3() {
        return DEVICE_ADDRESS_3;
    }

    public void setDEVICE_ADDRESS_3(String newAddress) {
        this.DEVICE_ADDRESS_3 = newAddress;
    }
}
