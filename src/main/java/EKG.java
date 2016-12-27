/**
 * Created by Jessica Laxa on 12/12/16.
 */

import java.io.IOException;
import java.text.DecimalFormat;

import com.pi4j.gpio.extension.ads.ADS1115GpioProvider;
import com.pi4j.gpio.extension.ads.ADS1115Pin;
import com.pi4j.gpio.extension.ads.ADS1x15GpioProvider.ProgrammableGainAmplifierValue;
import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinAnalogInput;
import com.pi4j.io.gpio.event.GpioPinAnalogValueChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerAnalog;
import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CFactory.UnsupportedBusNumberException;


public class EKG {
    public final DecimalFormat df;
    public final DecimalFormat pdf;
    String values;

    public EKG() {
        // number formatters
        df= new DecimalFormat("#.##");
        pdf = new DecimalFormat("###.#");
    }


    public void getValues() throws IOException, UnsupportedBusNumberException, InterruptedException {
        // create gpio controller
        final GpioController gpio = GpioFactory.getInstance();

        // create custom ADS1115 GPIO provider
        final ADS1115GpioProvider gpioProvider = new ADS1115GpioProvider(I2CBus.BUS_1, ADS1115GpioProvider.ADS1115_ADDRESS_0x48);

        // provision gpio analog input pins from ADS1115
        GpioPinAnalogInput myInputs[] = {
                gpio.provisionAnalogInputPin(gpioProvider, ADS1115Pin.INPUT_A0, "MyAnalogInput-A0"),
                gpio.provisionAnalogInputPin(gpioProvider, ADS1115Pin.INPUT_A1, "MyAnalogInput-A1"),
                gpio.provisionAnalogInputPin(gpioProvider, ADS1115Pin.INPUT_A2, "MyAnalogInput-A2"),
                gpio.provisionAnalogInputPin(gpioProvider, ADS1115Pin.INPUT_A3, "MyAnalogInput-A3"),
        };


        gpioProvider.setProgrammableGainAmplifier(ProgrammableGainAmplifierValue.PGA_4_096V, ADS1115Pin.ALL);


        // Define a threshold value for each pin for analog value change events to be raised.
        // It is important to set this threshold high enough so that you don't overwhelm your program with change events for insignificant changes
        gpioProvider.setEventThreshold(500, ADS1115Pin.ALL);


        // Define the monitoring thread refresh interval (in milliseconds).
        // This governs the rate at which the monitoring thread will read input values from the ADC chip
        // (a value less than 50 ms is not permitted)
        gpioProvider.setMonitorInterval(100);


        // create analog pin value change listener
        GpioPinListenerAnalog listener = new GpioPinListenerAnalog()
        {

            public void handleGpioPinAnalogValueChangeEvent(GpioPinAnalogValueChangeEvent event)
            {
                // RAW value
                double value = event.getValue();

                // percentage
                double percent =  ((value * 100) / ADS1115GpioProvider.ADS1115_RANGE_MAX_VALUE);

                // approximate voltage ( *scaled based on PGA setting )
                double voltage = gpioProvider.getProgrammableGainAmplifier(event.getPin()).getVoltage() * (percent/100);

                // display output
                //System.out.println(" (" + event.getPin().getName() +") : VOLTS=" + df.format(voltage) + "  | PERCENT=" + pdf.format(percent) + "% | RAW=" + value + "       ");
                String val = (" (" + event.getPin().getName() +") : VOLTS=" + df.format(voltage) + "  | PERCENT=" + pdf.format(percent) + "% | RAW=" + value + "       ");
                values = val;
            }
        };

        myInputs[0].addListener(listener);
        //myInputs[1].addListener(listener);
        //myInputs[2].addListener(listener);
        //myInputs[3].addListener(listener);
        // keep program running for 10 minutes
        //Thread.sleep(600000);
        // stop all GPIO activity/threads by shutting down the GPIO controller
        // (this method will forcefully shutdown all GPIO monitoring threads and scheduled tasks)
        //gpio.shutdown();

    }

    public String sendValues() throws InterruptedException, IOException, UnsupportedBusNumberException {
        getValues();
        return values;
    }

}
