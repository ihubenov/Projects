package tokenization;

import com.thoughtworks.xstream.XStream;
import data.*;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.AbstractMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

public class Server extends JFrame {

    private List<Account> accounts;
    private List<Entry<CardNumber, Token>> pairs;
    private final JLabel toFileLabel;
    private final JTextArea console;
    private final JTextArea displayUsers;
    private final JTextArea displayNumbers;
    private final JButton sortByCardNumbers;
    private final JButton sortByTokens;
    private ServerSocket server; 
    private Socket connection; 
    private int counter = 1; 

    public Server() {

        super("Server");

        accounts = new LinkedList<>();
        pairs = new LinkedList<>();

        accounts.add(new Account("Admin", "Admin", 1));
        
        CardNumber cn = new CardNumber("1111111111111111");
        Token tok = new Token("2222222222222222");
        pairs.add(new AbstractMap.SimpleEntry<>(cn, tok));

        displayUsers = new JTextArea();
        displayUsers.setEditable(false);
        displayUsers.setPreferredSize(new Dimension(160, 120));
        add(new JScrollPane(displayUsers));

        displayNumbers = new JTextArea();
        displayNumbers.setEditable(false);
        displayNumbers.setPreferredSize(new Dimension(160, 120));
        add(new JScrollPane(displayNumbers));
        
        XStream xstream = new XStream();
        xstream.alias("account", Account.class);
        xstream.alias("pair", Pair.class);
        for( Account a : accounts) {
            displayUsers.append(xstream.toXML(a));
        }
        for( Entry<CardNumber, Token> e : pairs) {
            Pair p = new Pair(e.getKey(), e.getValue());
            displayNumbers.append(xstream.toXML(p));
        }

        toFileLabel = new JLabel("Extract card number <-> token pair to file "
                + "sorted by:");
        add(toFileLabel);

        sortByCardNumbers = new JButton("Credit card numbers");
        add(sortByCardNumbers);
        sortByCardNumbers.addActionListener((ActionEvent event) -> {
            pairsToFile(false);
        });

        sortByTokens = new JButton("Tokens");
        add(sortByTokens);
        sortByTokens.addActionListener((ActionEvent event) -> {
            pairsToFile(true);
        });

        console = new JTextArea();
        console.setEditable(false);
        console.setPreferredSize(new Dimension(360, 40));
        add(new JScrollPane(console));

        setSize(370, 270);
        setLayout(new FlowLayout());
        setVisible(true); 
    } 

    
    public void runServer() {
        try 
        {
            server = new ServerSocket(12345, 100); 

            while (true) {
                try {
                    waitForConnection(); 
                    Thread user = new Thread(new User());
                    user.start();
                } 
                catch (EOFException eofException) {
                    displayMessage("\nServer terminated connection");
                } 
                finally {
                    connection.close();
                    counter++;
                }
            } 
        } 
        catch (IOException ioException) {
            ioException.printStackTrace();
        } 
    }

    private void waitForConnection() throws IOException {
        displayMessage("Waiting for connection\n");
        connection = server.accept(); 
        displayMessage("Connection " + counter + " received from: "
                + connection.getInetAddress().getHostName());
    } 
    
    private void pairsToFile(boolean sortByTokens) {
        
        int n = pairs.size();
        String[] numbersArr = new String[n]; 
        String[] tokensArr = new String[n];
        String min;
        String swap;
        int index;
        int i = 0;
        
        for (Entry<CardNumber, Token> e : pairs) {
            numbersArr[i] = e.getKey().getCardNumber();
            tokensArr[i] = e.getValue().getToken();
            i++;
        }
        
        if(sortByTokens) {
            for(int k = 0 ; k < n - 1 ; k++) {
                min = tokensArr[k];
                index = k;
                for(int j = k + 1 ; j < n ; j++) {
                    if((tokensArr[j]).compareTo(min) < 0 ) {
                        min = tokensArr[j];
                        index = j;
                    }
                }
                swap = tokensArr[index];
                tokensArr[index] = tokensArr[k];
                tokensArr[k] = swap;
                
                swap = numbersArr[index];
                numbersArr[index] = numbersArr[k];
                numbersArr[k] = swap;
            }
        } else {
            for(int k = 0 ; k < n - 1 ; k++) {
                min = numbersArr[k];
                index = k;
                for(int j = k + 1 ; j < n ; j++) {
                    if((numbersArr[j]).compareTo(min) < 0 ) {
                        min = numbersArr[j];
                        index = j;
                    }
                }
                swap = numbersArr[index];
                numbersArr[index] = numbersArr[k];
                numbersArr[k] = swap;
                
                swap = tokensArr[index];
                tokensArr[index] = tokensArr[k];
                tokensArr[k] = swap;
            }
        }
        
        Path path = Paths.get("pairs.txt");
        byte[] newLine = System.getProperty("line.separator").getBytes();
        try {
            Files.write(path, "Credit card number  |    Token".getBytes());
            Files.write(path, newLine, StandardOpenOption.APPEND);
            Files.write(path, "-----------------------------------------".getBytes(),
                    StandardOpenOption.APPEND);
            Files.write(path, newLine, StandardOpenOption.APPEND);
            for(int k = 0 ; k < n ; k++) {
                Files.write(path,
                        (numbersArr[k] + "    |    " + tokensArr[k]).getBytes(),
                        StandardOpenOption.APPEND);
                Files.write(path, newLine, StandardOpenOption.APPEND);
                Files.write(path, "-----------------------------------------".getBytes(),
                    StandardOpenOption.APPEND);
                Files.write(path, newLine, StandardOpenOption.APPEND);
            }
        } catch (IOException ioException) {
            System.out.println("Error with writing in file");
        }
        
        
    }

    class User implements Runnable {

        private ObjectOutputStream output; 
        private ObjectInputStream input; 

        @Override
        public void run() {
            try {
                getStreams();
                processConnection(); 
            } catch (IOException ioe) {
                displayMessage("\nServer terminated connection");
            }
        }

        private void getStreams() throws IOException {
            output = new ObjectOutputStream(connection.getOutputStream());
            output.flush(); 
            
            input = new ObjectInputStream(connection.getInputStream());

            displayMessage("\nGot I/O streams\n");
        } 

        private void processConnection() throws IOException {
            String message = "Connection successful";
            
            do 
            {
                try 
                {
                    message = (String) input.readObject(); 
                    handleMessage(message);
                    displayMessage("\n" + message); 
                } 
                catch (ClassNotFoundException classNotFoundException) {
                    displayMessage("\nUnknown object type received");
                } 

            } while (true);
        } 

        private void handleMessage(String msg) {
            XStream xstream = new XStream();
            xstream.alias("account", Account.class);
            xstream.alias("cardnumber", CardNumber.class);
            xstream.alias("token", Token.class);

            if (msg.contains("Accout")) {
                Account current = (Account) xstream.fromXML(msg);

                for (Account acc : accounts) {
                    if (acc.equals(current)) {
                        try 
                        {
                            output.writeObject("correct account");
                            output.flush(); 
                            displayMessage("User login - successful");
                            return;
                        } // end try
                        catch (IOException ioException) {
                            displayMessage("Error writing object");
                        } 
                    }
                }
                try {
                    output.writeObject("incorrect account");
                    output.flush();
                    displayMessage("User login - failed");
                } catch (IOException ioException) {
                    displayMessage("Error writing object");
                }
            } else if (msg.contains("CardNumber")) {
                CardNumber current = (CardNumber) xstream.fromXML(msg);

                if (generateToken(current.getCardNumber())) {
                    try {
                        output.writeObject("token saved");
                        output.flush();
                        displayMessage("token saved successfully");
                    } catch (IOException ioException) {
                        displayMessage("Error writing object");
                    }
                } else {
                    try {
                        output.writeObject("token not saved");
                        output.flush();
                        displayMessage("token save failed");
                    } catch (IOException ioException) {
                        displayMessage("Error writing object");
                    }
                }
            } else if (msg.contains("Token")) {
                Token current = (Token) xstream.fromXML(msg);

                String card = getCardFromToken(current.getToken());
                if (card == "") {
                    try {
                        output.writeObject("unregistered token");
                        output.flush();
                    } catch (IOException ioException) {
                        displayMessage("Error writing object");
                    }
                } else {
                    try {
                        output.writeObject(card);
                        output.flush();
                    } catch (IOException ioException) {
                        displayMessage("Error writing object");
                    }
                }
            }
        }

        private void closeConnection() {
            displayMessage("\nTerminating connection\n");

            try {
                output.close();
                input.close(); 
            } 
            catch (IOException ioException) {
                ioException.printStackTrace();
            } 
        } 

    }

    private void displayMessage(final String messageToDisplay) {
        SwingUtilities.invokeLater(
                new Runnable() {
            public void run() 
            {
                console.append(messageToDisplay);
            }
        } 
        ); 
    } 

    public boolean generateToken(String cardNum) {
        char[] cardNumChar = cardNum.toCharArray();
        char[] token = new char[16];
        byte rand;
        int sum = 0;
        int tokenSum = 0;

        if (cardNumChar.length != 16) {
            return false;
        }

        do {
            for (int i = 0; i < 16; i++) {
                int current = Character.getNumericValue(cardNumChar[i]);

                if (i == 0) {
                    if (current != 3 && current != 4
                            && current != 5 && current != 6) {
                        return false;
                    } else {
                        do {
                            rand = (byte) (Math.random() * 10);
                        } while (rand != 3 && rand != 4 && rand != 5
                                && rand != 6);

                        tokenSum += rand;
                        token[i] = (char) (rand + '0');
                    }
                } else if (i < 12) {
                    do {
                        rand = (byte) (Math.random() * 10);
                    } while (rand == current);

                    tokenSum += rand;
                    token[i] = (char) (rand + '0');
                } else {
                    tokenSum += Character.getNumericValue(cardNumChar[i]);
                    token[i] = cardNumChar[i];
                }
                if (i % 2 == 0) {
                    current *= 2;
                    sum += current > 9 ? current - 9 : current;
                } else {
                    sum += current;
                }
            }
        } while (tokenContained(token.toString()) && tokenSum % 10 == 0);

        if (sum % 10 == 0) {
            CardNumber cNum = new CardNumber(cardNum);
            Token t = new Token(token.toString());

            Entry<CardNumber, Token> e = new AbstractMap.SimpleEntry<>(cNum, t);
            pairs.add(e);
            Pair p = new Pair(cNum, t);
            XStream xs = new XStream();
            xs.alias("pair", Pair.class);
            displayNumbers.append(xs.toXML(p));
            
            return true;
        }
        return false;
    }

    private String getCardFromToken(String t) {
        for (Entry<CardNumber, Token> e : pairs) {
            if ((e.getValue().getToken()).equals(t)) {
                return e.getKey().getCardNumber();
            }
        }
        return "";
    }

    private boolean tokenContained(String token) {
        for (Entry<CardNumber, Token> e : pairs) {
            if ((e.getValue()).equals(new Token(token))) {
                return true;
            }
        }
        return false;
    }
}
