package com.mytrial.android;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.salesforce.androidsdk.app.SalesforceSDKManager;
import com.salesforce.androidsdk.rest.RestClient;
import com.salesforce.androidsdk.rest.RestRequest;
import com.salesforce.androidsdk.rest.RestResponse;
import com.salesforce.androidsdk.ui.SalesforceActivity;

import org.json.JSONArray;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;

public class NavigationContainerActivity extends SalesforceActivity implements NavigationView.OnNavigationItemSelectedListener {

    private RestClient client;
    private RoundedBitmapDrawable roundedBitmapDrawable;

    @Override
    public void onResume(final RestClient client) {
        // Keeping reference to rest client
        this.client = client;
        if(!TextUtils.isEmpty(client.getClientInfo().thumbnailUrl)) {
            System.out.println(client.getClientInfo().thumbnailUrl + "?oauth_token=" + client.getAuthToken());
//            new DownloadImageTask().execute(client.getClientInfo().thumbnailUrl);

            /*RestRequest restRequest = new RestRequest(RestRequest.RestMethod.GET, client.getClientInfo().thumbnailUrl, null);
            client.sendAsync(restRequest, new RestClient.AsyncRequestCallback() {
                @Override
                public void onSuccess(RestRequest request, RestResponse response) {
                    System.out.println(request);
                    System.out.println(response);
                }

                @Override
                public void onError(Exception exception) {
                    Log.e("photo download", "Error", exception);
                }
            });*/

            try {
                sendRequest("SELECT Email,Id,Name,FullPhotoUrl FROM User WHERE Id = '" + client.getClientInfo().userId + "'",
                        new DefaultAsyncResultCallback() {
                            @Override
                            public void onSuccess(RestRequest request, RestResponse response) {
                                try {
                                    JSONArray records = response.asJSONObject().getJSONArray("records");
                                    final String smallPhotoUrl = records.getJSONObject(0).getString("FullPhotoUrl");
                                    /*RestRequest restRequest = new RestRequest(RestRequest.RestMethod.GET, smallPhotoUrl, null);
                                    client.sendAsync(restRequest, new DefaultAsyncResultCallback() {
                                        @Override
                                        public void onSuccess(RestRequest request, RestResponse response) {

                                        }
                                    });*/
                                    new DownloadImageTask().execute(smallPhotoUrl);
                                } catch (Exception e) {
                                    onError(e);
                                }
                            }
                        });
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }


        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation_container);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        if (drawer != null) {
            drawer.addDrawerListener(toggle);
        }
        toggle.syncState();

        final NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        if (navigationView != null) {
            navigationView.setNavigationItemSelectedListener(this);
        }

    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_logged_in, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        final MenuItem item = menu.findItem(R.id.action_profile);
        if(roundedBitmapDrawable != null) {
            item.setIcon(roundedBitmapDrawable);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    private class DownloadImageTask extends AsyncTask<String, Void, Boolean> {

        protected Boolean doInBackground(String... urls) {
            String urldisplay = urls[0];
            try {


                java.net.URL url = new java.net.URL(urldisplay);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestProperty("Authorization", "Bearer " + client.getAuthToken());
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();
//                Bitmap myBitmap = BitmapFactory.decodeStream(input);
//                return myBitmap;


//                InputStream in = new java.net.URL(urldisplay).openStream();
                roundedBitmapDrawable = RoundedBitmapDrawableFactory.create(getResources(), input);
                roundedBitmapDrawable.setCircular(true);
                return true;
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            if(aBoolean) invalidateOptionsMenu();
        }
    }

    private void sendRequest(String soql, RestClient.AsyncRequestCallback callback) throws UnsupportedEncodingException {
        RestRequest restRequest = RestRequest.getRequestForQuery(getString(R.string.api_version), soql);

        client.sendAsync(restRequest, callback);
    }

    private abstract class DefaultAsyncResultCallback implements RestClient.AsyncRequestCallback {

        @Override
        public abstract void onSuccess(RestRequest request, RestResponse response);

            /*public void onSuccess(RestRequest request, RestResponse result) {
                try {
                    JSONArray records = result.asJSONObject().getJSONArray("records");
                    listAdapter.add(records.getJSONObject(0).getString("Name"));
                    System.out.println(result.asString());
                } catch (Exception e) {
                    onError(e);
                }

            }*/

            @Override
            public void onError(Exception exception) {
                Toast.makeText(NavigationContainerActivity.this,
                        NavigationContainerActivity.this.getString(SalesforceSDKManager.getInstance().getSalesforceR().stringGenericError(), exception.toString()),
                        Toast.LENGTH_LONG).show();
            }
    }

}
