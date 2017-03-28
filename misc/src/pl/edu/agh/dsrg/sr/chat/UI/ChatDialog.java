package pl.edu.agh.dsrg.sr.chat.UI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class ChatDialog extends JDialog implements ActionListener {
    private String field;
    private boolean okClicked;
    private String labelText;

    private JTextField textField;

    ChatDialog(JFrame parent, String title, String labelText) {
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

    private void initLayout() {
        setLayout(new GridLayout(2, 2));
        setSize(300, 100);

        textField = new JTextField();

        JLabel label = new JLabel(labelText);

        JButton buttonOK = new JButton("OK");
        buttonOK.addActionListener(this);

        JButton buttonCancel = new JButton("Cancel");
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

    String getField() {
        return field;
    }

    boolean isOkClicked() {
        return okClicked;
    }
}
