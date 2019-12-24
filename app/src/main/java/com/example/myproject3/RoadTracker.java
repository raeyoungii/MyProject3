package com.example.myproject3;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class RoadTracker {
    private static final String TAG = "RoadTracker";
    private ArrayList<com.google.android.gms.maps.model.LatLng> mapPoints;

    private int totalDistance;

    public ArrayList<com.google.android.gms.maps.model.LatLng> getJsonData(final LatLng startPoint, final LatLng endPoint) {
        Thread thread = new Thread() {
            @Override
            public void run() {
                HttpClient httpClient = new DefaultHttpClient();

                String urlString = "https://api2.sktelecom.com/tmap/routes?version=1&format=json&appKey=a9c5d70d-427e-4e61-bc9c-da34d73ed949";
                try {
                    URI uri = new URI(urlString);

                    HttpPost httpPost = new HttpPost();
                    httpPost.setURI(uri);

                    List<BasicNameValuePair> nameValuePairs = new ArrayList<BasicNameValuePair>();
                    nameValuePairs.add(new BasicNameValuePair("startX", Double.toString(startPoint.longitude)));
                    nameValuePairs.add(new BasicNameValuePair("startY", Double.toString(startPoint.latitude)));

                    nameValuePairs.add(new BasicNameValuePair("endX", Double.toString(endPoint.longitude)));
                    nameValuePairs.add(new BasicNameValuePair("endY", Double.toString(endPoint.latitude)));

                    nameValuePairs.add(new BasicNameValuePair("startName", "출발지"));
                    nameValuePairs.add(new BasicNameValuePair("endName", "도착지"));

                    nameValuePairs.add(new BasicNameValuePair("reqCoordType", "WGS84GEO"));
                    nameValuePairs.add(new BasicNameValuePair("resCoordType", "WGS84GEO"));

                    httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                    HttpResponse response = httpClient.execute(httpPost);

                    int code = response.getStatusLine().getStatusCode();
                    String message = response.getStatusLine().getReasonPhrase();
                    Log.d(TAG, "run: " + message);
                    String responseString;
                    if (response.getEntity() != null)
                        responseString = EntityUtils.toString(response.getEntity(), HTTP.UTF_8);
                    else
                        return;
                    String strData = "";

                    Log.d(TAG, "0\n");
                    JSONObject jAr = new JSONObject(responseString);

                    Log.d(TAG, "1\n");

                    JSONArray features = jAr.getJSONArray("features");
                    mapPoints = new ArrayList<>();


                    for (int i = 0; i < features.length(); i++) {
                        JSONObject test2 = features.getJSONObject(i);
                        if (i == 0) {
                            JSONObject properties = test2.getJSONObject("properties");
                            totalDistance += properties.getInt("totalDistance");
                        }
                        JSONObject geometry = test2.getJSONObject("geometry");
                        JSONArray coordinates = geometry.getJSONArray("coordinates");


                        String geoType = geometry.getString("type");
                        if (geoType.equals("Point")) {
                            double lonJson = coordinates.getDouble(0);
                            double latJson = coordinates.getDouble(1);

                            Log.d(TAG, "-");
                            Log.d(TAG, lonJson + "," + latJson + "\n");
                            LatLng point = new LatLng(latJson, lonJson);
                            mapPoints.add(point);

                        }
                        if (geoType.equals("LineString")) {
                            for (int j = 0; j < coordinates.length(); j++) {
                                JSONArray JLinePoint = coordinates.getJSONArray(j);
                                double lonJson = JLinePoint.getDouble(0);
                                double latJson = JLinePoint.getDouble(1);

                                Log.d(TAG, "-");
                                Log.d(TAG, lonJson + "," + latJson + "\n");
                                LatLng point = new LatLng(latJson, lonJson);

                                mapPoints.add(point);

                            }
                        }
                    }
                } catch (URISyntaxException e) {
                    Log.e(TAG, e.getLocalizedMessage());
                    e.printStackTrace();
                } catch (ClientProtocolException e) {
                    Log.e(TAG, e.getLocalizedMessage());
                    e.printStackTrace();
                } catch (IOException e) {
                    Log.e(TAG, e.getLocalizedMessage());
                    e.printStackTrace();
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            }
        };
        thread.start();

        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return mapPoints;
    }
}

