package com.kushal.gcmdemoapp;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class EventsDisplayActivity extends AppCompatActivity implements AdapterView.OnItemClickListener,AdapterView.OnItemLongClickListener{

    DBHelper database;
    private ListView v;
    private Cursor c;
    /*
        bundles for Listitem click and then launchactivity intents
         This will save frequent database operations
     */
    private Bundle listitems[];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_events_display);
        database = new DBHelper(this);
        v = (ListView)findViewById(R.id.listview);
        v.setOnItemClickListener(this);
        v.setOnItemLongClickListener(this);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.delete, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId())
        {
            case R.id.menu_delete:
                database.deleteData();
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        int count = database.getcount();
        //Log.d("KUSHAL","Database entries count = "+count);
        listitems = new Bundle[count];
        /*
            Keeping this in onResume
            so that each time user comes back the most recent updates are also shown
        */
        c = database.getdata();
        int i=0;
        if(count!=0)
        {
            if (c.moveToFirst()){
                do{
                    Bundle bundle = new Bundle();
                    bundle.putString("latitude",c.getString(1)); //lat
                    bundle.putString("longitude",c.getString(2));
                    bundle.putString("message",c.getString(3));
                    bundle.putString("hotness",c.getString(4));
                    bundle.putString("stuff",c.getString(5));
                    listitems[i] = bundle;
                    i++;
                }while(c.moveToNext());
            }
        }

        v.setAdapter(new ListViewCursorAdapter(this,c));
        super.onResume();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        //Log.d("KUSHAL","clicked item at:"+position);
        Intent i = new Intent(getApplicationContext(),DisplayMessage.class);
        i.putExtras(listitems[position]);
        startActivity(i);
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        //Log.d("KUSHAL","Lonng CLick!");
        return true;
    }

    public class ListViewCursorAdapter extends CursorAdapter {
        public ListViewCursorAdapter(Context context, Cursor cursor) {
            super(context, cursor, 0);
        }

        // The newView method is used to inflate a new view and return it,
        // you don't bind any data to the view at this point.
        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            return LayoutInflater.from(context).inflate(R.layout.listview_item, parent, false);
        }

        // The bindView method is used to bind all data to a given view
        // such as setting the text on a TextView.
        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            // Find fields to populate in inflated template
            TextView text1 = (TextView) view.findViewById(R.id.listview_text1);
            TextView text2 = (TextView) view.findViewById(R.id.listview_text2);
            // Extract properties from cursor
            String freestuff = "Free "+cursor.getString(cursor.getColumnIndexOrThrow("stuff"));
            String time = cursor.getString(cursor.getColumnIndexOrThrow("time"));
            // Populate fields with extracted properties
            text1.setText(freestuff);
            text2.setText(String.valueOf(time));
        }
    }

    }
