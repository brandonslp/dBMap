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





    public void execute(){
        class Send extends AsyncTask<Void, Void, String>{

            private boolean send(){

                List<MarkerdBEntity> list = dbController.getAllNoSend();
                Request request;
                Response response;
                try {
                    for (MarkerdBEntity m:list) {
                        String url = String.format("http://190.144.171.172/tracker/store.php?user=%s&time=%s&lat=%s&lng=%s"
                                ,"nleal",new SimpleDateFormat("yyyyMMddhhmmss").format(new Date()).toString(),m.getLatitude(),m.getLongitude());
                        Log.v("Brandon-lp","La url quedo -> "+url);
                        request = new Request.Builder()
                                .url(url)
                                .get()
                                .build();

                        response = client.newCall(request).execute();
                        Log.v("Brandon-lp","Response -> "+response.body().string());
                    }
                    return true;
                }catch (IOException e) {
                    Log.v("Brandon-lp","Exploto en el call");
                    e.printStackTrace();
                    return false;
                }catch (NullPointerException e){
                    Log.v("Brandon-lp","no hay registros");
                    return false;
                    //Toast.makeText(context,"No hay registros nuevos",Toast.LENGTH_SHORT).show();
                }

            }

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
                if (send())
                return "Sincronizacion completa";
                else return "No hay registros nuevos";
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
                dbController.changeStatus();
                progressDialog.cancel();
                if (!s.isEmpty() && s!=null)
                Toast.makeText(context,s,Toast.LENGTH_SHORT).show();
            }
        }
        new Send().execute();
    }

}
