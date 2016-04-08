import com.sun.deploy.util.SessionState;

import java.io.*;
import java.net.*;
import java.util.ArrayList;

class ClientWorker implements Runnable
{
    private Socket client;
    public int clientID;
    private String line, clientName;
    private BufferedReader in = null;
    private PrintWriter out = null;
    private String[] messages = new String[10];
    private boolean connected;
    private int index, loopMax;

    ClientWorker(Socket client, int id, boolean con)
    {
        this.client = client;
        clientID = id;
        connected = con;
    }

    public void run()
    {
        try
        {
            in = new BufferedReader(new InputStreamReader(client.getInputStream()));
            out = new PrintWriter(client.getOutputStream(), true);
        }
        catch (IOException e)
        {
            System.out.println("in or out failed");
            System.exit(-1);
        }

            // Receive text from client
            read();
            clientName = line;
            checkIfDuplicateName();

            // Send response back to client
            line = "Hi " + clientName;
            write(line);

            do
            {
                read();
                    int temp2 = Integer.parseInt(line);
                    switch (temp2) {
                        case 1:
                            displayAllKnownUsers();
                            break;
                        case 2:
                            displayNamesOfConnectedUsers();
                            break;
                        case 3:
                            sendMessageToUser();
                            break;
                        case 4:
                            sendMessageToAllConnectedUsers();
                            break;
                        case 5:
                            sendMessageToAllKnownUsers();
                            break;
                        case 6:
                            getMyMessages();
                            break;
                        case 7:
                            closeSocket();
                            break;
                        default:
                            System.out.println("Invalid Menu entry");
                            break;
                }
            }while(this.connected);

            //Close Socket
            closeSocket();

    }
    //Start of client's menu options

    public void displayAllKnownUsers()
    {
        //Go through array of ClientWorkers up until count of ClientWorkers
        //append name to mega-message of known usernames
        //each username has a newline before the name to format the message.
        index = 0;
        loopMax = SocketThrdServer.clients.size();
        line = "";
        while(index < loopMax){
            line += "/n";
            line += "" + index + ") ";
            line += SocketThrdServer.clients.get(index).clientName;
            index++;
        }
        write(line);
    }

    public void displayNamesOfConnectedUsers()
    {
        index = 0;
        loopMax = SocketThrdServer.clients.size();
        line = "";
        while(index < loopMax){
            //check if client at index is connected, if so add name to message
            if(SocketThrdServer.clients.get(index).connected == true){
                line += "/n";
                line += "" + index + ") ";
                line += SocketThrdServer.clients.get(index).clientName;
            }//end if
        }//end while loop
        index++;
        write(line);
    }

    public void sendMessageToUser()
    {
        index = 0;
        loopMax = SocketThrdServer.clients.size();
        line = "";
        while(index < loopMax){
            line += "/n";
            line += "" + index + ") ";
            line += SocketThrdServer.clients.get(index).clientName;
            index++;
        }
        //add option to send message to unknown user
        line += "/n";
        line += "" + index + ") Other User";
        write(line);
    }

    public void sendMessageToAllConnectedUsers()
    {

    }

    public void sendMessageToAllKnownUsers()
    {

    }
    public void getMyMessages()
    {
    }

    //check if duplicate client names
    public void checkIfDuplicateName()
    {
            for( int x=0; x<SocketThrdServer.clients.size(); x++)
            {
                ClientWorker c = SocketThrdServer.clients.get(x);
                if((c.clientName == this.clientName) && (c.clientID!=this.clientID))
                {
                    if (c.connected == true)
                    {
                        System.out.println("Error: Socket name already in use");
                        closeSocket();
                    }
                    else
                    {
                     write("This user already exists. Would you like to claim it? /n1) Yes /n2) No/nEnter option number indicating your choice: ");
                        SocketClient.isDuplicate=true;
                        read();


                    }
                    //if so, send error message and do not allow another thread to handle that client
                    //if not, allow thread to handle that client and set c.connected to true
                }
            }
    }


    // read in line from client
    public void read()
    {
        try
        {
            //get text from client
            line = in.readLine();

        }catch(IOException e)
        {
            System.out.println("Read Failed");
            System.exit(-1);
        }
    }

    //write message to client
    public void write(String toSend)
    {
            //send response to client
            out.println(toSend);
    }
    // close socket
    public void closeSocket()
    {
        try
        {
            client.close();
            this.connected=false;
        }
        catch (IOException e)
        {
            System.out.println("Close failed");
            System.exit(-1);
        }
    }
}

class SocketThrdServer
{
    ServerSocket server = null;
    private static int count =0;
    public static ArrayList<Thread> workers= new ArrayList<>();
    public static ArrayList<ClientWorker> clients = new ArrayList<>();

    public void listenSocket(int port)
    {
        try
        {
            server = new ServerSocket(port);
            System.out.println("Server running on port " + port +
                    "," + " use ctrl-C to end");
        }
        catch (IOException e)
        {
            System.out.println("Error creating socket");
            System.exit(-1);
        }



        while(true)
        {
            ClientWorker w;
            try
            {
                w = new ClientWorker(server.accept(), count, true);
                clients.add(w);
                Thread t = new Thread(clients.get(count));
                workers.add(t);
                workers.get(count).start();
                count++;
            }
            catch (IOException e)
            {
                System.out.println("Accept failed");
                System.exit(-1);
            }
        }
    }

    protected void finalize()
    {
        try
        {
            server.close();
        }
        catch (IOException e)
        {
            System.out.println("Could not close socket");
            System.exit(-1);
        }
    }

    public static void main(String[] args)
    {
        if (args.length != 1)
        {
            System.out.println("Usage: java SocketThrdServer port");
            System.exit(1);
        }

        SocketThrdServer server = new SocketThrdServer();
        int port = Integer.valueOf(args[0]);
        server.listenSocket(port);
    }
}