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
import java.text.SimpleDateFormat;
import java.util.*;
import java.text.DateFormat;

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

            // Send response back to client
            line = "Hi " + clientName;
            write(line);

            //check if name is a duplicate/already in use by a client
            checkIfDuplicateName();

            do
            {
                //receive menu choice from user
                read();
                int temp2 = Integer.parseInt(line.trim());
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
                        this.closeSocket();
                        break;
                    default:
                        System.out.println("Invalid Menu entry");
                        break;
                }
            }while(this.connected);


    }
    public void setClientName(String name)
    {
        clientName = name;
    }

    public void displayAllKnownUsers()
    {
        //Go through array of ClientWorkers up until count of ClientWorkers
        //append name to mega-message of known usernames
        //each username has a newline before the name to format the message.
        write("~!2");
        index = 0;
        loopMax = SocketThrdServer.clients.size();
        while(index < loopMax){
            line = "\n";
            line += "" + index + ") ";
            line += SocketThrdServer.clients.get(index).clientName;
            write(line);
            index++;
        }
        write("\n");
        //toggle isLooping off
        write("~!2");
    }

    public void displayNamesOfConnectedUsers()
    {
        write("~!2");
        index = 0;
        loopMax = SocketThrdServer.clients.size();
        while(index < loopMax){
            //check if client at index is connected, if so add name to message
            if(SocketThrdServer.clients.get(index).connected){
                line = "\n";
                line += "" + index + ") ";
                line += SocketThrdServer.clients.get(index).clientName;
                write(line);
            }//end if
            index++;
        }//end while loop
        write("~!2");
    }

    public void sendMessageToUser()
    {
        duplicate = false;
        write("~!2");

        write("\nChoose a user to send message to:");
        
        //compile list of users to send to user        
        index = 0;
        loopMax = SocketThrdServer.clients.size();
        while(index < loopMax){
            System.out.println("index: "+index+" , loopMax: "+loopMax);
            line = "\n";
            line += "" + index + ") ";
            line += SocketThrdServer.clients.get(index).clientName;
            write(line);
            index++;
        }
        //add option to send message to unknown user
        line = "\n";
        line += "" + index + ") Other User";
        write(line);
        write("User choice: ");
        //toggle isLooping off
        write("~!2");

        //update line to reflect user's choice
        read();
        tempInt = Integer.parseInt(line);
        write("~!3"+index);
        System.out.println("~!3"+index);
        //if choice is new user
        if(tempInt == index)
        {
            //ask for new user's name/receive user's name
            write("\nEnter the name of the message recipient:\n");

            //check that new user's name is not a duplicate
            read();         // line now equals what the user entered
            System.out.println("name of person to send to :"+line);
            duplicate = checkIfNameExists(line);
            System.out.println("Got past duplicate");
            System.out.println(duplicate);
            if(duplicate)
            {
                System.out.println("Got to duplicate if");
                //send flag that it is a duplicate
                write("~!0");
                write("\nThat user already exists. Exiting to main menu.\n");
                //toggle duplicate on client side
                write("~!0");
                //end sendMessageToUser() if duplicate
                return;
            }
            // make new client
            SocketThrdServer.newUser(false);
            SocketThrdServer.setClientName(line, SocketThrdServer.clients.size());

        }
        System.out.println("About to check");
        //check if messages are full
        tempBool = checkIfInboxFull(tempInt);

        System.out.println("Checked inbox if full, tempBool: "+tempBool);
        //if messages are NOT full:
        if(!tempBool)
        {
            //prompt user
            write("\nWhat message would you like to send? Limit is 80 characters: \n");

            //receive message
            read();

            //Truncate to 80 characters
            if (line.length() > 80)
            {
                line = line.substring(0, 79);
            }

            //put message in clients' message inbox
            insertMessage(tempInt, line);
            write("\nMessage has been sent.\n");
        }
        //else messages ARE full:
        else
        {
            //tell user messages are full
            write("~!1");
            //send message to user saying messages are full
            write("\nCannot send message. Inbox full.\n");
            //toggle isFull on client side
            write("~!1");
        }

    }

    public void sendMessageToAllConnectedUsers()
    {
        //prompt user
        write("\nWhat message would you like to send? Limit is 80 characters: \n");

        //receive message
        read();

        //Truncate to 80 characters
        if (line.length() > 80)
        {
            line = line.substring(0, 79);
        }


        //loop going through all clients:
        index = 0;
        loopMax = SocketThrdServer.clients.size();
        //send isLooping flag
        write("~!2");
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
            index++;
        }
        //toggle isLooping
        write("~!2");
    }

    public void sendMessageToAllKnownUsers()
    {
        //prompt user
        line = "\nWhat message would you like to send? Limit is 80 characters: \n";
        write(line);

        //receive message
        read();

        //Truncate to 80 characters
        if (line.length() > 80)
        {
            line = line.substring(0, 79);
        }

        //loop going through all clients:
        index = 0;
        loopMax = SocketThrdServer.clients.size();
        //send isLooping flag
        write("~!2");
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
            index++;
        }
        //toggle isLooping
        write("~!2");

    }
    public void getMyMessages() 
    {
        //toggle isLooping on
        write("~!2");
        line = "";
        //For loop going through user/client's messages:
        for (int i = 0; i < 10; i++) {
            //if message exists:
            if (this.messages[i] != null) 
            {
                //send message
                line = "\n";
                line += this.messages[i];
                write(line);

                //clear message
                this.messages[i] = null;
            }
        }
        if(line == "")    //meaning every message was empty
        {
            line = "\nYou have no messages.\n";
            write(line);
        }
        //toggle isLooping off
        write("~!2");
    }

    //check if duplicate client names
    public void checkIfDuplicateName()
    {
            for( int x=0; x<SocketThrdServer.clients.size(); x++)
            {
                ClientWorker c = SocketThrdServer.clients.get(x);
                if((c.clientName.equals(this.clientName)) && (c.clientID!=this.clientID))
                {
                    if (c.connected == true)
                    {
                        System.out.println("Error: Client attempted to use client name already in use");
                        this.closeSocket();
                    }
                    else
                    {
                        write("~!0");
                        write("This user already exists. Would you like to claim it? \n1) Yes \n2) No\nEnter option number indicating your choice: ");

                        //get users response to the question
                        read();
                        int temp = Integer.parseInt(line.trim());
                        if (temp==1) //user wants to claim already used username
                        {
                            temp =this.clientID;
                            this.clientID = c.clientID;
                            this.messages = c.messages;
                            System.out.println("Client ID: "+temp+", has claimed Client ID: "+ this.clientID);
                            write("~!0");
                            //remove old client
                            SocketThrdServer.clients.remove(temp);
                            SocketThrdServer.client_count--;
                            return;

                        }
                        else if (temp==2)
                        {
                            this.closeSocket();
                        }

                    }
                    //if so, send error message and do not allow another thread to handle that client
                    //if not, allow thread to handle that client and set c.connected to true
                }
                else if(x == SocketThrdServer.clients.size()-1)
                {
                    write("Connected To Server");
                }
            }

    }

    //returns true if name sent as parameter is a known client name
    public boolean checkIfNameExists(String name)
    {
        System.out.println("Checking if name exists for: "+name);
        for(int i = 0; i < SocketThrdServer.client_count; i++)
        {
            if(SocketThrdServer.clients.get(i).clientName == name.trim())
            {
                System.out.println("Name does exist");
                return true;
            }
        }
        System.out.println("Name does not exist");
        return false;
    }

    public boolean checkIfInboxFull(int ID){
        for(int i = 0; i < 10; i++)
        {
            //if any message is empty
            if(SocketThrdServer.clients.get(ID).messages[i] == null)
            {
                System.out.println("Inbox was not full");
                return false;
            }
        }
        System.out.println("Inbox was full");
        return true;
    }

    public void insertMessage(int ID, String m){
        String temp_m;
        DateFormat form = new SimpleDateFormat("MM/dd/yy hh:mm:ss a");
        Date currentDate = new Date();
        temp_m = "From: "+this.clientName+", ";
        temp_m += form.format(currentDate)+", "+m;
        for(int i = 0; i < 10; i++)
        {
            //find first empty message slot
            if(SocketThrdServer.clients.get(ID).messages[i] == null)
            {
                //store message
                SocketThrdServer.clients.get(ID).messages[i] = temp_m;
                return;
            }
        }
    }

    // read in line from client
    public void read()
    {
        try {
            do {

            }while(!in.ready());
        }catch(IOException in_wrong)
        {
            System.out.println("Error: couldn't receive, ClientWorker_BufferedReader not ready");
        }

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
            System.out.println("Write has been called");
     //       System.out.println("Line is " + line);
            System.out.println("toSend is " + toSend);
            //send response to client
            out.println(toSend);
    }
    // close socket
    public void closeSocket()
    {
        try
        {
            write("~!9");
            this.connected=false;
            System.out.println("Client: "+this.clientName+", ID: "+this.clientID+" has been disconnected");
            client.close();
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
    public static int client_count, workers_count =0;
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
        }
    }

    public static void clientMaker(boolean isConnected)
    {
        System.out.println("Started making client");
        System.out.println("client count:"+SocketThrdServer.client_count);
        ClientWorker w;
        try
        {
            w = new ClientWorker(server.accept(), SocketThrdServer.client_count, isConnected);
            System.out.println("Got here ~~~~~~~~~");
            SocketThrdServer.clients.add(w);
            System.out.println("Client ID: "+w.clientID+" is made");
            SocketThrdServer.client_count++;
        }
        catch (IOException e)
        {
            System.out.println("Accept failed");
            System.exit(-1);
        }

        System.out.println("finished making client");
    }

    public static void newUser(boolean isConnected)
    {
        System.out.println("Started adding unknown user");
        
        ClientWorker w = new ClientWorker(null, SocketThrdServer.client_count, isConnected);
        System.out.println("Got here ~~~~~~~~~");
        SocketThrdServer.clients.add(w);
        System.out.println("Client ID: "+w.clientID+" is made");
        SocketThrdServer.client_count++;
    

        System.out.println("finished adding unknown user");
    }

    public void threadMaker()
    {
        Thread t = new Thread(clients.get(workers_count));
        workers.add(t);
        workers.get(workers_count).start();
        System.out.println("Client ID: "+clients.get(workers_count).clientID+" is connected");
        workers_count++;
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