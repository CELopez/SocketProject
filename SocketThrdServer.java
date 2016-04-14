//The server will: 
// 1) Accept a port number as a command line argument.
// 2) Accept connections from clients.
// 3) Create a new thread for each client.
// 4) Store messages sent to each user. //max of 10 messages per user, so String[10] //max lentgh of messages is 80 characters   //remove messages from server when client gets them
// 5) End by termination with control-C.

//The server thread will:
// 1) Accept and process requests from the client. //remove messages from server when client gets them
// 2) Add the user’s name to the list of known users. //The same user name cannot have multiple connections at once...remove name once connection severed. //max of 100 users
// 3) Provide mutual exclusion protection for the data structure that stores the messages. //messages are unique to the two user...only user that sent can retrieve msg
// 4) Send only the minimal data needed to the client, not the menu or other UI text.

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
        //compile list of users to send to user
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
        //update line to reflect user's choice
        read();
        //if choice is new user
        if(line == index)
        {
            //ask for new user's name/receive user's name
            //check that new user's name is not a duplicate
            //send flag if duplicate or not
            //if not duplicate: 
                //create new clients for new user, create with third parameter as false

        }
        //check if messages are full
        //send message to user with ~@ to indicate this message is a flag
        //if messages are NOT full:
            //receive message
            //put message in clients' message inbox
        //if messages ARE full:
            //send message to user saying messages are full
    }

    public void sendMessageToAllConnectedUsers()
    {
        //For loop going through all clients:
            //if connected:
                //if messages are NOT full:
                    //put message in client's message box
                    //send feedback to user saying msg sent to client's name
                //else:
                    //send feedback to user saying client's name's inbox is full
    }

    public void sendMessageToAllKnownUsers()
    {
        //For loop going through all clients
            //if messages are NOT full:
                //put message in client's message box
                //send feedback to user saying msg sent to client's name
            //else:
                //send feedback to user saying client's name's inbox is full
    }
    public void getMyMessages()
    {
        //Go to user's client //How? client ID?
        //For loop going through user/client's messages:
            //if message != "":
                //append /n + message to line 
                //set message to ""
        //if line == "": //meaning every message was empty
            //line = "/n You have no messages.\n"
        //send line to user
    }

    //check if duplicate client names
    public void checkIfDuplicateName()
    {
        for(int x = 0; x< SocketThrdServer.clients.size(); x++)
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