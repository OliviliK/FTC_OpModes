package org.usfirst.FTC5866.opmodes;

/**
 * Created by Ollie_2 on 9/27/2015.
 */

import org.usfirst.FTC5866.library.*;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;

public class ColorSensorTest extends OpMode {
    private DataLogger              dl;
    private TCS34725_ColorSensor    cs;

    private long                    phaseStart;
    private int                     gain    = 1;
    private int                     iTime   = 4;

    int     gainValue;
    double  timeValue;

    public void init() {
        dl          = new DataLogger("CS Test");
        cs          = new TCS34725_ColorSensor(hardwareMap,"port3");

        dl.addField("Gain");
        dl.addField("ITime");
        dl.addField("Clr");
        dl.addField("Red");
        dl.addField("Blue");
        dl.addField("Green");
        dl.addField("CCT");
        dl.newLine();
    }

    public void init_loop() {
        telemetry.addData("isIdOk", cs.isIdOk());
        telemetry.addData("CCT", cs.colorTemp());
    }

    public void start() {
        phaseStart  = System.currentTimeMillis();
        cs.setGain(gain);
        cs.setIntegrationTime(iTime);
        gainValue   = cs.getGain();
        timeValue   = cs.getIntegrationTime();
    }

    @Override
    public void loop() {
        dl.addField(gainValue);
        dl.addField(timeValue);
        dl.addField(cs.clearColor());
        dl.addField(cs.redColor());
        dl.addField(cs.blueColor());
        dl.addField(cs.greenrColor());
        dl.addField(cs.colorTemp());
        dl.newLine();
        if (System.currentTimeMillis() - phaseStart >= 1000) {
            phaseStart  = System.currentTimeMillis();
            telemetry.addData("gain", gain);
            telemetry.addData("iTime", iTime);
            telemetry.addData("Clear", cs.clearColor());
            telemetry.addData("CCT", cs.colorTemp());

            gain    *= 4;
            if (gain>64) {
                gain    = 1;
                iTime  *= 4;
                if (iTime > 1024) {
                    iTime   = 4;
                }
                cs.setIntegrationTime(iTime);
                timeValue   = cs.getIntegrationTime();
            }
            cs.setGain(gain);
            gainValue   = cs.getGain();
        }
    }

    public void stop() {
        dl.closeDataLogger();
        cs.close();
    }
}
