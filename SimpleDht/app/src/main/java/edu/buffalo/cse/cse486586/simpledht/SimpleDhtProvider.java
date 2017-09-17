package edu.buffalo.cse.cse486586.simpledht;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.telephony.TelephonyManager;
import android.util.Log;

import org.w3c.dom.Node;

import static android.content.ContentValues.TAG;

// Reference: Context: https://developer.android.com/reference/android/content/Context.html#getApplicationInfo()
// Reference: Previous assignment PA2A and PA2B codes
// Reference: Android: http://stackoverflow.com/questions/3554722/how-to-delete-internal-storage-file-in-android
// Reference: Android: http://stackoverflow.com/questions/12310577/iterate-through-a-specified-directory-in-android
// Reference: Content Values - https://developer.android.com/reference/android/content/ContentProvider.html#insert(android.net.Uri,%20android.content.ContentValues)
// Reference: Some suggestions from Piazza posts!
// Reference: Socket Programming - https://developer.android.com/reference/java/net/Socket.html
// Reference: List - https://docs.oracle.com/javase/tutorial/collections/interfaces/list.html

public class SimpleDhtProvider extends ContentProvider {
    String myID;
    int portnumber = 0;
    boolean stop = false;
    int leaderport = 11108;
    String pred = null;
    String succ = null;
    HashMap<String,String> map_Hash = new HashMap<String, String>();
    List<String> hash_values = new ArrayList<String>();
    List<String> Nodes = new ArrayList<String>();



    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // TODO Auto-generated method stub
        try {
        if (Nodes.size() == 1) {
            String filename = selection;
            getContext().deleteFile(filename);
            System.out.println("File deleted");
        }
        else {
                String filename = selection;
                String current_hash = genHash(filename);
                String node_Hash = null;
                for (int i = 0; i < Nodes.size(); i++) {
                    String locHash = Nodes.get(i);


                    if (locHash.compareTo(current_hash) >= 0) {
                        node_Hash = locHash;
                        break;

                    } else {
                        if (i == (Nodes.size() - 1)) {
                            node_Hash = Nodes.get(0);
                        }
                    }

                }

            String avd = map_Hash.get(node_Hash);

//                if (node_Hash.equals(genHash(String.valueOf(portnumber / 2)))) {
            if(avd.equals(myID)){
                System.out.println("Delete:Key" + filename + ":TobeDeleted from " + myID);
                getContext().deleteFile(filename);
            }
            else
            {
                String insert_msg="DeleteFrom";
                String avd_fname = avd+":"+filename;
                new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, insert_msg, avd_fname);
            }


            }

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return 0;
    }
    @Override
    public String getType(Uri uri) {
        // TODO Auto-generated method stub
        return null;
    }


    @Override
    public Uri insert(Uri uri, ContentValues values) {

        try {
            String filename = (String) values.get("key");
            String val = (String) values.get("value");
            String node_Hash = null;
            String current_hash = genHash(filename);
            System.out.println("InsideInsert");
            Collections.sort(Nodes);
            System.out.println("Hash_Values List:" + Nodes);
            if (Nodes.size() == 1) {
                System.out.println("InsertNonLeader");
                System.out.println("InsertKey" + filename);
                System.out.println("InsertVal" + val.toString());
                OutputStreamWriter outputStreamWriter = new OutputStreamWriter(getContext().openFileOutput(filename, Context.MODE_PRIVATE));
                outputStreamWriter.write(val.toString());
                outputStreamWriter.close();
                System.out.println("INSERTNonLeader:Key" + filename + ":TobeStoredIn" + portnumber);
            } else {

                for (int i = 0; i < Nodes.size(); i++) {
                    String locHash = Nodes.get(i);

                    if (locHash.compareTo(current_hash) >= 0) {
                        node_Hash = locHash;
                        break;

                    }
                    else{
                        if (i== (Nodes.size()-1)){
                            node_Hash=Nodes.get(0);
                        }
                    }

                }


//            if(hash_values.size()!=0)
//            {
//                for(int i=0;i<hash_values.size();i++)
//            {
//                System.out.println("EnteringInsertForLoop");
//                if(hash_values.size()==1)
//                {
//                    node_Hashval=hash_values.get(0);
//                    System.out.println("InsertLeader");
//                    break;
//                }
//                    if ((hash_values.get(i)).compareTo(current_hash) >= 0)
//                    {
//                            node_Hashval = hash_values.get(i);
//                            break;
//                    }
//                else{
//                        if(i<(hash_values.size()-1))
//                        continue;
//                }
//
//                        node_Hashval = hash_values.get(0);
//            }
//
//                System.out.println("Found position for "+filename + " at "+node_Hashval);
//            }
//            if(Nodes.size()==1)
//            {
//                System.out.println("InsertNonLeader");
//                System.out.println("InsertKey"+filename);
//                System.out.println("InsertVal"+val.toString());
//                OutputStreamWriter outputStreamWriter = new OutputStreamWriter(getContext().openFileOutput(filename, Context.MODE_PRIVATE));
//                outputStreamWriter.write(val.toString());
//                outputStreamWriter.close();
//                System.out.println("INSERTNonLeader:Key"+filename+":TobeStoredIn"+portnumber);
//            }
                    String avd = map_Hash.get(node_Hash);

//                if (node_Hash.equals(genHash(String.valueOf(portnumber / 2)))) {
                    if(avd.equals(myID)){
                    System.out.println("INSERT:Key" + filename + ":TobeStoredIn" + portnumber);
                    OutputStreamWriter outputStreamWriter = new OutputStreamWriter(getContext().openFileOutput(filename, Context.MODE_PRIVATE));
                    outputStreamWriter.write(val.toString());
                    outputStreamWriter.close();
                }
                else
                    {
                        String insert_msg="InsertInto";
                        String avd_fname_val = avd+":"+filename+":"+val;
                        new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, insert_msg, avd_fname_val);
                    }
                //  System.out.println("File Name is:"+filename);
                //  System.out.println("Value Name is:"+val.toString());

            }
        }catch (Exception e) {
            Log.e(TAG, "File write failed");
        }
        Log.v("insert", values.toString());
        return uri;
        
        // TODO Auto-generated method stub
    }

    @Override
    public boolean onCreate() {

        TelephonyManager tel = (TelephonyManager) getContext().getSystemService(Context.TELEPHONY_SERVICE);
        String portStr = tel.getLine1Number().substring(tel.getLine1Number().length() - 4);
        final String myPort = String.valueOf((Integer.parseInt(portStr) * 2));
        portnumber = Integer.parseInt(myPort);
        myID=portStr;

        try {
            Nodes.add(genHash(portStr));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        if(portnumber != leaderport)
        {

            System.out.println("Sending request to join " + (portnumber/2));
            String request_msg = "Request2Join";
            new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, request_msg, myPort);

        }
        if(portnumber == leaderport)
        {
            try {
                map_Hash.put(genHash(portStr),portStr);
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }


//            if (hash_values.size() == 0) {
//                int val = leaderport / 2;
//                String hash = null;
//                try {
//                    hash = genHash(String.valueOf(val));
//                } catch (NoSuchAlgorithmException e) {
//                    e.printStackTrace();
//                }
//                map_Hash.put(hash,String.valueOf(val));
//                hash_values.add(hash);
//                System.out.println("Leader Added to List:");
//
//            }
        }

        try {

            ServerSocket serverSocket = new ServerSocket(10000);
            new ServerTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, serverSocket);
        } catch (IOException e) {

            Log.e(TAG, "Can't create a ServerSocket"+myPort);
            return false;
        }

        // TODO Auto-generated method stub
        return false;
    }

    public void UpdateNeighbors()
    {
//        int last=hash_values.size();
        System.out.println("MAP is " + map_Hash);

        System.out.println("Entering UpdateNeighbors");
        for(int i=0;i<Nodes.size();i++)
        {

            String node = map_Hash.get(Nodes.get(i));
//            String predecessor = null;
//            String successor = null;

//            if(i==0)
//            {
//                predecessor = hash_values.get(last - 1);
//                successor = hash_values.get(i+1);
//            }
//            else if(i==(last-1))
//            {
//                successor =hash_values.get(0);
//                predecessor =hash_values.get(i-1);
//            }
//            else
//            {
//                predecessor =hash_values.get(i-1);
//                successor = hash_values.get(i+1);
//            }
//            String clientId = map_Hash.get(hash_values.get(i));
//            String pred = map_Hash.get(predecessor);
//            String succ = map_Hash.get(successor);
            //System.out.println("ClientID is " );
            if ((node.equals("5554"))) {

            }
            else{
//            System.out.println("SENDING Table data to all clients pred is " + pred + " suc is " + succ + " node is " + clientId);
//            String sendNeighborInfo = "client:" + clientId + ":pre:" + pred + ":suc:" + succ;
//            System.out.println("Neighbor info:" + sendNeighborInfo);
                System.out.println("CURRENT MAP is "+map_Hash);
            new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, "DistMap",node);
        }
        }

    }

    private class ServerTask extends AsyncTask<ServerSocket, String, Void> {
        @Override

        protected Void doInBackground(ServerSocket... sockets) {
            ServerSocket serverSocket = sockets[0];
            while (true) {
                try {
                    //    Log.v(TAG, "Server Socket");
                    Socket leader = serverSocket.accept();
                    InputStreamReader input = new InputStreamReader(leader.getInputStream());
                    BufferedReader reader = new BufferedReader(input);
                    System.out.println("check message type");
                    String Message;
                    if ((Message = reader.readLine()) != null)                    //Read string from client side
                    {
                        System.out.println("Received at SERVER "+Message);
                        System.out.println("Message not null");
//                    if (Message.contains("Request2Join") && hash_values.size() != 0) {
//                        if (Message.contains("Request2Join")){
                        if (Message.contains("JOIN")){

                            String[] getIdarray = Message.split(":");
                        int port_Num = Integer.parseInt(getIdarray[1]);
                        int avd_id = port_Num / 2;
                        String hash_val = genHash(String.valueOf(avd_id));
                        System.out.println("Avd ID:" + avd_id);
                        System.out.println("Received request to join from "+avd_id);
//                            PrintStream printer = new PrintStream(leader.getOutputStream());             //Write over the socket
//                            String messagesendback = "Request Received:" + filename + ":" + p;
//                            printer.println(messagesendback);
//                            printer.flush();
                        map_Hash.put(hash_val, String.valueOf(avd_id));
//                        hash_values.add(hash_val);
                            Nodes.add(hash_val);
                        System.out.println("Hash Values Array:Before Sort" + hash_values);
//                        Collections.sort(hash_values);
                            Collections.sort(Nodes);
                        System.out.println("Hash Values Array:After Sort" + hash_values);
                        UpdateNeighbors();


                    }
                    if(Message.equals("UpdMap")){
                        Nodes.clear();
                        int num = Integer.parseInt(reader.readLine());
                        for(int i = 0;i<num;i++){
                            String s = reader.readLine();
                            String[] map = s.split(":");
                            map_Hash.put(map[0],map[1]);
                            Nodes.add(map[0]);
                            Collections.sort(Nodes);
                        }
                        System.out.println("Updated MAP at " + portnumber + map_Hash);

                    }
                    if(Message.equals("DeleteFile"))
                    {
                        String fname = reader.readLine();
                        getContext().deleteFile(fname);
                        System.out.println("Delete Successful");
                    }
                    if (Message.equals("InsertTo"))
                    {
                        String fname = reader.readLine();
                        String val = reader.readLine();
                        System.out.println("INSERT(ServerSide):Key" + fname + ":TobeStoredIn" + portnumber);
                        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(getContext().openFileOutput(fname, Context.MODE_PRIVATE));
                        outputStreamWriter.write(val.toString());
                        outputStreamWriter.close();

                    }
                    if(Message.equals("QueryPresent"))
                    {

                        String filename = reader.readLine();
                        System.out.println("query received at " + myID + " query is " + filename);

                        FileInputStream fis = getContext().openFileInput(filename);
                        InputStreamReader isr = new InputStreamReader(fis);
                        BufferedReader bufferedReader = new BufferedReader(isr);
                        String p;
                        p = bufferedReader.readLine();
                        PrintStream printer = new PrintStream(leader.getOutputStream());             //Write over the socket
                        //String messageback = "MessageBack:";
                        String query_return = filename + ":" + p;
                        //printer.println(messageback);
                        //printer.flush();
                        printer.println(query_return);
                        System.out.println("Sent back query result "+query_return );
                        printer.flush();
                        //matrixCursor.addRow(new Object[]{filename, p});
                    }
                    if(Message.equals("PerformStar"))
                    {
                        //System.out.println("Selection is "+selection);
                        System.out.println("Inside: Perform Star");
                        String appPath = getContext().getApplicationContext().getFilesDir().getPath();
                        System.out.println("App Path: " + appPath);
                        File path = new File(appPath);
                        File[] files = path.listFiles();
                        int count = files.length;
                        PrintStream printer = new PrintStream(leader.getOutputStream());
                        printer.println(count);
                        printer.flush();
                        for (int i = 0; i < files.length; i++) {
                            System.out.println("InsideFor:");
                            if (files[i].isFile()) {
                                System.out.println(files[i]);
                                String filedir = String.valueOf(files[i]);
                                String[] filenamearr = filedir.split("/");
                                String filename = filenamearr[5];
                                System.out.println("filename is:"+filename);
                                FileInputStream fis = getContext().openFileInput(filename);
                                InputStreamReader isr = new InputStreamReader(fis);
                                BufferedReader bufferedReader = new BufferedReader(isr);
                                String p;
                                p = bufferedReader.readLine();
                                System.out.println("P is:" + p);

                                printer.println(filename+":"+p);
                                printer.flush();
                            }
                        }
                    }

                }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private class ClientTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... msgs) {

            //int[] PORTS = {11108,11112,11116,11120,11124};                              //Array of all 5 port values
            try {
                //for (int i : myPortsList )                                                //Loop over all ports to multicast

                if(msgs[0].contains("Request2Join")) {
                    System.out.println("Trying to send request to Leader to join port "+portnumber);
                    //Log.e(TAG, "Inside ClientTask");
                    Socket socket = null;
//                    while(!socket.isConnected())
//                    do {
                        socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}), leaderport);
//                        stop=true;
//                    }while(!(socket.isConnected()));
                    System.out.println("connected to leader");
                    //Log.e(TAG, "Client Socket Created");


//                    String msg_request = msgs[0].trim();                                                         //Message to be sent
                    String msg_request = "JOIN";
                    PrintStream ps = new PrintStream(socket.getOutputStream());
                    msg_request = msg_request + ":" + portnumber;                                           //Send message and sender portnumber
                    System.out.println("MSG_REQUEST:" + msg_request);
                    ps.println(msg_request);
//                    ps.flush();
                    Thread.sleep(100);
                    System.out.println("Sent request to leader");

                }
                if(msgs[0].equals("DeleteFrom"))
                {
                    String[] avd_fname = msgs[1].split(":");
                    String avd = avd_fname[0];
                    String file = avd_fname[1];
                    Socket client = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}), Integer.parseInt(avd)*2);
                    PrintStream psc = new PrintStream(client.getOutputStream());
                    String msg_client = "DeleteFile";
                    psc.println(msg_client);
                    psc.flush();
                    psc.println(file);
                    psc.flush();
                }
                if(msgs[0].equals("InsertInto"))
                {
                String[] avd_fname_val = msgs[1].split(":");
                    String avd = avd_fname_val[0];
                    String file = avd_fname_val[1];
                    String val = avd_fname_val[2];
                    Socket client = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}), Integer.parseInt(avd)*2);
                    PrintStream psc = new PrintStream(client.getOutputStream());
                    String msg_client = "InsertTo";
                    psc.println(msg_client);
                    psc.flush();
                    System.out.println("Sending File: "+file+" and Val "+val+" to avd "+avd);
                    psc.println(file);
                    psc.flush();
                    psc.println(val);
                    psc.flush();
                }
                if(msgs[0].equals("DistMap"))
                {
                    String clientPort = msgs[1];
//                    String msg_neighbor = msgs[0].trim();                                                         //Message to be sent
//                    String[] msg_neighbor_array = msg_neighbor.split(":");
//                    String clientId = msg_neighbor_array[1];
//                    int clientPort = Integer.parseInt(clientId)*2;
//                     pred = msg_neighbor_array[3];
//                     succ = msg_neighbor_array[5];

                    Socket client = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}), Integer.parseInt(clientPort)*2);
                    PrintStream psc = new PrintStream(client.getOutputStream());
                    String msg_client = "UpdMap";
//                    System.out.println("Neighbor_msg:" + msg_client);
                    psc.println(msg_client);
                    psc.flush();
//                    System.out.println("Printing MAP to send " + map_Hash);
                    System.out.println("Sending MAPINFO to " + clientPort);
                    psc.println(map_Hash.size());
                    psc.flush();
                    for(String key : map_Hash.keySet())
                    {
//                        String mapinfo = "MapInfo:"+key+":"+map_Hash.get(key);
                                                String mapinfo = key+":"+map_Hash.get(key);
                        System.out.println("Sending MapInfo: "+mapinfo);
                        psc.println(mapinfo);
                        psc.flush();
                    }

                }
                if(msgs[0].equals("QueryForward"))
                {

                    String msg_query = msgs[1].trim();                                                         //Message to be sent
                    String[] msg_query_array = msg_query.split(":");
                    String presentinavd = msg_query_array[1];
                    String keytoquery = msg_query_array[3];
                    System.out.println("Present in Avd"+presentinavd+"Key to Query "+keytoquery);
                    int QueryPort = Integer.parseInt(presentinavd)*2;
                    Collections.sort(Nodes);

                    Socket Queryclient = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}), QueryPort);
                    PrintStream psq = new PrintStream(Queryclient.getOutputStream());
                    String query_client = "QueryPresent";
                    psq.println(query_client);
                    psq.flush();
                    System.out.println("Sending Message"+query_client+" and key "+keytoquery+" to "+presentinavd);
                    psq.println(keytoquery);
                    psq.flush();
                    Thread.sleep(100);
                    InputStreamReader input = new InputStreamReader(Queryclient.getInputStream());   //Read over the socket
                    BufferedReader reader = new BufferedReader(input);
                    String query_return = reader.readLine();
                    System.out.println("Received from " + QueryPort + " message " + query_return);
                    String[] query_array = query_return.split(":");
                    String fname = query_array[0];
                    String p_val = query_array[1];
                    System.out.println("Fname returned:"+fname+"Pval returned:"+p_val);
                    return fname+":"+p_val;

                }
                if(msgs[0].equals("PerformAll"))
                {
                      List<String> tosend = new ArrayList<String>();
                      for(String key : map_Hash.keySet())
                      {
                          tosend.add(map_Hash.get(key));
                          System.out.println("To send list is (before)"+tosend);
                      }
                      tosend.remove(myID);
                      System.out.println("To send list is (after)"+tosend);
                    //int total_count=0;
                    String toreturn="firsttime";
                    for(int j=0;j<tosend.size();j++) {

                        int socket = Integer.parseInt(tosend.get(j))*2;
                        System.out.println("Sending to"+socket);
                        System.out.println("Performing Star on " + myID + " Originated by " + msgs[1]);
                        Socket Queryclient = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}), socket);
                        PrintStream psq = new PrintStream(Queryclient.getOutputStream());
                        String query_client = "PerformStar";
                        psq.println(query_client);
                        psq.flush();
                        System.out.println("Sending Message" + query_client);
                        Thread.sleep(100);

                        InputStreamReader input = new InputStreamReader(Queryclient.getInputStream());   //Read over the socket
                        BufferedReader reader = new BufferedReader(input);
                        int count = Integer.parseInt(reader.readLine());

                        for (int i = 0; i < count; i++) {
                            String recv = reader.readLine();
                            String[] recvarr = recv.split(":");
                            String fname = recvarr[0];
                            String p_val = recvarr[1];
                            System.out.println("Fname returned:" + fname + "Pval returned:" + p_val);
                            if(toreturn.equals("firsttime"))
                            toreturn = fname + ":" + p_val + "-";
                            else
                                toreturn = toreturn + fname + ":" + p_val + "-";
                            System.out.println("To return " + toreturn);
                        }
                        //total_count = total_count + count;
                    }
                    return toreturn;
                }

            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }
    }


    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
            String sortOrder) {
        System.out.println("Nodes list is " + Nodes);
        System.out.println("Query is "+selection);
        MatrixCursor matrixCursor = new MatrixCursor(new String[]{"key", "value"});
        try {


            System.out.println("Selection:"+selection);
            if(Nodes.size()==1)
            {
                System.out.println("Single Node Query");
                if(selection.equals("@") || selection.equals("*"))
                {
                    System.out.println("Inside@/*:Single Node Query");
                    String appPath = getContext().getApplicationContext().getFilesDir().getPath();
                    System.out.println("App Path: " + appPath);
                    File path = new File(appPath);
                    File[] files = path.listFiles();
                    for (int i = 0; i < files.length; i++) {
                        System.out.println("InsideFor:");
                        if (files[i].isFile()) {
                            System.out.println("InsideIf2:");
                            System.out.println(files[i]);
                            String filedir = String.valueOf(files[i]);
                            String[] filenamearr = filedir.split("/");
                            String filename = filenamearr[5];
                            System.out.println("filename is:"+filename);
                            FileInputStream fis = getContext().openFileInput(filename);
                            InputStreamReader isr = new InputStreamReader(fis);
                            BufferedReader bufferedReader = new BufferedReader(isr);
                            //StringBuilder sb = new StringBuilder();
                            String p;
                            //System.out.println("InsideWhile:");
                            p = bufferedReader.readLine();
                            System.out.println("P is:" + p);
                            matrixCursor.addRow(new Object[]{filename, p});
                        }
                    }
                }
                else
                {
                    System.out.println(selection);
                    String filename = selection;
                    FileInputStream fis = getContext().openFileInput(selection);
                    InputStreamReader isr = new InputStreamReader(fis);
                    BufferedReader bufferedReader = new BufferedReader(isr);
                    StringBuilder sb = new StringBuilder();
                    String line, p = null;
                    while ((line = bufferedReader.readLine()) != null) {
                        p = line;
                    }
                    System.out.println("VALLLUEUEUUEUEUEUEE =" + sb);
                    matrixCursor.addRow(new Object[]{filename, p});
                    //return matrixCursor;
                }
            }
            else
            {
                System.out.println("Inside else");
                if(selection.equals("*"))
                {
                    System.out.println("Selection is "+selection);
                    System.out.println("Inside*:Add Self Files");
                    String appPath = getContext().getApplicationContext().getFilesDir().getPath();
                    System.out.println("App Path: " + appPath);
                    File path = new File(appPath);
                    File[] files = path.listFiles();
                    for (int i = 0; i < files.length; i++) {
                        System.out.println("InsideFor:");
                        if (files[i].isFile()) { //this line weeds out other directories/folders
                            System.out.println("InsideIf2:");
                            System.out.println(files[i]);
                            String filedir = String.valueOf(files[i]);
                            String[] filenamearr = filedir.split("/");
                            String filename = filenamearr[5];
                            System.out.println("filename is:"+filename);
                            FileInputStream fis = getContext().openFileInput(filename);
                            InputStreamReader isr = new InputStreamReader(fis);
                            BufferedReader bufferedReader = new BufferedReader(isr);
                            //StringBuilder sb = new StringBuilder();
                            String p;
                            //System.out.println("InsideWhile:");
                            p = bufferedReader.readLine();
                            System.out.println("P is:" + p);
                            matrixCursor.addRow(new Object[]{filename, p});
                        }
                    }
                    String sendtoall = "PerformAll";
                    String strreturned = new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, sendtoall,myID).get();
                    System.out.println("Str Returned "+strreturned);
                    String[] strarray =strreturned.split("-");
                    int num = strarray.length;
                    for(int i=0;i<num;i++) {
                        String[] str =strarray[i].split(":");
                        String fname = str[0].trim();
                        String pval = str[1].trim();
                        System.out.println("Fname:Before adding: " + fname);
                        System.out.println("Pval:Before adding: " + pval);
                        matrixCursor.addRow(new Object[]{fname, pval});
                    }
                }
                if(selection.equals("@"))
                {
                    System.out.println("Selection is "+selection);
                    System.out.println("Inside@:Add Self Files");
                    String appPath = getContext().getApplicationContext().getFilesDir().getPath();
                    System.out.println("App Path: " + appPath);
                    File path = new File(appPath);
                    File[] files = path.listFiles();
                    for (int i = 0; i < files.length; i++) {
                        System.out.println("InsideFor:");
                        if (files[i].isFile()) {
                            System.out.println("InsideIf2:");
                            System.out.println(files[i]);
                            String filedir = String.valueOf(files[i]);
                            String[] filenamearr = filedir.split("/");
                            String filename = filenamearr[5];
                            System.out.println("filename is:"+filename);
                            FileInputStream fis = getContext().openFileInput(filename);
                            InputStreamReader isr = new InputStreamReader(fis);
                            BufferedReader bufferedReader = new BufferedReader(isr);
                            //StringBuilder sb = new StringBuilder();
                            String p;
                            //System.out.println("InsideWhile:");
                            p = bufferedReader.readLine();
                            System.out.println("P is:" + p);
                            matrixCursor.addRow(new Object[]{filename, p});
                        }
                    }
                }

                if(!(selection.equals("*")) && !(selection.equals("@")))
                {
                    System.out.println("Query is single query" + selection);
                    System.out.println("QueryingNormal");
                    System.out.println(selection);
                    String filename = selection;
                    System.out.println("Selection is "+selection);
                    //String keyhash = genHash(filename);
                    String node_Hash = null;
                    String current_hash = genHash(filename);
                    System.out.println("QueryingFile "+selection+" HashValue "+current_hash);
                    Collections.sort(Nodes);
                    for (int i = 0; i < Nodes.size(); i++) {
                        String locHash = Nodes.get(i);

                        if (locHash.compareTo(current_hash) >= 0) {
                            node_Hash = locHash;
                            break;

                        }
                        else{
                            if (i== (Nodes.size()-1)){
                                node_Hash=Nodes.get(0);
                            }
                        }
                    }
                    String avd = map_Hash.get(node_Hash);

//                if (node_Hash.equals(genHash(String.valueOf(portnumber / 2)))) {
                    if(avd.equals(myID)) {

                        System.out.println("Query is in myID "+myID+"Filename "+filename);
                        FileInputStream fis = getContext().openFileInput(selection);
                        InputStreamReader isr = new InputStreamReader(fis);
                        BufferedReader bufferedReader = new BufferedReader(isr);
                        String line, p = null;
                        while ((line = bufferedReader.readLine()) != null) {
                            p = line;
                        }
                        matrixCursor.addRow(new Object[]{filename, p});
                    }
                    else
                    {
                        String message2send = "QueryForward";
                        String msg2send ="PresentInAvd:"+avd+":Key:"+filename;
                        String str = new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, message2send,msg2send).get();
                        String[] strarray =str.split(":");
                        String fname = strarray[0].trim();
                        String pval = strarray[1].trim();
                        System.out.println("Fname:Before adding: "+fname);
                        System.out.println("Pval:Before adding: "+pval);
                        matrixCursor.addRow(new Object[]{fname,pval});
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "File query failed");
        }
return matrixCursor;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        // TODO Auto-generated method stub
        return 0;
    }

    private String genHash(String input) throws NoSuchAlgorithmException {
        MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
        byte[] sha1Hash = sha1.digest(input.getBytes());
        Formatter formatter = new Formatter();
        for (byte b : sha1Hash) {
            formatter.format("%02x", b);
        }
        return formatter.toString();
    }


}
