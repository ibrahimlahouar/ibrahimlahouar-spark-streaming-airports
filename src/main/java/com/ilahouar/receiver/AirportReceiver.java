package com.ilahouar.receiver;

import com.ilahouar.App;
import org.apache.spark.storage.StorageLevel;
import org.apache.spark.streaming.receiver.Receiver;


public class AirportReceiver extends Receiver<String> {

    private String apiUrl;

    public AirportReceiver(String apiUrl) {
        super(StorageLevel.MEMORY_AND_DISK_2());
        this.apiUrl = apiUrl;
    }

    @Override
    public void onStart() {
        new Thread(this::receive).start();
    }

    private void receive() {
        try {
            String airportsData = App.callApi(apiUrl);
            store(airportsData);
        } catch (Exception e) {
            restart("Error receiving data", e);
        }
    }

    @Override
    public void onStop() {
        // Do nothing
    }
}
