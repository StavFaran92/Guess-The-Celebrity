package com.example.guessthecelebrity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    Random random = new Random();
    ArrayList<String> celebURLs = new ArrayList<String>();
    ArrayList<String> celebNames = new ArrayList<String>();
    int chosenCeleb = 0;
    int chosenButton = 0;
    ImageView celebImage;
    Button[] buttons = new Button[4];

    public class ImageDownloader extends AsyncTask<String, Void, Bitmap>{

        @Override
        protected Bitmap doInBackground(String... urls) {
            try {
                URL url = new URL(urls[0]);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.connect();
                InputStream inputStream = conn.getInputStream();
                Bitmap image = BitmapFactory.decodeStream(inputStream);
                return image;

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        };
    }

    public class DownloadTask extends AsyncTask<String, Void, String>{

        @Override
        protected String doInBackground(String... urls) {

            String result = "";
            URL url;
            HttpURLConnection urlConnection = null;

            try {
                url = new URL(urls[0]);
                urlConnection = (HttpURLConnection) url.openConnection();

                urlConnection.setInstanceFollowRedirects(true);
                HttpURLConnection.setFollowRedirects(true);

                InputStream inputStream = urlConnection.getInputStream();
                InputStreamReader reader = new InputStreamReader(inputStream);
                int data = reader.read();

                while( data != -1){
                    char current = (char)data;
                    result += current;
                    data = reader.read();
                }
                return result;

            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }
    }

    public void celebChosen(View view){
        int tag = Integer.parseInt(view.getTag().toString());
        Toast msg;
        if (tag == chosenButton)
            msg = Toast.makeText(this, "Correct", Toast.LENGTH_LONG);
        else
            msg = Toast.makeText(this, "Wrong", Toast.LENGTH_LONG);

        msg.show();

        generateCeleb();
    }

    public void generateCeleb(){
        chosenCeleb = random.nextInt(celebNames.size());
        ImageDownloader imageDownloader = new ImageDownloader();
        try {
            Bitmap image = imageDownloader.execute(celebURLs.get(chosenCeleb)).get();
            celebImage.setImageBitmap(image);
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        chosenButton = random.nextInt(4);
        for(int i=0; i<4; ++i){
            if (i == chosenButton)
                buttons[i].setText(celebNames.get(chosenCeleb));
            else{
                int r = random.nextInt(celebNames.size());
                while( r == chosenCeleb )
                    r = random.nextInt(celebNames.size());
                buttons[i].setText(celebNames.get(r));
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        DownloadTask task = new DownloadTask();
        String result = null;

        try {
            result = task.execute("http://www.posh24.se/kandisar").get();
            String[] splitResult = result.split("<div class=\"sidebarContainer\">");

            Pattern pattern = Pattern.compile("<img src=\"(.*?)\"");
            Matcher matcher = pattern.matcher(splitResult[0]);

            while( matcher.find() ){
                celebURLs.add(matcher.group(1));
            }

            pattern = Pattern.compile("alt=\"(.*?)\"");
            matcher = pattern.matcher(splitResult[0]);

            while( matcher.find() ){
                celebNames.add(matcher.group(1));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        celebImage = findViewById(R.id.imageView);
        buttons[0] = findViewById(R.id.button1);
        buttons[1] = findViewById(R.id.button2);
        buttons[2] = findViewById(R.id.button3);
        buttons[3] = findViewById(R.id.button4);

        generateCeleb();
    }
}
