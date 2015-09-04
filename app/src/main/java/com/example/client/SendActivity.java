package com.example.client;

import android.content.Intent;
import android.os.AsyncTask;
import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;


public class SendActivity extends Activity {
    String dstAddress;
    int dstPort;
    Button sendButton;
    Button clearButton;
    EditText overtMessage;
    EditText covertMessage;
    TextView response;

    static {
        System.loadLibrary("hello-jni");
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send);
        Intent intent=getIntent();
        dstAddress=intent.getStringExtra("dstAddress");
        dstPort=intent.getIntExtra("dstPort", 0);
        sendButton=(Button)findViewById(R.id.send);
        clearButton=(Button)findViewById(R.id.clear);
        overtMessage=(EditText)findViewById(R.id.overt);
        covertMessage=(EditText)findViewById(R.id.covert);
        response=(TextView)findViewById(R.id.response);

        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                response.setText("");
            }
        });

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              /*  SendTask sendTask = new SendTask(dstAddress, dstPort, overtMessage.getText().toString(), covertMessage.getText().toString());
                sendTask.execute();*/
                sendMessage();

            }
        });


    }

    public void sendMessage() {
        this.sendFromJNI(this.dstAddress, this.dstPort,overtMessage.getText().toString(),covertMessage.getText().toString());
        this.response.setText("Message Sent");

    }


   /* public class SendTask extends AsyncTask<Void, Void, Void> {
        private String address;
        private int port;
        private String overt;
        private String covert;
        String res="";


        public SendTask(String dstAddress,int dstPort,String overt,String covertMessage) {
            this.address=dstAddress;
            this.port=dstPort;
            this.overt=overt;
            this.covert=covertMessage;

        }

        @Override
        protected Void doInBackground(Void... arg0) {
            DatagramSocket socket;
            byte[] sendData;
            int j=0;
            long timing_interval=900;
            try {
                socket = new DatagramSocket();
                InetAddress ipAddress = InetAddress.getByName(this.address);
                int[] encodedOvert = encode(this.overt);
                int[] encodedCovert=encode(this.covert);

                //invio lunghezza del messaggio al server
                ByteArrayOutputStream b1=new ByteArrayOutputStream();
                DataOutputStream d1=new DataOutputStream(b1);
                d1.writeInt(encodedOvert.length);
                d1.writeLong(timing_interval);
                d1.close();
                sendData=b1.toByteArray();
                DatagramPacket sendPacketLength = new DatagramPacket(sendData, sendData.length, ipAddress, port);
                socket.send(sendPacketLength);

                for (int i = 0; i < encodedOvert.length; i++) {
                    //sendFromJNI(this.address,this.port,encodedCovert[0]);
                    ByteArrayOutputStream baos=new ByteArrayOutputStream();
                    DataOutputStream daos=new DataOutputStream(baos);
                    daos.writeInt(encodedOvert[i]);
                    daos.close();
                    sendData=baos.toByteArray();
                    DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, ipAddress, port);

                    if (j<encodedCovert.length) {
                        if (encodedCovert[j] == 0) {
                            TimeUnit.MILLISECONDS.sleep(timing_interval);
                            i--;
                        } else if (encodedCovert[j] == 1) {
                            TimeUnit.MILLISECONDS.sleep(timing_interval/2);
                            socket.send(sendPacket);
                            TimeUnit.MILLISECONDS.sleep(timing_interval/2);

                        }
                        j++;
                    }
                    else
                        socket.send(sendPacket);
                }
            }
            catch (UnknownHostException e) {
                res+= e.toString();
            } catch (IOException e) {
                res+= e.toString();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            res = "Message sent";
            return null;
        }


        @Override
        protected void onPostExecute(Void result) {
            response.setText(res);
        }

        // codifica un carattere in binario
        public int[] encodeChar (char c) {
            int[] output=new int[8];
            int i;
            for (i=0;i<8;i++) {
                output[7-i]=((c>>i)& 1);
            }
            return output;

        }


        public int[] encode(String message) {
            int[] encoded = new int[message.length()*8];
            int i,j;
            int pos=0;
            int[] single_char;
            for (i=0;i<message.length();i++) {
                single_char=encodeChar(message.charAt(i));
                for(j=0;j<8;j++)
                    encoded[j+pos]=single_char[j];
                pos= pos+8; // per ricordare la posizione corrente nell'array finale
            }
            return encoded;

        }
        public native int sendFromJNI(String address, int port, int data);


    }*/

    public native int sendFromJNI(String address, int port, String overt,String covert);


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_send, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }



}

