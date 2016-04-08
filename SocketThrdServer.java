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

    ClientWorker(Socket client, int id)
    {
        this.client = client;
        clientID = id;
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
//            checkIfDuplicateName();

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
                            System.out.println(" Invalid Menu");
                            break;
                }
            }while(client.isConnected());

            //Close Socket
            closeSocket();

    }
    //Start of client's menu options

    public void displayAllKnownUsers()
    {

    }

    public void displayNamesOfConnectedUsers()
    {

    }

    public void sendMessageToUser()
    {

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
        boolean uniqueName=false;
        do {
            for( ClientWorker c : SocketThrdServer.clients)
            {
                if((c.clientName == this.clientName) && (c.clientID!=this.clientID))
                {

                }
            }
        }while(!uniqueName);
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
                w = new ClientWorker(server.accept(), count);
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