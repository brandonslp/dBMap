package com.lp.brandon.dbmap;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * Created by brand on 2/11/2016.
 */
public class Sync {
    private Context context;
    List<List<NameValuePair>> items;
    List<NameValuePair> nameValuePairList;
    private DbController controller;

    public Sync(Context context) {
        this.context = context;
        controller = new DbController(context);
    }

    private void cargar(){
        try{
            List<MarkerdBEntity> entities= controller.getAllNoSend();
            items = new ArrayList<List<NameValuePair>>();
            for (MarkerdBEntity m : entities){
                nameValuePairList = new ArrayList<NameValuePair>();
                nameValuePairList.add(new BasicNameValuePair("user","nleal"));
                nameValuePairList.add(new BasicNameValuePair("time",new SimpleDateFormat("yyyyMMddhhmmss").format(new Date()).toString()));
                Log.v("Brandon-lp","time -> "+new SimpleDateFormat("yyyyMMddhhmmss").format(new Date()).toString());
                nameValuePairList.add(new BasicNameValuePair("lat",m.getLatitude()));
                nameValuePairList.add(new BasicNameValuePair("lng",m.getLongitude()));
                items.add(nameValuePairList);
            }
        }catch (NullPointerException e){
            Toast.makeText(context,"No hay registros para sincronizar",Toast.LENGTH_SHORT).show();
        }
    }

    protected void sync_news(){
        cargar();
        class Enviar extends AsyncTask<List<List<NameValuePair>>,Void,String> {


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
            protected String doInBackground(List<List<NameValuePair>>... params) {

                List<List<NameValuePair>> items = params[0];
                HttpClient httpClient = new DefaultHttpClient();
                HttpPost httpPost = new HttpPost("http://190.144.171.172/tracker/store.php");
                HttpResponse response;
                HttpEntity entity;
                for (List<NameValuePair> x :items) {
                    try {
                        Log.v("Brandon-lp","Enviando -> "+ x.get(0).toString() + "->"+x.get(0).getValue());
                        httpPost.setEntity(new UrlEncodedFormEntity(x));
                        Log.v("Brandon-lp","URI -> "+ httpPost.getURI());
                        response = httpClient.execute(httpPost);
                        entity = response.getEntity();
                        Log.v("Brandon-lp","El resultado fue -> "+ EntityUtils.toString(entity));
                    } catch (UnsupportedEncodingException e) {
                        cancel(true);
                        Log.v("brandon-lp","Error de encoding");
                    } catch (ClientProtocolException e) {
                        cancel(true);
                        Log.v("brandon-lp", "error de client");
                    } catch (IOException e) {
                        cancel(true);
                        Log.v("brandon-lp", "Error de escritura");
                    }

                }
                return "Sincronizacion Completa";
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
                controller.changeStatus();
                Toast.makeText(context,s,Toast.LENGTH_SHORT).show();
            }
        }
        Enviar enviar = new Enviar();
        enviar.execute(items);
    }
}
