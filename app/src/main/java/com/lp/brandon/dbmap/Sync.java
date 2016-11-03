package com.lp.brandon.dbmap;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;


import cz.msebera.android.httpclient.HttpEntity;
import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.NameValuePair;
import cz.msebera.android.httpclient.client.ClientProtocolException;
import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.client.methods.HttpPost;
import cz.msebera.android.httpclient.entity.StringEntity;
import cz.msebera.android.httpclient.impl.client.DefaultHttpClient;
import cz.msebera.android.httpclient.protocol.HTTP;
import cz.msebera.android.httpclient.util.EntityUtils;

import org.json.JSONException;
import org.json.JSONObject;

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

    private DbController controller;
    List<JSONObject> params;
    public Sync(Context context) {
        this.context = context;
        controller = new DbController(context);
    }

    private void cargar(){
        params = new ArrayList<>();
        List<MarkerdBEntity> list = controller.getAll();
        JSONObject data;
        for (MarkerdBEntity m:list) {
            data = new JSONObject();
            try {
                data.put("user", "nleal");
                data.put("time", new SimpleDateFormat("yyyyMMddhhmmss").format(new Date()).toString());
                data.put("lat", m.getLatitude());
                data.put("lng", m.getLongitude());
                params.add(data);
            }catch (JSONException e){
                params=null;
                break;
            }
        }
    }



    protected void sync_news(){
        cargar();
        class Enviar extends AsyncTask<List<JSONObject>,Void,String>{

            public void HttpPost(List<JSONObject> params) {
                HttpPost httpPost = new HttpPost("http://190.144.171.172/tracker/store.php");
                //httpPost.setHeader(headerName, headerValue);
                //if (content != null && type != null)
                httpPost.setHeader("content-type","application/json");
                HttpResponse response = null;
                HttpClient httpClient = new DefaultHttpClient();
                String result = null;
                if (params!=null){
                    for (JSONObject data:params) {
                        try {
                            Log.v("Brandon-lp","data ->"+data.toString());
                            StringEntity entity = new StringEntity(data.toString(), HTTP.UTF_8);
                            httpPost.setEntity(entity);
                            response = httpClient.execute(httpPost);
                            HttpEntity entity1 = response.getEntity();
                            result = EntityUtils.toString(entity1);
                            Log.v("Brandon-lp","Result -> "+result);
                        } catch (UnsupportedEncodingException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        } catch (ClientProtocolException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            // TODO Auto-generated catch block
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
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
            protected String doInBackground(List<JSONObject>... params) {
                HttpPost(params[0]);

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
                Toast.makeText(context,s,Toast.LENGTH_SHORT).show();
            }
        }
        Enviar enviar = new Enviar();
        enviar.execute(params);
    }
}
