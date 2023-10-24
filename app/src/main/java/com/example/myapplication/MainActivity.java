package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    private SensorManager sensorManager;
    private static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 1;
    String activity="ideal";
    public static int stop_all=0;
    private Sensor gyroscope;
    private Sensor accelorometer;
    static FileWriter csvWriter;
    static int iterNum=0;
    static int windownumber=0,time=0;
    LinearLayout ly1,ly2;
    Handler handler;
    Runnable runnable,runnable1;

    File csvFile;
    TextView gx,gy,gz,ax,ay,az,timer, iter, act;
    Button start, end, reset;
    double[] prevacc=new double[]{0,0,0};
    double[] prevgy=new double[]{0,0,0};


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ly1=findViewById(R.id.gm);
        ly2=findViewById(R.id.am);
        gx=findViewById(R.id.gx);
        gy=findViewById(R.id.gy);
        gz=findViewById(R.id.gz);
        ax=findViewById(R.id.ax);
        ay=findViewById(R.id.ay);
        az=findViewById(R.id.az);
        act=findViewById(R.id.activity);
        timer=findViewById(R.id.timeeer);
        iter=findViewById(R.id.Iter);
        start = findViewById(R.id.start);
        reset = findViewById(R.id.reset);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
        }

        createCSVFile();

        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.e("error","clicked");
                try {
                    csvFile.setWritable(true);
                    csvFile.setExecutable(true);
                    csvFile.setReadable(true);
                    FileWriter fileWriter=new FileWriter(csvFile,false);
                    fileWriter.append("iteration number,window number,acc_x vals,acc_y_vals,acc_z_vals,gyro_x vals,gyro_y_vals,gyro_z_vals,activity_name\n");
                    iterNum=0;
                    windownumber=0;
                    time=0;
                    prevacc=new double[]{0,0,0};
                    prevgy=new double[]{0,0,0};

                    fileWriter.close();

                   Toast.makeText(getApplicationContext(),"done",Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    Toast.makeText(getApplicationContext(),"error"+e.getMessage(),Toast.LENGTH_LONG).show();

                    Log.e("error",e.getMessage());
                }
            }
        });
        end = findViewById(R.id.stop);
        sensorManager = (SensorManager) getSystemService(this.SENSOR_SERVICE);
        gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        accelorometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                register();
                csvFile = new File(Environment.getExternalStorageDirectory().toString()+ "/Download/dataset.csv");


                iterNum=0;
                handler = new Handler();

                iterNum++;
                iter.setText("Iteration: " + iterNum);


                runnable = new Runnable() {
                    @Override
                    public void run() {
                        windownumber++;
                        if (windownumber == 9) windownumber = 1;
                        handler.postDelayed(this, 2000);
                    }
                };
                runnable1 = new Runnable() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void run() {
                        time++;
                        timer.setText("Time : "+time);
                        Log.e("time",""+time);

                        if(time<=5){
                            activity="Idle";
                        }else if(time<=20){
                            activity="Sit";
                        }else if(time<=25){
                            activity="Idle";
                        }else if(time<=40){
                            activity="Walk";
                        }else if(time<=45){
                            activity="Idle";
                        }else if(time<=60){
                            activity="Climb";
                            act.setText("Activity: "+activity);
                        }
                        act.setText("Activity: "+activity);

                        if(time==60){
                            Log.e("error","inside this 10 block");
                            try {
                                if(csvWriter==null)Log.e("error","csvwriter is null");
                                csvWriter.flush();
                                time=0;
                                iterNum++;
                                iter.setText("Iteration : "+iterNum);
                                if(iterNum==6 || stop_all==1){
                                    unregister();
                                    stop_all=0;
                                    time=0;
                                    iterNum=0;
                                    prevacc=new double[]{0,0,0};
                                    prevgy=new double[]{0,0,0};
                                    handler.removeCallbacks(runnable);
                                    handler.removeCallbacks(runnable1);
                                    return;
                                }

                                Toast.makeText(getApplicationContext(),"Stopped",Toast.LENGTH_LONG).show();
                            }catch (Exception e){
                            }
                        }
                        handler.postDelayed(this, 1000);
                    }
                };
                handler.postDelayed(runnable, 0);
                handler.postDelayed(runnable1, 0);
            }
        });

        end.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    stop_all=1;
                    csvWriter.flush();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                unregister();
                time=0;
                prevacc=new double[]{0,0,0};
                prevgy=new double[]{0,0,0};
                handler.removeCallbacks(runnable);
                handler.removeCallbacks(runnable1);
            }
        });
    }

    public void createCSVFile() {
        csvFile = new File(Environment.getExternalStorageDirectory().toString()+ "/Download");
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
        }
        try {
                csvFile = new File(Environment.getExternalStorageDirectory().toString()+ "/Download/dataset.csv");
                csvFile.createNewFile();
                csvWriter = new FileWriter(csvFile,true);
                csvWriter.append("iteration number,window number,acc_x vals,acc_y_vals,acc_z_vals,gyro_x vals,gyro_y_vals,gyro_z_vals,activity_name\n");
            } catch (Exception e) {
            Log.e("message", "Testing" + e.toString());
        }
    }

    public void register() {
        sensorManager.registerListener(this, gyroscope, sensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, accelorometer, sensorManager.SENSOR_DELAY_NORMAL);
    }
    public void unregister() {
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (sensorEvent.sensor == gyroscope) {
           try {
               gx.setText("Gyro-x : "+sensorEvent.values[0]);
               gy.setText("Gyro-y : "+sensorEvent.values[1]);
               gz.setText("Gyro-z : "+sensorEvent.values[2]);
               prevgy=new double[]{sensorEvent.values[0],sensorEvent.values[1],sensorEvent.values[2]};
               csvWriter.append(iterNum + "," + windownumber + "," + prevacc[0]+","+ prevacc[1]+","+ prevacc[2]+"," + sensorEvent.values[0] + "," + sensorEvent.values[1] + "," + sensorEvent.values[2] + ","+activity+" \n");

           }catch (Exception e){

           }
           Log.d("Message", "gyro" + sensorEvent.values[sensorEvent.values.length - 1]);

        } else if (sensorEvent.sensor == accelorometer) {
            ContentValues values = new ContentValues();
            try {
                ax.setText("Acc-x : "+sensorEvent.values[0]);
                ay.setText("Acc-y : "+sensorEvent.values[1]);
                az.setText("Acc-z : "+sensorEvent.values[2]);
                prevacc=new double[]{sensorEvent.values[0],sensorEvent.values[1],sensorEvent.values[2]};
                csvWriter.append(iterNum + "," + windownumber + "," + sensorEvent.values[0] + "," + sensorEvent.values[1] + "," + sensorEvent.values[2] + ","+prevgy[0]+","+prevgy[1]+","+prevgy[2]+","+activity+"\n");

            }catch (Exception e){
            }
            Log.d("Message", "acclerometer" + sensorEvent.values[sensorEvent.values.length - 1]);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

}



