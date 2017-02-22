package tokenization;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.*;

class LoginPanel extends JPanel {

    private JFrame frame;
    private final JLabel userLabel;
    private final JLabel passLabel;
    private final JTextField userField;
    private final JPasswordField passField;
    private final JButton loginButton;

    protected LoginPanel() {
        setLayout(new FlowLayout());

        frame = new JFrame("Login");
        userLabel = new JLabel("Username: ");
        passLabel = new JLabel("Password: ");
        userField = new JTextField(15);
        passField = new JPasswordField(15);
        loginButton = new JButton("Log in");

        add(userLabel);
        add(userField);
        add(passLabel);
        add(passField);
        add(loginButton, BorderLayout.CENTER);

        loginButton.addActionListener((ActionEvent e) -> {
            Pattern p = Pattern.compile("[a-zA-Z0-9]{5,}");
            Matcher m = p.matcher(userField.getText());
            Matcher n = p.matcher(passField.getText());
            if (m.matches() && n.matches()) {
                Client application;

                application = new Client("127.0.0.1");
                application.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                application.runClient();
            } else {
                JOptionPane.showMessageDialog(null, "5 or more letters must "
                        + "be entered in the fields");
            }
        });

    }

    protected void display() {
        LoginPanel lp = new LoginPanel();

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(325, 125);
        frame.setLocationRelativeTo(null);
        frame.add(lp);
        frame.setVisible(true);

    }

}

public class UserInterface {

    public static void main(String[] args) {
        LoginPanel lp = new LoginPanel();
        lp.display();
    }
}
