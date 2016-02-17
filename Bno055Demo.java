package org.usfirst.FTC5866.opmodes;

/**
 * Created by Olavi Kamppari on 11/5/2015.
 */

/**
 * Demonstrate the simultaneous usage of 5 parallel data loggers
 * working on different timings. Combine the result in Excel for single time range.
 *
 */
import android.util.Log;

import org.usfirst.FTC5866.library.*;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;

public class Bno055Demo extends OpMode{

    Bno055      bno;                                // The bno055 sensor object
    DataLogger  dlCounts,dlSensor,dlFusion,         // Data loggers
                dlTemp,dlCalib;
    boolean     initComplete        = false;        // Flag to stop initialization

    Bno055.ScheduleItem     sensorData,fusionData,  // Data read schedules
                            tempData,calibData;

    public void init() {
        bno         = new Bno055(hardwareMap,"bno055");
        bno.init();
        
        dlCounts       = new DataLogger("bno Counts");
        dlCounts.addField("Request");
        dlCounts.addField("Response");
        dlCounts.addField("Read");
        dlCounts.addField("Write");
        dlCounts.newLine();

        dlSensor       = new DataLogger("bno Sensor");
        dlSensor.addField("tStamp");
        dlSensor.addField("AccX");
        dlSensor.addField("AccY");
        dlSensor.addField("AccZ");
        dlSensor.newLine();

        dlFusion       = new DataLogger("bno Fusion");
        dlFusion.addField("tStamp");
        dlFusion.addField("eulerX");
        dlFusion.addField("eulerY");
        dlFusion.addField("eulerZ");
        dlFusion.addField("quatW");
        dlFusion.addField("quatX");
        dlFusion.addField("quatY");
        dlFusion.addField("quatZ");
        dlFusion.addField("linAX");
        dlFusion.addField("linAY");
        dlFusion.addField("linAZ");
        dlFusion.addField("gravX");
        dlFusion.addField("gravY");
        dlFusion.addField("gravZ");
        dlFusion.newLine();
        
        dlTemp       = new DataLogger("bno Temp");
        dlTemp.addField("tStamp");
        dlTemp.addField("Temp");
        dlTemp.addField("CalSys");
        dlTemp.addField("CalAcc");
        dlTemp.addField("CalMag");
        dlTemp.addField("CalGyr");
        dlTemp.addField("Status");
        dlTemp.addField("Error");
        dlTemp.newLine();

        dlCalib       = new DataLogger("bno Calib");
        dlCalib.addField("tStamp");
        dlCalib.addField("GyrX");
        dlCalib.addField("GyrY");
        dlCalib.addField("GyrZ");
        dlCalib.addField("AccR");
        dlCalib.addField("MagR");
        dlCalib.newLine();
    }

    public void init_loop() {
        if (bno.isInitActive()) {
            bno.init_loop();
        } else if (!initComplete) {
            initComplete        = true;
            String      status  = bno.isInitDone()?"OK":"Failed";
            Log.i("BnoIL", "Initializtion " + status);
            telemetry.addData("Init", status);
        }
    }

    public void start() {
        sensorData  = bno.startSchedule(Bno055.BnoPolling.SENSOR, 100);     // 10 Hz
        fusionData  = bno.startSchedule(Bno055.BnoPolling.FUSION, 33);      // 30 Hz
        tempData    = bno.startSchedule(Bno055.BnoPolling.TEMP, 200);       // 5 Hz
        calibData   = bno.startSchedule(Bno055.BnoPolling.CALIB, 250);      // 4 Hz
    }

    @Override
    public void loop() {
        dlCounts.addField(bno.requestCount());
        dlCounts.addField(bno.responseCount());
        dlCounts.addField(bno.readCount());
        dlCounts.addField(bno.writeCount());
        dlCounts.newLine();

        bno.loop();

        if (sensorData.isChanged()) {
            dlSensor.addField(bno.sensorMicros() / 1e6);    // Convert the micros into seconds
            dlSensor.addField(bno.accX());
            dlSensor.addField(bno.accY());
            dlSensor.addField(bno.accZ());
            dlSensor.newLine();
        }
        if (fusionData.isChanged()) {
            dlFusion.addField(bno.fusionMicros() / 1e6);    // Convert the micros into seconds
            dlFusion.addField(bno.eulerX());
            dlFusion.addField(bno.eulerY());
            dlFusion.addField(bno.eulerZ());
            dlFusion.addField(bno.quaternionW());
            dlFusion.addField(bno.quaternionX());
            dlFusion.addField(bno.quaternionY());
            dlFusion.addField(bno.quaternionZ());
            dlFusion.addField(bno.linearAccelerationX());
            dlFusion.addField(bno.linearAccelerationY());
            dlFusion.addField(bno.linearAccelerationZ());
            dlFusion.addField(bno.gravityX());
            dlFusion.addField(bno.gravityY());
            dlFusion.addField(bno.gravityZ());
            dlFusion.newLine();
        }
        if (tempData.isChanged()) {
            dlTemp.addField(bno.tempMicros() / 1e6);        // Convert the micros into seconds
            dlTemp.addField(bno.temperature());
            dlTemp.addField(bno.sysCalibrationLevel());
            dlTemp.addField(bno.accCalibrationLevel());
            dlTemp.addField(bno.magCalibrationLevel());
            dlTemp.addField(bno.gyrCalibrationLevel());
            dlTemp.addField(bno.sysStatus());
            dlTemp.addField(bno.sysError());
            dlTemp.newLine();
        }
        if (calibData.isChanged()){
            dlCalib.addField(bno.calibMicros() / 1e6);      // Convert the micros into seconds
            dlCalib.addField(bno.gyrX());
            dlCalib.addField(bno.gyrY());
            dlCalib.addField(bno.gyrZ());
            dlCalib.addField(bno.accRadius());
            dlCalib.addField(bno.magRadius());
            dlCalib.newLine();

        }
        telemetry.addData("ReqC",bno.requestCount());
        telemetry.addData("RspC",bno.responseCount());
        telemetry.addData("RdC", bno.readCount());
    }

    public void stop() {
        bno.stop();
        dlCounts.closeDataLogger();
        dlSensor.closeDataLogger();
        dlFusion.closeDataLogger();
        dlTemp.closeDataLogger();
        dlCalib.closeDataLogger();
    }
}
