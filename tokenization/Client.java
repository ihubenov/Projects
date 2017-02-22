package tokenization;

// Fig. 24.7: Client.java
// Client that reads and displays information sent from a Server.
import com.thoughtworks.xstream.XStream;
import data.CardNumber;
import data.Token;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import java.util.regex.*;

public class Client extends JFrame {

    private JTextField textField;
    private JButton generateTokenButton;
    private JButton getCardNumFromToken;
    private ObjectOutputStream output; // output stream to server
    private ObjectInputStream input; // input stream from server
    private String message = ""; // message from server
    private String server; // host server for this application
    private Socket client; // socket to communicate with server

    // initialize chatServer and set up GUI
    public Client(String host) {
        super("Menu");

        XStream xstream = new XStream();
        xstream.alias("cardnumber", CardNumber.class);
        xstream.alias("token", Token.class);

        textField = new JTextField(16);
        add(textField);
        
        server = host; // set server to which this client connects

        generateTokenButton = new JButton("Generate Token");
        generateTokenButton.setPreferredSize(new Dimension(200, 50));
        generateTokenButton.addActionListener((ActionEvent event) -> {
            Pattern p = Pattern.compile("[0-9]{16}");
            Matcher m = p.matcher(textField.getText());  
            if(m.matches()) {
                String msg = xstream.toXML(textField.getText());
                try {
                    output.writeObject(msg);
                    output.flush();
                } catch (IOException ex) {
                    System.out.println("Error when writing to server");
                }
            } else {
                JOptionPane.showMessageDialog(null, "Enter 16 numbers in the field");
            }
            
        });
        add(generateTokenButton);

        getCardNumFromToken = new JButton("Get card number from token");
        getCardNumFromToken.setPreferredSize(new Dimension(200, 50));
        getCardNumFromToken.addActionListener((ActionEvent event) -> {
            Pattern p = Pattern.compile("[0-9]{16}");
            Matcher m = p.matcher(textField.getText());  
            if(m.matches()) {
                String msg = xstream.toXML(textField.getText());
                try {
                    output.writeObject(msg);
                    output.flush();
                } catch (IOException ex) {
                    System.out.println("Error when writing to server");
                }
            } else {
                JOptionPane.showMessageDialog(null, "Enter 16 numbers in the field");
            }
        });
        add(getCardNumFromToken);

        setSize(300, 180); // set size of window
        setLayout(new FlowLayout());
        setVisible(true); // show window
    } // end Client constructor

    // connect to server and process messages from server
    public void runClient() {
        try // connect to server, get streams, process connection
        {
            connectToServer(); // create a Socket to make connection
            getStreams(); // get the input and output streams
            processConnection(); // process connection
        } // end try
        catch (EOFException eofException) {
            System.out.println("Client terminated connection");
        } // end catch
        catch (IOException ioException) {
            ioException.printStackTrace();
        } // end catch
        finally {
            //closeConnection(); // close connection
        } // end finally
    } // end method runClient

    // connect to server
    private void connectToServer() throws IOException {
        System.out.println("!" + InetAddress.getByName(server) + "!");
        client = new Socket(InetAddress.getByName(server), 12345);
        System.out.println(client.toString());

    } // end method connectToServer

    // get streams to send and receive data
    private void getStreams() throws IOException {

        // set up output stream for objects
        output = new ObjectOutputStream(client.getOutputStream());
        output.flush(); // flush output buffer to send header information
        // set up input stream for objects
        input = new ObjectInputStream(client.getInputStream());
    } // end method getStreams

    // process connection with server
    private void processConnection() throws IOException {

        System.out.println("processCon");
        do // process messages sent from server
        {
            try // read message and display it
            {
                message = (String) input.readObject(); // read new message
                switch (message) {
                    case "correct account":
                        JOptionPane.showMessageDialog(null, "Logged in!");
                        break;
                    case "incorrect account":
                        JOptionPane.showMessageDialog(null, "Incorrect "
                                + "account or password!");
                        this.dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
                        break;
                    case "token saved":
                        JOptionPane.showMessageDialog(null,
                                "Token saved successfully!");
                        break;
                    case "token not saved":
                        JOptionPane.showMessageDialog(null,
                                "Incorrect card number");
                        break;
                    case "unregistered token":
                        JOptionPane.showMessageDialog(null,
                                "Token has not been registered");
                        break;
                    default:
                        JOptionPane.showMessageDialog(null, "Card Number for"
                                + "this token: " + message);
                        break;
                }
            } // end try
            catch (ClassNotFoundException classNotFoundException) {
                JOptionPane.showMessageDialog(null, "Error!");
            } // end catch

        } while (true);
    } // end method processConnection

    // close streams and socket
    private void closeConnection() {
        try {
            output.close(); // close output stream
            input.close(); // close input stream
            client.close(); // close socket
        } // end try
        catch (IOException ioException) {
            ioException.printStackTrace();
        } // end catch
    } // end method closeConnection

    // send message to server
    private void sendData(String message) {
        try // send object to server
        {
            output.writeObject(message);
            output.flush(); // flush data to output
        } catch (IOException ioException) {

        } // end catch
    } // end method sendData

} // end class Client
