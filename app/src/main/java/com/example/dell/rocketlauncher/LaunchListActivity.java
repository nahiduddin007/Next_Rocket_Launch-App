package com.example.dell.rocketlauncher;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import static java.security.AccessController.getContext;

public class LaunchListActivity extends AppCompatActivity {

    private static final String TAG = LaunchListActivity.class.getSimpleName();

    private static String url = "https://launchlibrary.net/1.4/launch/next/5";
    private  ArrayList<HashMap<String, String>> launchList;

    private ProgressDialog pDialog;

    private RocketLaunchAdapter mAdapter;
    private ListView listView;
    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch_list);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        initialization();
    }

    private void initialization() {
        listView = findViewById(R.id.launchSheduleListView);
        launchList = new ArrayList<>();
        imageView = findViewById(R.id.launchImageViewList);

        new GetContacts().execute();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private class GetContacts extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Showing progress dialog
            pDialog = new ProgressDialog(LaunchListActivity.this);
            pDialog.setMessage("Please wait...");
            pDialog.setCancelable(false);
            pDialog.show();

        }

        @Override
        protected Void doInBackground(Void... arg0) {
            HttpHandler sh = new HttpHandler();

            // Making a request to url and getting response
            String jsonStr = sh.makeServiceCall(url);

            Log.e(TAG, "Response from url: " + jsonStr);

            if (jsonStr != null) {
                try {

                    JSONObject jsonObj = new JSONObject(jsonStr);

                    // Getting JSON Array node
                    JSONArray contacts = jsonObj.getJSONArray("launches");

                    // looping through All Contacts
                    for (int i = 0; i < contacts.length(); i++) {
                        JSONObject c = contacts.getJSONObject(i);
                        HashMap<String, String> launch = new HashMap<>();

                        String id = c.getString("id");
                        String name = c.getString("name");
                        String net = c.getString("net");
                        String tbdtime = c.getString("tbdtime");
                        String tbddate = c.getString("tbddate");
                        String windowstart = c.getString("windowstart");
                        String windowend = c.getString("windowend");
                        String changed = c.getString("changed");
                        String launchUrl = "Unknown";
                        if(c.has("vidURLs")){
                            JSONArray urlArray = c.getJSONArray("vidURLs");
                            for (int o=0; o<urlArray.length(); o++){
                                launchUrl = urlArray.getString(0);
                                break;
                            }
                        }

                        String stationLocation = null;
                        String stationMap = null;
                        String countryCode = null;
                        JSONObject locationObj = c.getJSONObject("location");
                        JSONArray padsArray = locationObj.getJSONArray("pads");
                        JSONObject rocketObj = c.getJSONObject("rocket");
                        String rocketImg = rocketObj.getString("imageURL");
                        String location = (String) locationObj.get("name");
                        countryCode = (String) locationObj.get("countryCode");
                        stationMap = padsArray.getJSONObject(0).getString("mapURL");

                        // adding each child node to HashMap key => value
                        launch.put("id", id);
                        launch.put("name", name);
                        launch.put("net", net);
                        launch.put("tbdtime", tbdtime);
                        launch.put("tbddate", tbddate);
                        launch.put("windowstart", windowstart);
                        launch.put("windowend", windowend);
                        launch.put("changed", changed);
                        launch.put("url", launchUrl);
                        launch.put("location", location);
                        launch.put("image", rocketImg);
                        launch.put("stationlocation", stationLocation);
                        launch.put("stationmap", stationMap);
                        launch.put("countrycode", countryCode);

                        launchList.add(launch);
                    }
                } catch (final JSONException e) {
                    Log.e(TAG, "Json parsing error: " + e.getMessage());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(),
                                    "Json parsing error: " + e.getMessage(),
                                    Toast.LENGTH_LONG)
                                    .show();
                        }
                    });

                }
            } else {
                Log.e(TAG, "Couldn't get json from server.");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(),
                                "Couldn't get json from server. Check LogCat for possible errors!",
                                Toast.LENGTH_LONG)
                                .show();
                    }
                });

            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            // Dismiss the progress dialog
            if (pDialog.isShowing())
                pDialog.dismiss();
            /**
             * Updating parsed JSON data into ListView
             * */

            android.widget.ListAdapter adapter = new SimpleAdapter(
                    LaunchListActivity.this, launchList,
                    R.layout.launch_shedule_layout, new String[]{"name", "net", "location"}
                    , new int[]{R.id.launchTitleTextView, R.id.launchTimeLocationTextView, R.id.launchLocationTextView});

            listView.setAdapter(adapter);
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    String id = launchList.get(i).get("id");
                    String name = launchList.get(i).get("name");
                    String net = launchList.get(i).get("net");
                    String tbdTime = launchList.get(i).get("tbdtime");
                    String tbdDate = launchList.get(i).get("tbddate");
                    String windowStart = launchList.get(i).get("windowstart");
                    String windowEnd = launchList.get(i).get("windowend");
                    String changed = launchList.get(i).get("changed");
                    String url = launchList.get(i).get("url");
                    String location = launchList.get(i).get("location");
                    String image = launchList.get(i).get("image");
                    String stationLocation = launchList.get(i).get("stationlocation");
                    String stationMap = launchList.get(i).get("stationmap");
                    String countryCode = launchList.get(i).get("countrycode");

                    Launch2 launch2 = new Launch2(id, name, net, tbdTime, tbdDate, windowStart, windowEnd, changed, url, location, image, stationLocation, stationMap, countryCode);
                    Intent detailsIntent = new Intent(LaunchListActivity.this, LaunchDetails.class);
                    detailsIntent.putExtra("launch", launch2);
                    startActivity(detailsIntent);

                }
            });
        }

    }
}
