package ro.pub.cs.systems.eim.practicaltest02.network;

import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import cz.msebera.android.httpclient.HttpEntity;
import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.client.methods.HttpGet;
import cz.msebera.android.httpclient.impl.client.DefaultHttpClient;
import cz.msebera.android.httpclient.util.EntityUtils;
import ro.pub.cs.systems.eim.practicaltest02.general.Constants;
import ro.pub.cs.systems.eim.practicaltest02.general.Utilities;
import ro.pub.cs.systems.eim.practicaltest02.model.Data;

public class CommunicationThread  extends Thread {

    private ServerThread serverThread;
    private Socket socket;

    public CommunicationThread(ServerThread serverThread, Socket socket) {
        this.serverThread = serverThread;
        this.socket = socket;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void run() {
        if (socket == null) {
            Log.e(Constants.TAG, "[COMMUNICATION THREAD] Socket is null!");
            return;
        }
        try {
            BufferedReader bufferedReader = Utilities.getReader(socket);
            PrintWriter printWriter = Utilities.getWriter(socket);
            if (bufferedReader == null || printWriter == null) {
                Log.e(Constants.TAG, "[COMMUNICATION THREAD] Buffered Reader / Print Writer are null!");
                return;
            }

            Log.i(Constants.TAG, "[COMMUNICATION THREAD] Waiting for parameters from client");
            String valuta = bufferedReader.readLine();
            if (valuta == null || valuta.isEmpty()) {
                Log.e(Constants.TAG, "[COMMUNICATION THREAD] Error receiving parameters from client");
                return;
            }

            HttpClient httpClient = new DefaultHttpClient();
            String pageSourceCode = "";

            long diff = 0;

            if(serverThread.getCache() != null) {
                Date currentDate = new Date();
                Date lastUpdate = serverThread.getCache().getLastUpdate();

                long diffInMillies = Math.abs(lastUpdate.getTime() - currentDate.getTime());
                diff = TimeUnit.SECONDS.convert(diffInMillies, TimeUnit.SECONDS);
            }


            Log.i(Constants.TAG, "[COMMUNICATION THREAD] Last update was " + diff + " seconds ago");

            if(serverThread.getCache() == null || diff > 60) {

                Log.i(Constants.TAG, "[COMMUNICATION THREAD] Getting the information from the webservice...");
                HttpGet httpGet = new HttpGet(Constants.WEB_SERVICE_ADDRESS + "eur" + Constants.WEB_SERVICE_MODE);
                HttpResponse httpGetResponse = httpClient.execute(httpGet);
                HttpEntity httpGetEntity = httpGetResponse.getEntity();

                if (httpGetEntity != null) {
                    pageSourceCode = EntityUtils.toString(httpGetEntity);
                }

                serverThread.setCache(null);
            } else {
                String result = null;
                switch(valuta) {
                    case Constants.EUR:
                        result = serverThread.getCache().getEUR();
                        break;
                    case Constants.USD:
                        result = serverThread.getCache().getUSD();
                        break;
                    default:
                        result = "[COMMUNICATION THREAD] Wrong information type (USD / EUR)!";
                }

                printWriter.println(result);
                printWriter.flush();
            }

            if(serverThread.getCache() == null) {
                if (pageSourceCode == null) {
                    Log.e(Constants.TAG, "[COMMUNICATION THREAD] Error getting the information from the webservice!");
                    return;
                } else {
                    Log.i(Constants.TAG, pageSourceCode);

                    /* info from server */
                    JSONObject content = new JSONObject(pageSourceCode);
                    Date updateFromWeb = new Date();

                    JSONObject time = content.getJSONObject("time");
                    String lastUpdateString = time.getString("updated");

                    DateFormat format = new SimpleDateFormat("MMMM d, yyyy", Locale.ENGLISH);
                    updateFromWeb = format.parse(lastUpdateString);

                    JSONObject bpi = content.getJSONObject("bpi");
                    JSONObject EURO = bpi.getJSONObject("EUR");
                    String EURO_Value = EURO.getString("rate");

                    JSONObject USD = bpi.getJSONObject("USD");
                    String USD_Value = USD.getString("rate");

                    if (serverThread.getCache() == null) {
                        serverThread.setCache(new Data(EURO_Value, USD_Value, updateFromWeb));
                    }

                    if (serverThread.getCache() == null) {
                        Log.e(Constants.TAG, "[COMMUNICATION THREAD] Weather Forecast Information is null!");
                        return;
                    }

                    String result = null;
                    Log.i(Constants.TAG, "[COMMUNICATION THREAD] " + valuta + " " + serverThread.getCache().toString());
                    switch (valuta) {
                        case Constants.EUR:
                            result = serverThread.getCache().getEUR();
                            break;
                        case Constants.USD:
                            result = serverThread.getCache().getUSD();
                            break;
                        default:
                            result = "[COMMUNICATION THREAD] Wrong information type (USD / EUR)!";
                    }

                    printWriter.println(result);
                    printWriter.flush();
                }
            }
        } catch (IOException ioException) {
            Log.e(Constants.TAG, "[COMMUNICATION THREAD] An exception has occurred: " + ioException.getMessage());
            if (Constants.DEBUG) {
                ioException.printStackTrace();
            }
        } catch (JSONException jsonException) {
            Log.e(Constants.TAG, "[COMMUNICATION THREAD] An exception has occurred: " + jsonException.getMessage());
            if (Constants.DEBUG) {
                jsonException.printStackTrace();
            }
        } catch (ParseException e) {
            e.printStackTrace();
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException ioException) {
                    Log.e(Constants.TAG, "[COMMUNICATION THREAD] An exception has occurred: " + ioException.getMessage());
                    if (Constants.DEBUG) {
                        ioException.printStackTrace();
                    }
                }
            }
        }
    }
}
