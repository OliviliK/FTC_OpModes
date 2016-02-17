package org.usfirst.FTC5866.opmodes;

/**
 * Created by Olavi Kamppari on 11/3/2015.
 */

/**
 * Demonstrate the simultaneous usage of 3 parallel data loggers
 * working on different timings. Combine the result in Excel for single time range.
 *
 */
import android.util.Log;

import org.usfirst.FTC5866.library.*;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;

public class DlDemo extends OpMode {
    private DataLogger              csDl;               // cs = Color Sensor
    private Wire                    csW;
    private DataLogger              dsDl;               // ds = Distance Sensor
    private Wire                    dsW;
    private DataLogger              ltDl;               // Linear Time Data Logger
    private int                     csReadCount = 0;
    private long                    csTimeStamp;        // In microseconds
    private int                     clear, red, green, blue;
    private int                     dsReadCount = 0;
    private long                    dsTimeStamp;        // In microseconds
    private int                     distance;
    private long                    pingTime;

    public void init() {
        initColorSensor();
        initDistanceSensor();

        ltDl        = new DataLogger("DlDemo_lt");
        ltDl.addField("Clr");
        ltDl.addField("Red");
        ltDl.addField("Blue");
        ltDl.addField("Green");
        ltDl.addField("Dist");
        ltDl.newLine();
    }

    private void initColorSensor() {
        csDl        = new DataLogger("DlDemo_cs");

        csDl.addField("Micros");    // Sensor reading time in microseconds
        csDl.addField("Clr");
        csDl.addField("Red");
        csDl.addField("Blue");
        csDl.addField("Green");
        csDl.newLine();

        csW = new Wire(hardwareMap,"Color",2*0x29);

        csW.write(0x80, 0x03);      // R[00] = 3    to enable power
        csW.requestFrom(0x92, 1);   // R[12]        is the device ID
        csW.write(0x8F, 0x02);      // R[0F] = 2    to set gain 16
        csW.write(0x81, 0xEC);      // R[01] = EC   to set integration time to 20* 2.4 ms
                                    // 256 - 20 = 236 = 0xEC
    }

    private void initDistanceSensor() {
        dsDl            = new DataLogger("DlDemo_ds");
        dsW             = new Wire(hardwareMap,"port3",0xE0);

        dsDl.addField("Micros");    // Sensor reading time in microseconds
        dsDl.addField("Dist");
        dsDl.newLine();
    }

    public  void init_loop() {
        colorSensorInitLoop();
    }

    private void colorSensorInitLoop() {
        if (csW.responseCount() > 0) {
            csW.getResponse();
            int regNumber = csW.registerNumber();
            if (csW.isRead()) {
                int regValue    = csW.read();
                telemetry.addData("Read  " + regNumber, regValue);
                Log.i("GST init", String.format("Read reg 0x%02X = 0x%02X", regNumber, regValue));
            }
            if (csW.isWrite()) {
                int regValue    = csW.read();
                telemetry.addData("Write " + regNumber, regValue);
                Log.i("GST init",String.format("Write reg 0x%02X = 0x%02X",regNumber,regValue));
            }
        }
    }
    
    public void start() {
        startColorSensor();
        startDistanceSensor();
    }

    private void startColorSensor() {
        csW.requestFrom(0x93, 1);               // Get sensor status
    }

    private void startDistanceSensor() {
        dsW.beginWrite(0x51);
        dsW.write(0);
        dsW.endWrite();                         // Send ping
        pingTime    = System.currentTimeMillis();
    }

    @Override
    public void loop() {
        if (isColorUpdate()) {
            csDl.addField(csTimeStamp/1e6);
            csDl.addField(clear);
            csDl.addField(red);
            csDl.addField(green);
            csDl.addField(blue);
            csDl.newLine();

            csReadCount++;
            telemetry.addData("Count",  "CS:" + csReadCount + " DS: " +dsReadCount);
            telemetry.addData("Time",   csTimeStamp/1e6);
            telemetry.addData("Colors", "C:"+ clear +
                    " R:" + red +
                    " G:" + green +
                    " B:" + blue);
        }

        if (isDistanceUpdate()) {
            dsDl.addField(dsTimeStamp/1e6);
            dsDl.addField(distance);
            dsDl.newLine();

            dsReadCount++;
            telemetry.addData("Count",  "CS:" + csReadCount + " DS: " +dsReadCount);
            telemetry.addData("Time", dsTimeStamp/1e6);
            telemetry.addData("cm", distance);
        }

        ltDl.addField(clear);
        ltDl.addField(red);
        ltDl.addField(blue);
        ltDl.addField(green);
        ltDl.addField(distance);
        ltDl.newLine();
    }

    public void stop() {
        dsDl.closeDataLogger();
        csDl.closeDataLogger();
        ltDl.closeDataLogger();
        csW.close();
        dsW.close();
    }

    private boolean isColorUpdate() {
        boolean isNew = false;
        if (csW.responseCount() > 0) {
            csW.getResponse();
            int regNumber = csW.registerNumber();
            if (csW.isRead()) {
                int regCount = csW.available();
                switch (regNumber) {
                    case 0x93:
                        if (regCount == 1) {
                            int status = csW.read();
                            if ((status & 1) != 0) {
                                csW.requestFrom(0x94,8);             // Get colors
                            } else {
                                csW.requestFrom(0x93,1);             // Keep polling
                            }
                        } else {
                            telemetry.addData("Error", regNumber + " length 1 != " + regCount);
                            Log.i("GST", String.format("ERROR reg 0x%02X Len = 0x%02X (!= 1)",
                                    regNumber, regCount));
                        }
                        break;
                    case 0x94:
                        csW.requestFrom(0x93,1);                     // Keep polling
                        if (regCount == 8) {                        // Check register count
                            csTimeStamp = csW.micros();              // Reading time
                            clear       = csW.readLH();              // Clear color
                            red         = csW.readLH();              // Red color
                            green       = csW.readLH();              // Green color
                            blue        = csW.readLH();              // Blue color
                            isNew       = true;
                        } else {
                            telemetry.addData("Error", regNumber + " length 8 != " + regCount);
                            Log.i("GST", String.format("ERROR reg 0x%02X Len = 0x%02X (!= 8)",
                                    regNumber, regCount));
                        }
                        break;
                    default:
                        telemetry.addData("Error", "Unexpected register " + regNumber);
                        break;
                }
            }
        }
        return isNew;
    }
    
    private boolean isDistanceUpdate() {
        boolean isNew = false;
        if ((System.currentTimeMillis() - pingTime) > 100 ) {
            dsW.requestFrom(0,2);
            dsW.beginWrite(0x51);                                // Request response
            dsW.write(0);
            dsW.endWrite();                                      // Send ping
            pingTime    = System.currentTimeMillis();
        }

        if (dsW.responseCount() > 0) {
            dsW.getResponse();
            if (dsW.isRead()) {
                dsTimeStamp     = dsW.micros();
                distance        = dsW.readHL();
                if (distance < 760) isNew = true;
            }
        }

        return isNew;
    }
}
