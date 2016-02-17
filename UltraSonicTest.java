package org.usfirst.FTC5866.opmodes;


/**
 * Created by Olavi Kamppari on 10/29/2015.
 */

import android.util.Log;

import org.usfirst.FTC5866.library.*;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;

public class UltraSonicTest extends OpMode {
    private DataLogger              dl;
    private Wire                    ds;                     // Distance Sensor
    private int                     readCount = 0;
    private int                     distance;
    private long                    pingTime;

    public void init() {
        dl          = new DataLogger("US Test");
        ds          = new Wire(hardwareMap,"port3",0xE0);

        dl.addField("Micros");      // Sensor reading time in microseconds
        dl.addField("Dist");
        dl.newLine();
    }

    public void start() {
        state   = States.SEND_PING;
    }

    private enum States{SEND_PING,ACK_PING,WAIT_REPLY,READ_RESPONSE};

    private States state;

    @Override
    public void loop() {
        switch (state) {
            case SEND_PING:
                ds.write(0x51, null);                                    // Send ping
                state = States.ACK_PING;
                break;
            case ACK_PING:
                if (ds.responseCount() > 0) {
                    ds.getResponse();
                    if (ds.isWrite()) {
                        pingTime = System.currentTimeMillis();
                        state = States.WAIT_REPLY;
                    }
                }
                break;
            case WAIT_REPLY:
                if ((System.currentTimeMillis() - pingTime) > 100) {
                    ds.requestFrom(0, 2);                        // Request response
                    state = States.READ_RESPONSE;
                }
                break;
            case READ_RESPONSE:
                if (ds.responseCount() > 0) {
                    ds.getResponse();
                    if (ds.isRead()) {
                        long micros = ds.micros();
                        distance = ds.readHL();
                        if (distance < 760) {
                            dl.addField(micros);
                            dl.addField(distance);
                            dl.newLine();

                            readCount++;
                            telemetry.addData("Count", readCount);
                            telemetry.addData("Time", micros / 1000);
                            telemetry.addData("cm", distance);
                        }
                        state = States.SEND_PING;
                    }
                }
                break;
        }
    }

    public void stop() {
        dl.closeDataLogger();
        ds.close();
    }
}
