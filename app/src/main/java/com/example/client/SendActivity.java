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
    EditText timingInterval;

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
        timingInterval=(EditText)findViewById(R.id.timing);

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
        this.sendFromJNI(this.dstAddress, this.dstPort,overtMessage.getText().toString(),covertMessage.getText().toString(),Integer.parseInt(timingInterval.getText().toString()));
        this.response.setText("Message Sent");

    }


    public native int sendFromJNI(String address, int port, String overt,String covert,int intervald);


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

