package com.example.lab6;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.ImageView;
import android.widget.ProgressBar;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity {

    private ImageView img;
    private ProgressBar bar;
    private CatImages catImages;
    private HttpURLConnection connection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        img = findViewById(R.id.imageView);
        bar = findViewById(R.id.progressBar);

        catImages = new CatImages();
        catImages.execute("https://cataas.com/cat?json=true");
    }

    private class CatImages extends AsyncTask<String, Integer, Bitmap>{
        Bitmap catImage;
        HttpsURLConnection Connection;
        BufferedReader reader;
        private String pictureId;
        private String pictureUrl;

        ArrayList<String> allIds = new ArrayList<>();
        @Override
        protected Bitmap doInBackground(String... strings) {

            ArrayList<String> allIds = new ArrayList<>();
            while(true){
                try{
                    URL url = new URL(strings[0]);
                    connection = (HttpsURLConnection) url.openConnection();
                    connection.setDoInput(true);
                    connection.connect();

                    InputStream stream = connection.getInputStream();

                    reader = new BufferedReader(new InputStreamReader(stream));

                    StringBuffer buffer = new StringBuffer();

                    String line ="";
                    while ((line = reader.readLine()) != null){
                        buffer.append(line);
                    }

                    String finalJson = buffer.toString();
                    JSONObject allJsonData = new JSONObject(finalJson);

                    pictureId = allJsonData.getString("_id");
                    pictureUrl = allJsonData.getString("url");
                    URL catURL = new URL("https://cataas.com"+pictureUrl);

                    File catFile = new File(getApplicationContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES), pictureId);
                    if (allIds.contains(pictureId)) {

                        catImage = BitmapFactory.decodeFile(catFile.getPath());
                    } else {

                        catImage = BitmapFactory.decodeStream(catURL.openConnection().getInputStream());
                        FileOutputStream outStream = new FileOutputStream(catFile);
                        catImage.compress(Bitmap.CompressFormat.JPEG, 100, outStream);
                        allIds.add(pictureId);
                    }

                    for (int i = 0; i < 100; i++) {
                        try {
                            publishProgress(i);
                            Thread.sleep(30);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                }catch (MalformedURLException e){
                    e.printStackTrace();
                    Log.d("URL broke","URL broke here");
                }catch (IOException e){
                    e.printStackTrace();
                    Log.d("IO broke","IO broke here");
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                } finally {

                    if (connection != null) {
                        connection.disconnect();
                    }
                    try {
                        if (reader != null) {
                            reader.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            //return catImage;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            int progress = values[0];
            bar.setProgress(progress);
            onPostExecute(catImage);
        }

        @Override
        protected void onPostExecute(Bitmap catImage) {
            img.setImageBitmap(catImage);

        }
    }
}