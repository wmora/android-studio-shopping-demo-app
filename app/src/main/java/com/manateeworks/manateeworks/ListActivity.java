package com.manateeworks.manateeworks;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.speech.RecognizerIntent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;


import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import io.smooch.ui.ConversationActivity;

import static android.os.Build.ID;
import static com.manateeworks.manateeworks.R.id.scun;

public class ListActivity extends AppCompatActivity {

    private EditText editTxt;
    private ListView list;
    private CustomAdapter adapter;
    private ArrayList<ListObject> arrayList;
    SharedPreferences settings;
    private String resultType;
    private String resultContent;
    private final int REQ_CODE_SPEECH_INPUT = 100;
    private final int REQ_CODE_SCANNER = 200;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        editTxt = (EditText) findViewById(R.id.edittext);
        list = (ListView) findViewById(R.id.list);
        arrayList = new ArrayList<ListObject>();


        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
//        getSupportActionBar().setIcon(R.drawable.launcher); //also displays wide logo
        getSupportActionBar().setDisplayShowTitleEnabled(false); //optional
        adapter = new CustomAdapter();

        list.setAdapter(adapter);


        loadArray(this);
        adapter.notifyDataSetChanged();


            // eduttxt is the place from where you add the name for the items and is add to the list
        editTxt.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(
                        ListActivity.this.editTxt.getWindowToken(), 0);
                if ((event.getAction() == KeyEvent.ACTION_DOWN)
                        && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {

                    ListObject object = new ListObject();
                    object.name = editTxt.getText().toString();

                    arrayList.add(object);
                    saveArray();
                    adapter.notifyDataSetChanged();
                    editTxt.setText("");
                    editTxt.clearFocus();


                }
                return false;
            }

        });

        // here is the click on the list row and show you the information for the item
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                AlertDialog.Builder builder = new AlertDialog.Builder(ListActivity.this);

                if (!arrayList.get(position).type.equals(""))
                    builder.setMessage(arrayList.get(position).type + ": " + arrayList.get(position).code);


                builder.setCancelable(false);
                builder.setTitle(arrayList.get(position).name);
                builder.setPositiveButton("DONE", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {


                    }
                });

                builder.setNegativeButton("SHARE", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                        sharingIntent.setType("text/plain");
                        String shareBody = "Here is the share content body";
                        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Like to Share this barcode");
                        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
                        startActivity(Intent.createChooser(sharingIntent, "Share via"));
                    }
                });

                AlertDialog dialog = builder.create();

                dialog.show();
            }
        });


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.scun, menu);

        return super.onCreateOptionsMenu(menu);
    }



        // googltalk icon in the action bar and you can use your voice to add item in the list
        // scun icon is open the scanner and you can add items in the list by scanning the barcode

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case scun:
                Intent intent = new Intent(ListActivity.this, ActivityCapture.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivityForResult(intent, REQ_CODE_SCANNER);
                break;
            case R.id.google_talk:

                promptSpeechInput();

                break;
            case R.id.help:
                ConversationActivity.show(this);
                break;
            default:
                return false;
        }
        return true;
    }

        // save the list in the SharedPreferences so when you close the application you dont lose the items in the list
    public boolean saveArray() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor mEdit1 = sp.edit();

        mEdit1.putInt("Status_size", arrayList.size());



        for (int i = 0; i < arrayList.size(); i++) {
            mEdit1.remove("Status_" + i);
            try {
                JSONObject cacheJSON = new JSONObject();

                cacheJSON.put("name", arrayList.get(i).name);
                cacheJSON.put("code", arrayList.get(i).code);
                cacheJSON.put("type", arrayList.get(i).type);

                mEdit1.putString("Status_" + i, cacheJSON.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return mEdit1.commit();

    }

        // load the list with items whe you open the application
    public void loadArray(Context mContext) {
        SharedPreferences mSharedPreference1 = PreferenceManager.getDefaultSharedPreferences(mContext);
        arrayList.clear();
        int size = mSharedPreference1.getInt("Status_size", 0);

        for (int i = 0; i < size; i++) {
            try {
                JSONObject cacheJSON = new JSONObject(mSharedPreference1.getString("Status_" + i, null));
                ListObject object = new ListObject();
                object.name = cacheJSON.getString("name");
                object.code = cacheJSON.getString("code");
                object.type = cacheJSON.getString("type");

                arrayList.add(object);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }


        // this is the adapter
    private Context context;

    public class CustomAdapter extends BaseAdapter {
        public CustomAdapter() {

            super();
        }

        Holder holder;

        @Override
        public int getCount() {
            return arrayList.size();
        }

        @Override
        public Object getItem(int position) {
            return arrayList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return arrayList.indexOf(getItem(position));
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                holder = new Holder();
                convertView = getLayoutInflater().inflate(R.layout.list_row, parent, false);
            }

            holder.txtCell = (TextView) convertView.findViewById(R.id.list_row);
            holder.delete = (ImageView) convertView.findViewById(R.id.delete);
            holder.imgCode = (ImageView) convertView.findViewById(R.id.imgCode);
            holder.editname = (ImageView) convertView.findViewById(R.id.editname);
            holder.txtDescription = (TextView) convertView.findViewById(R.id.txtDescription);
            holder.txtCell.setText(arrayList.get(position).name);


            holder.txtDescription.setVisibility(arrayList.get(position).type.equals("") ? View.GONE : View.VISIBLE);
            holder.txtDescription.setText((arrayList.get(position).type + ": " + arrayList.get(position).code));

            final int finalPosition = position;

            // delete imagebutton is used for deleting the items from the list
            holder.delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    AlertDialog.Builder adb = new AlertDialog.Builder(ListActivity.this);
                    adb.setMessage("Are you sure you want to delete " + arrayList.get(finalPosition).name);
                    adb.setNegativeButton("Cancel", null);
                    adb.setPositiveButton("Ok", new AlertDialog.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {

                            arrayList.remove(finalPosition);
                            adapter.notifyDataSetChanged();
                            saveArray();
                        }
                    });
                    adb.show();


                }
            });


                // click on image in left of the row ( barcode icon ) opens the barcode and add the code that you are scunning to the item
            holder.imgCode.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder adb = new AlertDialog.Builder(ListActivity.this);
                    adb.setMessage("Add barcode for this item " + arrayList.get(finalPosition).name);
                    adb.setNegativeButton("Cancel", null);
                    adb.setPositiveButton("Ok", new AlertDialog.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {

                            Intent intent = new Intent(ListActivity.this, ActivityCapture.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                            intent.putExtra("position", finalPosition);
                            startActivityForResult(intent, REQ_CODE_SCANNER);

                        }
                    });
                    adb.show();


                }
            });

                // editname is frome where you can change the name to the item
            holder.editname.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    final AlertDialog.Builder adb = new AlertDialog.Builder(ListActivity.this);
                    adb.setMessage("Are you sure you want to edit the name for " + arrayList.get(finalPosition).name);
                    final EditText input = new EditText(ListActivity.this);
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                    input.requestFocus();

                    adb.setView(input, 100, 50, 100, 0);
                    adb.setNegativeButton("Cancel", new AlertDialog.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(input.getWindowToken(), 0);
                        }
                    });

                    InputMethodManager immm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    immm.hideSoftInputFromWindow(input.getWindowToken(), 0);
                    adb.setPositiveButton("Ok", new AlertDialog.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {


                            ListObject object = arrayList.get(finalPosition);
                            object.name = input.getText().toString();
                            saveArray();
                            adapter.notifyDataSetChanged();
                            input.setText("");


                            Toast.makeText(getApplicationContext(), "The name is changed", Toast.LENGTH_SHORT).show();
                            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(input.getWindowToken(), 0);


                        }
                    });
                    adb.show();


                }
            });


            return convertView;
        }

        public class Holder {
            TextView txtCell, txtDescription;
            ImageView delete, imgCode, editname;
        }

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

    }
            // code for google talk
    private void promptSpeechInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                getString(R.string.speech_prompt));

        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getApplicationContext(),
                    getString(R.string.speech_not_supported),
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
            // google talk code
        switch (requestCode) {
            case REQ_CODE_SPEECH_INPUT: {
                if (resultCode == RESULT_OK && null != data) {

                    ArrayList<String> result = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    editTxt.setText(result.get(0));

                    getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);


                }
                break;
            }
                // callback from scanner and add the resoult to the item
            case REQ_CODE_SCANNER: {
                if (resultCode == RESULT_OK) {
                    if (data.hasExtra("position")) {
                        String type = data.getStringExtra("type");
                        String code = data.getStringExtra("code");
                        int position = data.getIntExtra("position", 0);

                        ListObject object = arrayList.get(position);
                        object.type = type;
                        object.code = code;

                        saveArray();
                        adapter.notifyDataSetChanged();

                        //  callback from scanner and add new item in the list with code in the item
                    } else {
                        String type = data.getStringExtra("type");
                        String code = data.getStringExtra("code");

                        ListObject object = new ListObject();
                        object.name = editTxt.getText().toString();
                        object.code = code;
                        object.type = type;

                        arrayList.add(object);
                        saveArray();
                        adapter.notifyDataSetChanged();
                    }
                }
                break;
            }

        }
    }


}
