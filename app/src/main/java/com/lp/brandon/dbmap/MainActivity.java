package com.lp.brandon.dbmap;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.os.SystemClock;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.IOException;
import java.util.Formatter;

public class MainActivity extends AppCompatActivity {

    private MediaRecorder mediaRecorder;
    private Button btnrecorder;
    private boolean listening;
    private ProgressBar pb;
    private TextView txtdB;
    private DbController dbController;
    int time = 5000;
    Ear ear;
    private GetPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnrecorder = (Button) findViewById(R.id.btnrecord);
        pb = (ProgressBar) findViewById(R.id.pg);
        txtdB = (TextView) findViewById(R.id.txtdB);
        preferences = new GetPreferences(this);
        dbController = new DbController(this);
        //time=Integer.parseInt(preferences.getStartTime());//en caso de que se utilice el contador para la captura
        btnrecorder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
        });
    }


    //utilizando el temporizador se genera algun problema al detener el asyntask
    public void startRecord(final long finish, long tick) {
        CountDownTimer t = new CountDownTimer(finish, tick) {
            @Override
            public void onTick(long l) {
                listening = true;
                ear = new Ear();
                ear.execute();
                btnrecorder.setText("Recording...");
            }

            @Override
            public void onFinish() {
                ear.cancel(true);
                stop();
                ear = null;

            }
        }.start();
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
        LocationManager locationManager = (LocationManager) getSystemService(this.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        String provider = locationManager.getBestProvider(criteria,false);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return null;
        }
        try{
            Log.v("Brandon-lp","Location -> "+locationManager.getLastKnownLocation(provider));
            return locationManager.getLastKnownLocation(provider);
        }catch (NullPointerException e){
            Log.v("Brandon-lp","Location -> "+locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER));
            return locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        }
    }

    @Override
    public boolean onCreatePanelMenu(int featureId, Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return super.onCreatePanelMenu(featureId, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId()==R.id.menu_settings){
            Intent i = new Intent().setClass(MainActivity.this, Settings.class);
            startActivity(i);
        }
        if (item.getItemId()==R.id.menu_loadmap){
            Intent i = new Intent().setClass(MainActivity.this, MapsActivity.class);
            startActivity(i);
        }
        if (item.getItemId()==R.id.menu_sync){
            new Sync(this).sync_news();
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
