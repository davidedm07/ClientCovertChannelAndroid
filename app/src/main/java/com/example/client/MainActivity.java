package com.example.client;
import android.content.Intent;
import android.os.Bundle;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import android.os.AsyncTask;
import android.app.Activity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends Activity {

    TextView textResponse;
    EditText editTextAddress, editTextPort;
    Button buttonConnect, buttonClear;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        editTextAddress = (EditText)findViewById(R.id.address);
        editTextPort = (EditText)findViewById(R.id.port);
        buttonConnect = (Button)findViewById(R.id.connect);
        buttonClear = (Button)findViewById(R.id.clear);
        textResponse = (TextView)findViewById(R.id.response);

        buttonConnect.setOnClickListener(buttonConnectOnClickListener);

        buttonClear.setOnClickListener(new OnClickListener(){

            @Override
            public void onClick(View v) {
                textResponse.setText("");
            }});

    }

    OnClickListener buttonConnectOnClickListener =
            new OnClickListener(){

                @Override
                public void onClick(View arg0) {
                    if(!editTextAddress.getText().toString().isEmpty()&& !editTextPort.getText().toString().isEmpty()) {
                        ConnectionEstablishedTask connectionEstablishedTask = new ConnectionEstablishedTask(
                                editTextAddress.getText().toString(),
                                Integer.parseInt(editTextPort.getText().toString()));
                        connectionEstablishedTask.execute();
                    }
                }};





    public class ConnectionEstablishedTask extends AsyncTask<Void, Void, Void> {

        String dstAddress;
        int dstPort;
        String response = "";

        ConnectionEstablishedTask(String addr, int port){
            dstAddress = addr;
            dstPort = port;


        }

        @Override
        protected Void doInBackground(Void... arg0) {
            boolean received=false;
            DatagramSocket socket = null;
            try {
                socket = new DatagramSocket();
                String sof="Start of Frame";
                byte[] sendData=sof.getBytes("UTF-8");
                InetAddress ipAddress=InetAddress.getByName(dstAddress);
                DatagramPacket startOfFrame=new DatagramPacket(sendData,sendData.length,ipAddress,dstPort);
                socket.send(startOfFrame);
                // wait for answer from server
                // while forse non necessario, metodo receive si occupa di porsi in attesa di un pacchetto
                while(!received) {
                    DatagramPacket receivePacket = new DatagramPacket(sendData, sendData.length);
                    socket.receive(receivePacket);
                    String acknowledgement = new String(receivePacket.getData(), 0, receivePacket.getLength(), "UTF-8");
                    //acknowledgement="SOF received"; // da togliere, server dava problemi
                    
                    if (acknowledgement.equals("SOF received")) {
                        response = "Connection Established";
                        received=true;
                    }
                }
            } catch (UnknownHostException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                response = "UnknownHostException: " + e.toString();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                response = "IOException: " + e.toString();
            }finally{
                if(socket != null){
                    socket.close();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            textResponse.setText(response);
            super.onPostExecute(result);
            if(response.equals("Connection Established")) {
                Intent intent = new Intent(MainActivity.this, SendActivity.class);
                intent.putExtra("dstAddress", editTextAddress.getText().toString());
                intent.putExtra("dstPort", Integer.parseInt(editTextPort.getText().toString()));
                startActivity(intent);
            }

        }

    }


    /* A native method that is implemented by the
     * 'hello-jni' native library, which is packaged
     * with this application.
     *//*
    public native String  stringFromJNI();

    *//* This is another native method declaration that is *not*
     * implemented by 'hello-jni'. This is simply to show that
     * you can declare as many native methods in your Java code
     * as you want, their implementation is searched in the
     * currently loaded native libraries only the first time
     * you call them.
     *
     * Trying to call this function will result in a
     * java.lang.UnsatisfiedLinkError exception !
     *//*
    public native String  unimplementedStringFromJNI();

    *//* this is used to load the 'hello-jni' library on application
     * startup. The library has already been unpacked into
     * /data/data/com.example.hellojni/lib/libhello-jni.so at
     * installation time by the package manager.
     *//*
    static {
        System.loadLibrary("hello-jni");
    }*/
}
