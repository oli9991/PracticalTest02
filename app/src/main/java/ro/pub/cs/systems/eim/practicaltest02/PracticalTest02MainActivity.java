package ro.pub.cs.systems.eim.practicaltest02;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import ro.pub.cs.systems.eim.practicaltest02.general.Constants;
import ro.pub.cs.systems.eim.practicaltest02.network.ClientThread;
import ro.pub.cs.systems.eim.practicaltest02.network.ServerThread;

public class PracticalTest02MainActivity extends AppCompatActivity {

    // Server widgets
    private EditText serverPortEditText = null;
    private Button connectButton = null;

    // Client widgets
    private EditText valuta = null;
    private EditText clientPortEditText = null;
    private EditText clientAddressEditText = null;

    private Button getinformation = null;
    private TextView information = null;

    private ServerThread serverThread = null;
    private ClientThread clientThread = null;

    private ConnectButtonClickListener connectButtonClickListener = new ConnectButtonClickListener();
    private class ConnectButtonClickListener implements Button.OnClickListener {

        @Override
        public void onClick(View view) {
            String serverPort = serverPortEditText.getText().toString();
            if (serverPort == null || serverPort.isEmpty()) {
                Toast.makeText(getApplicationContext(), "[MAIN ACTIVITY] Server port should be filled!", Toast.LENGTH_SHORT).show();
                return;
            }

            serverThread = new ServerThread(Integer.parseInt(serverPort));
            if (serverThread.getServerSocket() == null) {
                Log.e(Constants.TAG, "[MAIN ACTIVITY] Could not create server thread!" + " Port provided = " + serverPort);
                return;
            }
            serverThread.start();
        }

    }

    private GetInfoListener GetInfoListener = new GetInfoListener();
    private class GetInfoListener implements Button.OnClickListener {

        @Override
        public void onClick(View view) {
            String clientAddress = clientAddressEditText.getText().toString();
            String valutaString = valuta.getText().toString();
            String clientPort = clientPortEditText.getText().toString();
            if (valutaString == null || valutaString.isEmpty()
                    || clientPort == null || clientPort.isEmpty()) {
                Toast.makeText(getApplicationContext(), "[MAIN ACTIVITY] Client connection parameters should be filled!", Toast.LENGTH_SHORT).show();
                return;
            }
            Log.i(Constants.TAG, "[MAIN ACTIVITY] onClick() callback method has been invoked : " +  valutaString);

            if(!valutaString.equals(Constants.EUR) && !valutaString.equals(Constants.USD)) {
                Toast.makeText(getApplicationContext(), "[MAIN ACTIVITY] The current options are EUR / USD!", Toast.LENGTH_SHORT).show();
                return;
            }
            if (serverThread == null || !serverThread.isAlive()) {
                Toast.makeText(getApplicationContext(), "[MAIN ACTIVITY] There is no server to connect to!", Toast.LENGTH_SHORT).show();
                return;
            }

            information.setText(Constants.EMPTY_STRING);

            clientThread = new ClientThread(
                    clientAddress, Integer.parseInt(clientPort), valutaString, information
            );
            clientThread.start();
        }

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_practical_test02_main);

        Log.i(Constants.TAG, "[MAIN ACTIVITY] onCreate() callback method has been invoked");


        serverPortEditText = (EditText)findViewById(R.id.port);
        clientAddressEditText = (EditText)findViewById(R.id.address);
        valuta = (EditText)findViewById(R.id.valuta);
        connectButton = (Button)findViewById(R.id.launch);
        connectButton.setOnClickListener(connectButtonClickListener);


        clientPortEditText = (EditText)findViewById(R.id.port_client);

        getinformation = (Button)findViewById(R.id.send);
        getinformation.setOnClickListener(GetInfoListener);

        information = (TextView)findViewById(R.id.information);
    }

    @Override
    protected void onDestroy() {
        Log.i(Constants.TAG, "[MAIN ACTIVITY] onDestroy() callback method has been invoked");
        if (serverThread != null) {
            serverThread.stopThread();
        }
        super.onDestroy();
    }
}