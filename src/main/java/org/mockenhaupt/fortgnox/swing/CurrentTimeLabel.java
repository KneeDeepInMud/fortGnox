package org.mockenhaupt.fortgnox.swing;

import javax.swing.JLabel;
import javax.swing.Timer;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class CurrentTimeLabel extends JLabel implements ActionListener
{
    private Timer refreshTimer;
    private int countdown;
    private final String prefix;

    enum Mode {
        TIME,
        COUNTDOWN
    }



    private final Mode mode;

    public CurrentTimeLabel(Mode mode, int countdown, String prefix)
    {
        this.prefix = prefix;
        this.countdown = countdown;
        this.mode = mode;
        initialize();
    }

    public CurrentTimeLabel()
    {
        this(Mode.TIME, 30, "");
    }

    private void initialize()
    {
        setFont(new Font(getFont().getName(), Font.BOLD, getFont().getSize()));
        refreshTimer = new Timer(1000, this);
        refreshTimer.setRepeats(true);
        refreshTimer.start();
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
        updateTime();
    }

    private void updateTime()
    {
        DateTimeFormatter formatter;
        LocalTime localTime = LocalTime.now();
        switch (mode)
        {
            case TIME:
                formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
                setText(prefix + localTime.format(formatter));
                break;
            case COUNTDOWN:
                formatter = DateTimeFormatter.ofPattern("mm:ss");
                int remain = countdown - localTime.toSecondOfDay() % countdown;
                LocalTime remainTime = LocalTime.ofSecondOfDay(remain);
                setText(prefix + remainTime.format(formatter));
                break;
        }
        repaint();
    }
}
