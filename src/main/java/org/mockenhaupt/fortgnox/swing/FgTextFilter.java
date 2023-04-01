package org.mockenhaupt.fortgnox.swing;

import org.mockenhaupt.fortgnox.misc.FileUtils;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class FgTextFilter extends JPanel
{
    private JTextField textFilter;
    private TextFilterHandler handler;

    private final JLabel label;

    public FgTextFilter (String label, TextFilterHandler handler)
    {
        if (label != null)
            this.label = new JLabel(label);
        else
            this.label = null;

        if (handler == null)
        {
            throw new NullPointerException("TextFilterHandler must not be null");
        }
        this.handler = handler;
        initialize();
    }

    public FgTextFilter (TextFilterHandler handler)
    {
        this(null, handler);
    }

    public interface TextFilterHandler
    {
        void handleTextFilterChanged (String filter);
    }


    private void initialize ()
    {
        setLayout(new BorderLayout());
        textFilter = new JTextField();
        textFilter.addKeyListener(new KeyAdapter()
        {
            @Override
            public void keyReleased (KeyEvent e)
            {
                super.keyReleased(e);
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE)
                {
                    textFilter.setText("");
                    callHandler();
                }
            }
        });

        textFilter.getDocument().addDocumentListener(new DocumentListener()
        {
            @Override
            public void insertUpdate (DocumentEvent e)
            {
                callHandler ();
            }

            @Override
            public void removeUpdate (DocumentEvent e)
            {
                callHandler ();
            }

            @Override
            public void changedUpdate (DocumentEvent e)
            {
                callHandler ();
            }
        });

        JButton cleanButton = new JButton();
        cleanButton.setFocusable(false);
        cleanButton.setMinimumSize(new java.awt.Dimension(30, 30));
        cleanButton.setPreferredSize(new java.awt.Dimension(30, 30));
        ImageIcon cleanButtonIcon = FileUtils.getScaledIcon(getClass(), "/org/mockenhaupt/fortgnox/cross32.png", 28);
        cleanButton.setIcon(cleanButtonIcon);
        cleanButton.addActionListener(this::cleanButtonActionPerformed);


        if (label != null)
        {
            add(label, BorderLayout.WEST);
        }

        add(textFilter, BorderLayout.CENTER);
        add(cleanButton, BorderLayout.EAST);
    }

    @Override
    public void requestFocus ()
    {
        textFilter.requestFocus();
    }

    public void setText (String text)
    {
         textFilter.setText(text);
    }

    public String getText ()
    {
        return textFilter.getText();
    }

    @Override
    public synchronized void addKeyListener (KeyListener l)
    {
        textFilter.addKeyListener(l);
    }

    private void callHandler ()
    {
        handler.handleTextFilterChanged(textFilter.getText());
    }

    private void cleanButtonActionPerformed (ActionEvent evt)
    {
        textFilter.setText("");
        requestFocus();
        callHandler();
    }
}
