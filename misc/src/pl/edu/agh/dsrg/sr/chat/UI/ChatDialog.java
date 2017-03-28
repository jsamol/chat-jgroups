package pl.edu.agh.dsrg.sr.chat.UI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Created by julia on 27.03.2017.
 */
public class ChatDialog extends JDialog implements ActionListener {
    private String field;
    private boolean okClicked;
    private String labelText;

    private JTextField textField;
    private JLabel label;

    private JButton buttonOK;
    private JButton buttonCancel;

    public ChatDialog(JFrame parent, String title, String labelText) {
        super(parent, true);
        setTitle(title);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                dispose();
            }
        });
        this.labelText = labelText;
        initLayout();
        setVisible(true);
    }

    public void initLayout() {
        setLayout(new GridLayout(2, 2));
        setSize(300, 100);

        textField = new JTextField();

        label = new JLabel(labelText);

        buttonOK = new JButton("OK");
        buttonOK.addActionListener(this);

        buttonCancel = new JButton("Cancel");
        buttonCancel.addActionListener(this);

        add(label);
        add(textField);
        add(buttonOK);
        add(buttonCancel);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();
        if (command.equals("OK")) {
            field = textField.getText();
            if (!field.equals("")) {
                okClicked = true;
                setVisible(false);
            }
        }
        else if (command.equals("Cancel"))
            setVisible(false);
    }

    public String getField() {
        return field;
    }

    public boolean isOkClicked() {
        return okClicked;
    }
}
