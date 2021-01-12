package org.mockenhaupt.fortgnox;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 *
 * @author fmoc
 */
public class PassphraseDialog extends javax.swing.JDialog
{
    String passPhrase = "";
    private javax.swing.JButton cancelButton;
    private javax.swing.JButton okButton;
    private javax.swing.JPasswordField passwordField;

    public PassphraseDialog (java.awt.Frame parent, boolean modal)
    {
        super(parent, modal);
        initComponents();
        final PassphraseDialog me = this;
        passwordField.addKeyListener(new KeyAdapter()
        {
            @Override
            public void keyReleased (KeyEvent e)
            {
                super.keyReleased(e);

                if (e.getKeyCode() == KeyEvent.VK_ENTER)
                {
                    e.consume();
                    passPhrase = new String(passwordField.getPassword()); //passwordField.getPassword().toString();
                    me.setVisible(false);
                }
                else if (e.getKeyCode() == KeyEvent.VK_ESCAPE)
                {
                    e.consume();
                    passPhrase = "";
                    me.setVisible(false);
                }
            }

        });
    }

    @Override
    public void setVisible (boolean b)
    {
        super.setVisible(b);
        passwordField.requestFocus();
    }

    public String getPassPhrase ()
    {
        return passPhrase;
    }

    public void setPassPhrase (String passPhrase)
    {
        this.passPhrase = passPhrase;
        passwordField.setText("");
    }

    private void initComponents()
    {

        okButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();
        passwordField = new javax.swing.JPasswordField();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Enter passphrase");

        okButton.setText("Ok");
        okButton.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                okButtonActionPerformed(evt);
            }
        });

        cancelButton.setText("Cancel");
        cancelButton.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                cancelButtonActionPerformed(evt);
            }
        });

        passwordField.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);

        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap(72, Short.MAX_VALUE)
                .addComponent(okButton, javax.swing.GroupLayout.PREFERRED_SIZE, 52, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(cancelButton, javax.swing.GroupLayout.PREFERRED_SIZE, 87, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(42, 42, 42))
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(passwordField)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(7, 7, 7)
                .addComponent(passwordField, javax.swing.GroupLayout.DEFAULT_SIZE, 37, Short.MAX_VALUE)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cancelButton)
                    .addComponent(okButton)))
        );

        pack();
    }

    private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {
        passPhrase = new String(passwordField.getPassword()); //passwordField.getPassword().toString();
        this.setVisible(false);
    }

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {
        passPhrase = "";
        this.setVisible(false);
        // TODO add your handling code here:
    }
}
