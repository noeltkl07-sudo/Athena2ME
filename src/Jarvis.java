
import javax.microedition.midlet.MIDlet;
import javax.microedition.lcdui.*;

public class Jarvis extends MIDlet {
    private Display display;
    private Form screen;

    public void startApp() {
        display = Display.getDisplay(this);
        screen = new Form("J.A.R.V.I.S.");
        screen.append("SYSTEM ONLINE");
        screen.append("\nWelcome back, Noel.");
        screen.append("\nMotorola Edition 1.0");
        display.setCurrent(screen);
    }

    public void pauseApp() {}

    public void destroyApp(boolean unconditional) {}
}
