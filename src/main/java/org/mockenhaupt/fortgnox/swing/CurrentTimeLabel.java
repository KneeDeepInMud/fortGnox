package org.mockenhaupt.fortgnox.swing;

import javax.swing.JLabel;
import javax.swing.Timer;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class CurrentTimeLabel extends JLabel implements ActionListener
{
    private Timer refreshTimer;

    public CurrentTimeLabel()
    {
        initialize();
    }

    private void initialize()
    {
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
        LocalTime localTime = LocalTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        setText(localTime.format(formatter));
        repaint();
    }
}
