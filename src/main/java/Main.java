import com.pi4j.io.i2c.I2CFactory;

import java.io.IOException;
import java.util.Timer;

public class Main {

    private static XMPP xmppSession = new XMPP();
    private static EKG ekgRead = new EKG();
    private static String messageCont="test";

    public static void main(String[] args) throws InterruptedException, IOException, I2CFactory.UnsupportedBusNumberException {

        xmppSession.createConfig("sepp","sepp");
        xmppSession.connectMe();
        xmppSession.login();


        while(true){
            messageCont = ekgRead.sendValues();
            xmppSession.sendMessage(messageCont);
            System.out.println("msg"+messageCont);
        }
    }
}