package com.lp.brandon.dbmap;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by brand on 3/11/2016.
 */
public class OkPostRequest {
    private final OkHttpClient client;
    private DbController dbController;
    private Context context;

    public OkPostRequest(Context context) {
        client = new OkHttpClient();
        this.context = context;
        dbController = new DbController(context);
    }



    private void send(){
        List<MarkerdBEntity> list = dbController.getAll();
        RequestBody body;
        Request request;
        Response response;
        for (MarkerdBEntity m:list) {
            body = new FormBody.Builder()
                        .add("user","nleal")
                        .add("time",new SimpleDateFormat("yyyyMMddhhmmss").format(new Date()).toString())
                        .add("lat",m.getLatitude())
                        .add("lng",m.getLongitude()).build();
            request = new Request.Builder()
                            .url("http://190.144.171.172/tracker/store.php")
                            .post(body)
                            .build();
            try {
                response = client.newCall(request).execute();
                Log.v("Brandon-lp","Response -> "+response.body());
            } catch (IOException e) {
                Log.v("Brandon-lp","Exploto en el call");
                e.printStackTrace();
            }
        }
    }

    public void execute(){
        class Send extends AsyncTask<Void, Void, String>{

            private ProgressDialog progressDialog;
            @Override
            protected void onPreExecute() {
                progressDialog = new ProgressDialog(context);
                progressDialog.setTitle("Por favor Espere");
                progressDialog.setMessage("...Sincronizando...");
                progressDialog.show();
                super.onPreExecute();
            }

            @Override
            protected String doInBackground(Void... voids) {
                send();
                return "Sincronizacion completa";
            }

            @Override
            protected void onCancelled() {
                Toast.makeText(context,"Error, sincronizacion Cancelada",Toast.LENGTH_SHORT).show();
                progressDialog.cancel();
                super.onCancelled();
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                progressDialog.cancel();
                Toast.makeText(context,s,Toast.LENGTH_SHORT).show();
            }
        }
        new Send().execute();
    }

}
