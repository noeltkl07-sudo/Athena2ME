import javax.microedition.midlet.MIDlet;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.List;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.AlertType;

/** Small CLDC 1.0 / MIDP 2.0 startup and menu diagnostic for Motorola RAZR. */
public class Jarvis extends MIDlet implements CommandListener {
    private Display display;
    private List menu;
    private Command exit = new Command("EXIT", Command.EXIT, 1);

    public void startApp() {
        display = Display.getDisplay(this);
        if (menu == null) {
            menu = new List("J.A.R.V.I.S. 2.1", List.IMPLICIT);
            menu.append("SYSTEM ONLINE", null);
            menu.append("COMMAND CENTER", null);
            menu.append("SYSTEM STATUS", null);
            menu.append("ABOUT", null);
            menu.addCommand(exit);
            menu.setCommandListener(this);
        }
        display.setCurrent(menu);
    }
    public void pauseApp() { }
    public void destroyApp(boolean unconditional) { }

    public void commandAction(Command c, Displayable d) {
        if (c == exit) {
            notifyDestroyed();
            return;
        }
        if (c == List.SELECT_COMMAND && d == menu) {
            int index = menu.getSelectedIndex();
            String result = "READY";
            if (index == 0) result = "SYSTEM ONLINE";
            else if (index == 1) result = "COMMAND CENTER TEST OK";
            else if (index == 2) result = "MODE: OFFLINE";
            else if (index == 3) result = "JARVIS TEST 2.1 - NO NETWORK";
            Alert alert = new Alert("J.A.R.V.I.S.", result, null, AlertType.INFO);
            alert.setTimeout(Alert.FOREVER);
            display.setCurrent(alert, menu);
        }
    }
}
