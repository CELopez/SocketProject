//The server will:
// 1) Accept a port number as a command line argument.
// 2) Accept connections from clients.
// 3) Create a new thread for each client.
// 4) Store messages sent to each user.
// max of 10 messages per user, so String[10]
// max lentgh of messages is 80 characters
// remove messages from server when client gets them
// 5) End by termination with control-C.

//The server thread will:
// 1) Accept and process requests from the client.
// remove messages from server when client gets them
// 2) Add the users name to the list of known users.
// The same user name cannot have multiple connections at once...remove name once connection severed.
// max of 100 users
// 3) Provide mutual exclusion protection for the data structure that stores the messages. //messages are unique to the two user...only user that sent can retrieve msg
// 4) Send only the minimal data needed to the client, not the menu or other UI text.


//If server gets killed, kill all clients

import java.io.*;
import java.net.*;
import java.util.ArrayList;

class ClientWorker implements Runnable
{
    private Socket client;
    public int clientID, tempInt;
    private String line, clientName;
    private BufferedReader in = null;
    private PrintWriter out = null;
    private String[] messages = new String[10];
    private boolean connected, duplicate, tempBool;
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
/*
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
*/
        //Test run
        do {
            read();
            if(line.equals("7"))
            {
                closeSocket();
            }

        }while(this.connected);

        //Close Socket
        closeSocket();

    }
    public void setClientName(String name)
    {
        this.clientName = name;
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
        duplicate = false;

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
        tempInt = Integer.parseInt(line);
        //if choice is new user
        if(tempInt == index)
        {
            //ask for new user's name/receive user's name
            line = "\nEnter the name of the message recipient:\n";
            write(line);

            //check that new user's name is not a duplicate
            read();         // line now equals what the user entered
            duplicate = checkIfNameExists(line);
            if(duplicate == false)
            {
                //create new clients for new user, line holds new user's name
                SocketThrdServer.clientMaker(false);
                SocketThrdServer.setClientName(line, SocketThrdServer.count);
            }

            //send flag if duplicate or not
            line = "~@0"; // first send code indicating next line is duplicate boolean
            write(line);

            line = ""+duplicate;

            //end sendMessageToUser() if duplicate
            if(duplicate == true)
            {
                return;
            }
        }
        //check if messages are full
        tempBool = checkIfInboxFull(tempInt);
        //send message to user with ~@ to indicate this message is a flag
        line = "~@2"+tempBool;
        //if messages are NOT full:
        if(tempBool == false)
        {
            //receive message
            read();
            //put message in clients' message inbox
            insertMessage(tempInt, line);
        }


        //else messages ARE full:
        else
        {
            //send message to user saying messages are full
            line = "\nCannot send message. Inbox full.\n";
        }

    }

    public void sendMessageToAllConnectedUsers()
    {   
        //receive message
        read();

        //loop going through all clients:
        index = 0;
        loopMax = SocketThrdServer.clients.size();
        while(index < loopMax)
        {
            //if connected:
            if(SocketThrdServer.clients.get(index).connected)
            {
                // if messages are NOT full:
                if(!checkIfInboxFull(index))
                {
                   //put message in client's message box
                   insertMessage(index, line);
                   //send feedback to user saying msg sent to client's name
                   write("\nMessage sent to " + SocketThrdServer.clients.get(index).clientName + "\n");
                }
                else
                {
                    //send feedback to user saying client's name's inbox is full
                    write("\n" + SocketThrdServer.clients.get(index).clientName + "\'s inbox is full.\n");
                }
            }
        }        

    public void sendMessageToAllKnownUsers()
    {   
        //receive message
        read();

        //loop going through all clients:
        index = 0;
        loopMax = SocketThrdServer.clients.size();
        while(index < loopMax)
        {
            // if messages are NOT full:
            if(!checkIfInboxFull(index))
            {
               //put message in client's message box
               insertMessage(index, line);
               //send feedback to user saying msg sent to client's name
               write("\nMessage sent to " + SocketThrdServer.clients.get(index).clientName + "\n");
            }
            else
            {
                //send feedback to user saying client's name's inbox is full
                write("\n" + SocketThrdServer.clients.get(index).clientName + "\'s inbox is full.\n");
            }
        }  

    public void getMyMessages()
    {
        line = "";
        
        //For loop going through user/client's messages:
        for(int i = 0; i < 10; i++)
        {
            //if message != "":
            if(this.messages[i] != "")
            {
                line += this.messages[i];
                line += "\n";

                //clear message
                this.messages[i] = "";
            }     
        }
        if(line == "\n")    //meaning every message was empty
        {
            line = "\nYou have no messages.\n";
        }

        //send line to user
        write(line);
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
                    this.closeSocket();
                }
                else
                {
                    write("~!0");
                    write("This user already exists. Would you like to claim it? /n1) Yes /n2) No/nEnter option number indicating your choice: ");

                    read();


                }
                //if so, send error message and do not allow another thread to handle that client
                //if not, allow thread to handle that client and set c.connected to true
            }
        }
    }

    //returns true if name sent as parameter is a known client name
    public boolean checkIfNameExists(String name)
    {
        for(int i = 0; i < SocketThrdServer.clients.size(); i++)
        {
            if(SocketThrdServer.clients.get(i).clientName == name)
            {
                return true;
            }
        }
        return false;
    }

    public boolean checkIfInboxFull(int ID){
        for(int i = 0; i < 10; i++)
        {
            //if any message is empty
            if(SocketThrdServer.clients.get(ID).messages[i] == "")
            {
                return false;
            }
        }
        return true;
    }

    public void insertMessage(int ID, String m){
        for(int i = 0; i < 10; i++)
        {
            //find first empty message slot
            if(SocketThrdServer.clients.get(ID).messages[i] == "")
            {
                //store message
                SocketThrdServer.clients.get(ID).messages[i] = m;
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
            System.out.println("Client: "+this.clientName+", ID: "+this.clientID+" has been disconnected");
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
    static ServerSocket server = null;
    public static int count =0;
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
            clientMaker(true);
            threadMaker();
            count++;
        }
    }

    public static void clientMaker(boolean isConnected)
    {
        ClientWorker w;
        try
        {
            w = new ClientWorker(server.accept(), count, isConnected);
            clients.add(w);

        }
        catch (IOException e)
        {
            System.out.println("Accept failed");
            System.exit(-1);
        }
    }

    public void threadMaker()
    {
        Thread t = new Thread(clients.get(count));
        workers.add(t);
        workers.get(count).start();
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
    public static void setClientName(String name, int position)
    {
        clients.get(position).setClientName(name);

    }

}