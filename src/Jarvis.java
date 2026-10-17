import javax.microedition.midlet.MIDlet;
import javax.microedition.lcdui.*;
import javax.microedition.rms.*;

public final class Jarvis extends MIDlet implements CommandListener {
    private Display display;
    private Hub hub;
    private TextBox editor;
    private final Command save = new Command("SAVE", Command.OK, 1);
    private final Command cancel = new Command("BACK", Command.BACK, 2);
    private String note = "";
    private String[] tasks = new String[12];
    private int taskCount = 0;
    private int editMode = 0; // 1 note, 2 task, 3 calculator
    private int selectedTask = 0;
    private String message = "READY";

    public void startApp() {
        display = Display.getDisplay(this);
        load();
        hub = new Hub();
        display.setCurrent(hub);
    }
    public void pauseApp() { persist(); }
    public void destroyApp(boolean unconditional) { persist(); }

    private void edit(int mode) {
        editMode = mode;
        String title = mode == 1 ? "PERSONAL NOTE" : mode == 2 ? "NEW MISSION" : "CALCULATOR";
        String initial = mode == 1 ? note : "";
        editor = new TextBox(title, initial, mode == 1 ? 600 : 80, mode == 3 ? TextField.ANY : TextField.ANY);
        editor.addCommand(save);
        editor.addCommand(cancel);
        editor.setCommandListener(this);
        display.setCurrent(editor);
    }
    public void commandAction(Command c, Displayable d) {
        if (c == save) {
            String s = editor.getString();
            if (editMode == 1) { note = s; message = "NOTE SAVED"; persist(); }
            if (editMode == 2) {
                if (s.trim().length() > 0 && taskCount < tasks.length) {
                    tasks[taskCount++] = s.trim(); message = "MISSION ADDED"; persist();
                } else message = "EMPTY OR FULL";
            }
            if (editMode == 3) { message = calculate(s); }
        }
        display.setCurrent(hub);
        hub.repaint();
    }
    private String calculate(String s) {
        try {
            s = s.trim(); int op = -1;
            for (int i = 1; i < s.length(); i++) {
                char ch = s.charAt(i);
                if (ch == '+' || ch == '-' || ch == '*' || ch == '/') { op = i; break; }
            }
            if (op < 0) return "USE: 12+3";
            double a = Double.parseDouble(s.substring(0, op).trim());
            double b = Double.parseDouble(s.substring(op+1).trim());
            double result;
            char sign = s.charAt(op);
            if (sign == '+') result = a + b;
            else if (sign == '-') result = a - b;
            else if (sign == '*') result = a * b;
            else { if (b == 0) return "DIVIDE BY ZERO"; result = a / b; }
            return "RESULT: " + result;
        } catch (Exception e) { return "INVALID EXPRESSION"; }
    }
    private void persist() {
        RecordStore rs = null;
        try {
            rs = RecordStore.openRecordStore("JARVIS2", true);
            java.io.ByteArrayOutputStream bytes = new java.io.ByteArrayOutputStream();
            java.io.DataOutputStream out = new java.io.DataOutputStream(bytes);
            out.writeUTF(note);
            out.writeInt(taskCount);
            for (int i = 0; i < taskCount; i++) out.writeUTF(tasks[i]);
            out.flush();
            byte[] data = bytes.toByteArray();
            if (rs.getNumRecords() == 0) rs.addRecord(data, 0, data.length);
            else rs.setRecord(1, data, 0, data.length);
            out.close();
        } catch (Exception e) { message = "SAVE ERROR"; }
        finally { try { if (rs != null) rs.closeRecordStore(); } catch (Exception e) {} }
    }
    private void load() {
        RecordStore rs = null;
        try {
            rs = RecordStore.openRecordStore("JARVIS2", true);
            if (rs.getNumRecords() > 0) {
                java.io.DataInputStream in = new java.io.DataInputStream(new java.io.ByteArrayInputStream(rs.getRecord(1)));
                note = in.readUTF();
                int n = in.readInt();
                if (n < 0 || n > tasks.length) n = 0;
                taskCount = n;
                for (int i = 0; i < n; i++) tasks[i] = in.readUTF();
                in.close();
            }
        } catch (Exception e) { note = ""; taskCount = 0; }
        finally { try { if (rs != null) rs.closeRecordStore(); } catch (Exception e) {} }
    }
    private final class Hub extends Canvas {
        private final int BG = 0x061320, PANEL = 0x102A3B, BLUE = 0x48CCFF, WHITE = 0xD7F2FF;
        private int screen = 0; // 0 menu, 1 notes, 2 tasks, 3 system, 4 help
        private int selection = 0;
        private final String[] menu = {"NOTES", "MISSIONS", "CALCULATOR", "SYSTEM", "HELP", "EXIT"};
        Hub() { setFullScreenMode(false); }
        protected void paint(Graphics g) {
            int w = getWidth(), h = getHeight();
            g.setColor(BG); g.fillRect(0, 0, w, h);
            g.setColor(BLUE); g.fillRect(0, 0, w, 3);
            g.setFont(Font.getFont(Font.FACE_SYSTEM, Font.STYLE_BOLD, Font.SIZE_MEDIUM));
            g.drawString("J.A.R.V.I.S. 2.0", w/2, 7, Graphics.TOP | Graphics.HCENTER);
            g.setColor(0x1E566F); g.drawLine(8, 28, w-8, 28);
            g.setFont(Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_SMALL));
            if (screen == 0) {
                g.setColor(WHITE); g.drawString("WELCOME BACK, NOEL", 10, 35, Graphics.TOP | Graphics.LEFT);
                int top = 56; int step = Math.max(19, Math.min(26, (h-94)/menu.length));
                for (int i = 0; i < menu.length; i++) {
                    int y = top + i*step;
                    g.setColor(i == selection ? BLUE : PANEL); g.fillRoundRect(8, y, w-16, step-3, 5, 5);
                    g.setColor(i == selection ? BG : WHITE);
                    g.drawString((i+1) + "  " + menu[i], 15, y+3, Graphics.TOP | Graphics.LEFT);
                }
            } else if (screen == 1) {
                g.setColor(BLUE); g.drawString("PERSONAL NOTE", 10, 35, Graphics.TOP | Graphics.LEFT);
                drawWrapped(g, note.length() == 0 ? "No note saved." : note, 10, 56, w-20, h-95);
            } else if (screen == 2) {
                g.setColor(BLUE); g.drawString("MISSIONS " + taskCount + "/12", 10, 35, Graphics.TOP | Graphics.LEFT);
                int y = 56;
                if (taskCount == 0) { g.setColor(WHITE); g.drawString("No missions yet.", 10, y, Graphics.TOP | Graphics.LEFT); }
                for (int i = 0; i < taskCount && y < h-39; i++) {
                    g.setColor(i == selectedTask ? BLUE : WHITE);
                    String t = tasks[i];
                    int max = Math.max(6, (w-38)/7);
                    if (t.length() > max) t = t.substring(0,max-2) + "..";
                    g.drawString((i == selectedTask ? "> " : "  ") + t, 9, y, Graphics.TOP | Graphics.LEFT);
                    y += 19;
                }
            } else if (screen == 3) {
                g.setColor(WHITE);
                g.drawString("STATUS: ONLINE", 10, 40, Graphics.TOP | Graphics.LEFT);
                g.drawString("MODE: OFFLINE", 10, 60, Graphics.TOP | Graphics.LEFT);
                g.drawString("MISSIONS: " + taskCount, 10, 80, Graphics.TOP | Graphics.LEFT);
                g.drawString("STORAGE: RMS", 10, 100, Graphics.TOP | Graphics.LEFT);
            } else {
                g.setColor(WHITE);
                drawWrapped(g, "MENU: UP/DOWN and FIRE. NOTES: FIRE edits. MISSIONS: FIRE adds, key 0 deletes selected. BACK: right softkey or star.", 10, 38, w-20, h-75);
            }
            g.setColor(0x0B283B); g.fillRect(0, h-27, w, 27);
            g.setColor(BLUE);
            String left = screen == 0 ? "SELECT" : screen == 1 ? "EDIT" : screen == 2 ? "ADD" : "BACK";
            g.drawString(left, 5, h-23, Graphics.TOP | Graphics.LEFT);
            g.drawString(screen == 0 ? "EXIT" : "BACK", w-5, h-23, Graphics.TOP | Graphics.RIGHT);
            if (message.length() > 0 && screen == 0) {
                g.setColor(0x9ABCCD); g.drawString(message, w/2, h-42, Graphics.TOP | Graphics.HCENTER);
            }
        }
        private void drawWrapped(Graphics g, String value, int x, int y, int width, int maxHeight) {
            g.setColor(WHITE);
            Font f = g.getFont(); int line = f.getHeight()+3;
            String s = value.replace('\n',' ');
            while (s.length() > 0 && y < maxHeight+35) {
                int end = s.length();
                while (end > 1 && f.stringWidth(s.substring(0,end)) > width) end--;
                if (end < s.length()) {
                    int space = s.lastIndexOf(' ', end);
                    if (space > 0) end = space;
                }
                g.drawString(s.substring(0,end), x, y, Graphics.TOP | Graphics.LEFT);
                s = s.substring(end).trim(); y += line;
            }
        }
        protected void keyPressed(int keyCode) {
            int action = 0;
            try { action = getGameAction(keyCode); } catch (Exception e) {}
            if (keyCode == KEY_NUM2 || action == UP) {
                if (screen == 0) selection = (selection + menu.length - 1) % menu.length;
                if (screen == 2 && taskCount > 0) selectedTask = (selectedTask + taskCount - 1) % taskCount;
            } else if (keyCode == KEY_NUM8 || action == DOWN) {
                if (screen == 0) selection = (selection+1)%menu.length;
                if (screen == 2 && taskCount > 0) selectedTask = (selectedTask+1)%taskCount;
            } else if (keyCode == KEY_STAR || keyCode == -7) {
                if (screen == 0) notifyDestroyed(); else screen = 0;
            } else if (keyCode == KEY_NUM0 && screen == 2 && taskCount > 0) {
                for (int i = selectedTask; i < taskCount-1; i++) tasks[i] = tasks[i+1];
                tasks[--taskCount] = null;
                if (selectedTask >= taskCount) selectedTask = Math.max(0,taskCount-1);
                message = "MISSION REMOVED"; persist();
            } else if (keyCode == KEY_NUM5 || action == FIRE || keyCode == -6) {
                if (screen == 0) {
                    if (selection == 0) screen = 1;
                    else if (selection == 1) screen = 2;
                    else if (selection == 2) { edit(3); return; }
                    else if (selection == 3) screen = 3;
                    else if (selection == 4) screen = 4;
                    else { persist(); notifyDestroyed(); return; }
                } else if (screen == 1) { edit(1); return; }
                else if (screen == 2) { edit(2); return; }
                else screen = 0;
            }
            repaint();
        }
    }
}

