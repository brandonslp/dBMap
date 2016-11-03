package com.lp.brandon.dbmap;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener{

    private MediaRecorder mediaRecorder;
    private Button btnrecorder;
    private boolean listening;
    private ProgressBar pb;
    private TextView txtdB;
    private DbController dbController;
    Ear ear;
    private GetPreferences preferences;
    private GoogleApiClient googleApiClient;
    private Location mylocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnrecorder = (Button) findViewById(R.id.btnrecord);
        pb = (ProgressBar) findViewById(R.id.pg);
        txtdB = (TextView) findViewById(R.id.txtdB);
        preferences = new GetPreferences(this);
        dbController = new DbController(this);
        //Log.v("Brandon-lp","time -> "+new SimpleDateFormat("yyyyMMddhhmmss").format(new Date()).toString());
        btnrecorder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startRecord();
            }
        });

        googleApiClient = new GoogleApiClient.Builder(this)
                                .addApi(LocationServices.API)
                                .addConnectionCallbacks(this)
                                .addOnConnectionFailedListener(this).build();
    }



    public void startRecord() {
        if (ear == null || !listening) {
            listening = true;
            ear = new Ear();
            ear.execute();
            btnrecorder.setText("Recording...");
        } else {
                    /*ear.cancel(true);
                    listening = false;
                    stop();*/
            listening = false;
            btnrecorder.setText("Record");
            //stop();
            ear = null;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        googleApiClient.connect();
    }

    @Override
    protected void onStop() {
        if (googleApiClient!=null){
            googleApiClient.disconnect();
        }
        super.onStop();
    }

    @Override
    protected void onPause() {
        listening = false;
        stop();
        super.onPause();
    }

    public void start() {
        if (mediaRecorder == null) {
            mediaRecorder = new MediaRecorder();
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

            mediaRecorder.setOutputFile("/dev/null");

            try {
                mediaRecorder.prepare();
            } catch (IOException e) {
                e.printStackTrace();
                Log.v("Brandon-lp", "Error al preparar mic " + e.getMessage());
            }
            mediaRecorder.start();
        }
    }

    public void stop() {
        if (mediaRecorder != null) {
            mediaRecorder.stop();
            mediaRecorder.reset();
            mediaRecorder.release();
            mediaRecorder = null;
        }
        btnrecorder.setText("Record");
    }

    public Location getLocation() {
        if(mylocation!=null)
            return mylocation;
        else{
            Log.v("Brandon-lp","Tambien explota con google api");
            return null;
        }
    }

    @Override
    public boolean onCreatePanelMenu(int featureId, Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return super.onCreatePanelMenu(featureId, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(!listening){
            if (item.getItemId()==R.id.menu_settings){
                Intent i = new Intent().setClass(MainActivity.this, Settings.class);
                startActivity(i);
            }
            if (item.getItemId()==R.id.menu_loadmap){
                Intent i = new Intent().setClass(MainActivity.this, MapsActivity.class);
                startActivity(i);
            }
            if (item.getItemId()==R.id.menu_sync){
                //new Sync(this).sync_news();
                new OkPostRequest(this).execute();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    public double getAmplitude() {
        if (mediaRecorder != null){
            double maxamp = mediaRecorder.getMaxAmplitude();
            Log.v("Brandon-lp","amplitud max"+maxamp);
            return maxamp;}
        else
            return 0.0;
    }

    public double sounddB(){
        return (20 * Math.log10(getAmplitude() / Double.parseDouble(preferences.getAmplitudeReference())));
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            Log.v("Brandon-lp","No hay permisos");
            return;
        }
        mylocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    public class Ear extends AsyncTask<Void, Double, Void>{
        @Override
        protected void onCancelled() {
            listening=false;
        }

        @Override
        protected void onPreExecute() {
            //super.onPreExecute();
            start();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            do{
                SystemClock.sleep(Integer.parseInt(preferences.gettTmelap()));
                double amp=sounddB();
                Log.v("Brandon-lp","dB por fuera en el log-> "+amp);
                publishProgress(amp);
            }while(listening);
            return null;
        }

        @Override
        protected void onProgressUpdate(Double... values) {
            super.onProgressUpdate(values);
            Log.v("Brandon-lp","dB -> "+ values[0]);
            //Log.v("Brandon-lp","formateando db  ->"+new Formatter().format("%03.1f",values[0]).toString());
            pb.setProgress((values[0].intValue()));
            txtdB.setText(String.valueOf(values[0]));
            if(Double.parseDouble(preferences.getdB())<values[0]){
                Location l=getLocation();
                Log.v("Brandon-lp","guardo ->"+dbController.insert(values[0],String.valueOf(l.getLatitude()),String.valueOf(l.getLongitude())));
                this.onPostExecute(null);
                this.cancel(true);
                listening =false;
            }
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            stop();
        }
    }

}
