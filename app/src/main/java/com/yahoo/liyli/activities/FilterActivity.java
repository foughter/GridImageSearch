package com.yahoo.liyli.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import com.yahoo.liyli.gridimagesearch.R;
import com.yahoo.liyli.models.Filter;

public class FilterActivity extends Activity {

    private Filter filter;
    EditText etSiteFilter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter);

        filter = (Filter) getIntent().getSerializableExtra("filter");
        etSiteFilter = (EditText)findViewById(R.id.etSiteFilter);

        setupColorFilter();
        setupImageSizeFilter();
        setupImageTypeFilter();
        etSiteFilter.setText(filter.site);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.filter, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupColorFilter(){
        // setup color filter spinner
        Spinner spColorFilter = (Spinner) findViewById(R.id.spColorFilter);
        ArrayAdapter<CharSequence> adapterColorFilter = ArrayAdapter.createFromResource(this,
                R.array.image_color, android.R.layout.simple_spinner_item);
        adapterColorFilter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spColorFilter.setAdapter(adapterColorFilter);
        spColorFilter.setSelection(filter.colorIdx);
        spColorFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int pos, long id) {
                filter.colorIdx = pos;
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void setupImageSizeFilter(){
        // setup image size spinner
        Spinner spImageSize = (Spinner) findViewById(R.id.spImageSize);
        ArrayAdapter<CharSequence> adapterImageSize = ArrayAdapter.createFromResource(this,
                R.array.image_size, android.R.layout.simple_spinner_item);
        adapterImageSize.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spImageSize.setAdapter(adapterImageSize);
        spImageSize.setSelection(filter.imageSizeIdx);
        spImageSize.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int pos, long id) {
                filter.imageSizeIdx = pos;
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void setupImageTypeFilter(){
        // setup
        Spinner spImageType = (Spinner) findViewById(R.id.spImageType);
        ArrayAdapter<CharSequence> adapterImageType = ArrayAdapter.createFromResource(this,
                R.array.image_type, android.R.layout.simple_spinner_item);
        adapterImageType.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spImageType.setAdapter(adapterImageType);
        spImageType.setSelection(filter.imageTypeIdx);
        spImageType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int pos, long id) {
                filter.imageTypeIdx = pos;
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    public void onSave(View view) {
        Intent i = new Intent();

        filter.site = etSiteFilter.getText().toString();

        i.putExtra("filter", filter);
        setResult(RESULT_OK, i);
        this.finish();
    }

}
