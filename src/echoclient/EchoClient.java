package echoclient;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import shared.ParseCommands;
import shared.ProtocolStrings;

/**
 * @author Tobias Jacobsen
 */
public class EchoClient extends Observable implements Runnable {

    Socket socket;
    private int port;
    private InetAddress serverAddress;
    private Scanner input;
    private PrintWriter output;
    private String msg = "";
    private ParseCommands parseCommands;
    
    
    

    public void connect(String address, int port) throws UnknownHostException, IOException {
        parseCommands = new ParseCommands();
        this.port = port;
        serverAddress = InetAddress.getByName(address);
        socket = new Socket(serverAddress, port);
        input = new Scanner(socket.getInputStream());
        output = new PrintWriter(socket.getOutputStream(), true);  //Set to true, to get auto flush behaviour
        run();
    }

    public void send(String msg) {
        output.println(msg);
    }

    public void stop() throws IOException {
        output.println(ProtocolStrings.STOP);
    }

    @Override
    public void run() {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                Map <String, String> map = new HashMap();
                while (true) {
                    msg = input.nextLine();
                    if (msg.equals(ProtocolStrings.STOP)) {
                        try {
                            socket.close();
                        } catch (IOException ex) {
                            Logger.getLogger(EchoClient.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    } else {
                        map = parseCommands.parseServerMessage(msg);
                    }
                    setChanged();
                    notifyObservers(msg);
                }
            }
        });
        t.start();
    }
}
