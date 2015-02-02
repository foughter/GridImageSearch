package com.yahoo.liyli.gridimagesearch;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.yahoo.liyli.activities.FilterActivity;
import com.yahoo.liyli.activities.ImageDisplayActivity;
import com.yahoo.liyli.adapters.ImageResultAdapter;

import com.yahoo.liyli.models.Filter;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URL;
import java.util.ArrayList;


public class SearchActivity extends Activity {
    private EditText etQuery;
    private GridView gvResult;
    private ArrayList<ImageResult> imageResults;
    private ImageResultAdapter aImageResults;
    String query = null;
    int pagesLoaded = 0;
    Filter filter;
    private final int SEARCH_RESULT = 50;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        setupView();
        filter = new Filter();
        imageResults = new ArrayList<ImageResult>();
        // attach the data source to an adapter
        aImageResults = new ImageResultAdapter(this, imageResults);

        gvResult.setAdapter(aImageResults);
        gvResult.setOnScrollListener(new EndlessScrollListener() {
            @Override
            public void onLoadMore(int page, int totalItemsCount) {
                // Triggered only when new data needs to be appended to the list
                // Add whatever code is needed to append new items to your AdapterView
                customLoadMoreDataFromApi(page);
                // or customLoadMoreDataFromApi(totalItemsCount);
            }
        });
    }

    // Append more data into the adapter
    public void customLoadMoreDataFromApi(int offset) {
        // This method probably sends out a network request and appends new data items to your adapter.
        // Use the offset value and add it as a parameter to your API request to retrieve paginated data.
        // Deserialize API response and then construct new objects to append to the adapter

        Log.i("INFO", "load more "+offset);
        if (offset > 8 || query == null)
            return;
        int start = offset * 8;

//        String searchUrl = "https://ajax.googleapis.com/ajax/services/search/images?v=1.0&q="
//                + query  + "&rsz=8&start="+start;
        String searchUrl = generateSearchUrl(start);

        AsyncHttpClient client = new AsyncHttpClient();

        client.get(searchUrl, new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Log.d("DEBUG", response.toString());
                JSONArray imageResultsJson =  null;
                try {
                    imageResultsJson = response.getJSONObject("responseData").getJSONArray("results");
                    aImageResults.addAll(ImageResult.fromJSONArray(imageResultsJson));

                } catch (JSONException e){
                    e.printStackTrace();
                }
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.search, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (R.id.miRequest == id) {
            Intent i = new Intent(this, FilterActivity.class);

            i.putExtra("filter", filter);

            startActivityForResult(i, SEARCH_RESULT);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Handle the form data
        if (requestCode == SEARCH_RESULT) {
            if (resultCode == RESULT_OK) {
                // Toast YES or NO based on if age is greater 21
                filter = (Filter)data.getSerializableExtra("filter");
                // Get the age out of the form data
                String message = "image size: " + filter.imageSizeIdx + "color: "+filter.colorIdx;

                // for debugging:
                //Toast.makeText(this, message, Toast.LENGTH_SHORT).show();

                // now do a new search
                doNewSearch();
            }
        }
    }

    private void setupView() {
        etQuery = (EditText) findViewById(R.id.etQuery);
        gvResult = (GridView) findViewById(R.id.gvResult);
        gvResult.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent i = new Intent(SearchActivity.this, ImageDisplayActivity.class);
                ImageResult result = imageResults.get(position);
                i.putExtra("result", result);
                startActivity(i);
            }
        });
    }

    public void onImageSearch(View v){
        hideSoftKeyBoard();
        query = etQuery.getText().toString();

        doNewSearch();
    }

    private void doNewSearch() {
        AsyncHttpClient client = new AsyncHttpClient();

        String searchUrl = generateSearchUrl(0);

        client.get(searchUrl, new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Log.d("DEBUG", response.toString());
                JSONArray imageResultsJson =  null;
                try {
                    imageResultsJson = response.getJSONObject("responseData").getJSONArray("results");

                    // this is a new search. clear everything
                    imageResults.clear();

                    //Two ways to do this.
                    // 1) do 'addAll' on adapter directly
                    aImageResults.addAll(ImageResult.fromJSONArray(imageResultsJson));

                    // 2) the other way is to addAll on data source and then call adapter.notifyDataSetChanged
                    //imageResults.addAll(ImageResult.fromJSONArray(imageResultsJson));
                    //aImageResults.notifyDataSetChanged();

                    Log.i("INFO", "first load complete");
                } catch (JSONException e){
                    e.printStackTrace();
                }
            }
        });

        searchUrl = generateSearchUrl(8);
        client.get(searchUrl, new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Log.d("DEBUG", response.toString());
                JSONArray imageResultsJson =  null;
                try {
                    imageResultsJson = response.getJSONObject("responseData").getJSONArray("results");
                    aImageResults.addAll(ImageResult.fromJSONArray(imageResultsJson));

                    Log.i("INFO", "second load complete");

                } catch (JSONException e){
                    e.printStackTrace();
                }
            }
        });
    }

    private void hideSoftKeyBoard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);

        if(imm.isAcceptingText()) { // verify if the soft keyboard is open
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
    }

    private String generateSearchUrl(int startWith) {
        String searchUrl = "https://ajax.googleapis.com/ajax/services/search/images?v=1.0&q="
                + query  + "&rsz=8&start=" + startWith + generateUrlParameterForFiltering();

        return searchUrl;
    }

    private String generateUrlParameterForFiltering() {

        String color = "";
        switch (filter.colorIdx) {
            case 1:
                color = "imgcolor=black";
                break;
            case 2:
                color = "imgcolor=blue";
                break;
            case 3:
                color = "imgcolor=brown";
                break;
            case 4:
                color = "imgcolor=gray";
                break;
            case 5:
                color = "imgcolor=green";
                break;
            case 6:
                color = "imgcolor=orange";
                break;
            case 7:
                color = "imgcolor=peak";
                break;
            case 8:
                color = "imgcolor=purple";
                break;
            case 9:
                color = "imgcolor=red";
                break;
            case 10:
                color = "imgcolor=teal";
                break;
            case 11:
                color = "imgcolor=white";
                break;
            case 12:
                color = "imgcolor=yellow";
                break;
            default:
                break;
            }

        String sizeFilter =  "";
        switch (filter.imageSizeIdx) {
            case 1:
                sizeFilter = "imgsz=icon";
                break;
            case 2:
                sizeFilter = "imgsz=small";
                break;
            case 3:
                sizeFilter = "imgsz=medium";
                break;
            case 4:
                sizeFilter = "imgsz=large";
                break;
            case 5:
                sizeFilter = "imgsz=xlarge";
                break;
            case 6:
                sizeFilter = "imgsz=xxlarge";
                break;
            case 7:
                sizeFilter = "imgsz=huge";
                break;
            default:
                break;
        }

        String typeFilter = "";
        switch (filter.imageTypeIdx) {
            case 1:
                typeFilter = "imgtype=face";
                break;
            case 2:
                typeFilter = "imgtype=photo";
                break;
            case 3:
                typeFilter = "imgtype=clipart";
                break;
            case 4:
                typeFilter = "imgtype=lineart";
                break;
            default:
                break;
        }

        String siteFilter = "";
        if (filter.site.length() > 0)
            siteFilter = "as_sitesearch=" + filter.site;

        String UrlParams="";
        if (color.length() != 0) {
            UrlParams = "&" + color;
        }
        if (sizeFilter.length() != 0){
            UrlParams = UrlParams + "&" + sizeFilter;
        }
        if (typeFilter.length() != 0) {
            UrlParams= UrlParams + "&" + typeFilter;
        }
        if (siteFilter.length() != 0) {
            UrlParams = UrlParams + "&" + siteFilter;
        }

        return UrlParams;
    }

}
