/*
Copyright 2011 codeoedoc

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */

package com.box.game.planeandroid;

import java.util.Enumeration;
import java.util.Hashtable;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.TableRow.LayoutParams;

/*
 * The login activity providing registration functionality
 */
public class LoginActivity extends Activity {

    public static final String TAG = "LoginActivity";

    private Button mLoginButton;
    private EditText mLoginIDEditText;
    private TextView mLoginEditTextLabel;

    private String selectedID = "";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        mLoginIDEditText = (EditText) findViewById(R.id.login_id_text);
        mLoginButton = (Button) findViewById(R.id.login_button);
        mLoginEditTextLabel = (TextView) findViewById(R.id.login_label);
        mLoginButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                onLoginButtonClicked();
            }
        });

        Hashtable<String, String> uniqueID = new Hashtable<String, String>();
        DBAdapter db = new DBAdapter(this);
        db.open();
        Cursor c = db.getAllRecords();
        if (c.getCount() != 0) {
            c.moveToFirst();
            do {
                uniqueID.put(c.getString(1), "");
            } while (c.moveToNext());
        }
        c.close();
        db.close();

        Spinner spinner = (Spinner) findViewById(R.id.spinner);
        if (uniqueID.size() != 0) {

            mLoginEditTextLabel
                    .setText("Input Pilot Name/Select From Dropdown");

            ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(
                    this, android.R.layout.simple_spinner_item);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
            Enumeration<String> ids = uniqueID.keys();
            while (ids.hasMoreElements()) {
                adapter.add(ids.nextElement());
            }
            spinner.setAdapter(adapter);
            
            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                public void onItemSelected(AdapterView<?> parent, View view,
                        int pos, long id) {
                    selectedID = parent.getItemAtPosition(pos).toString();
                }
                public void onNothingSelected(AdapterView<?> arg0) {
                }
            });

        } else {
            mLoginEditTextLabel.setText("Input Pilot Name");
            spinner.setVisibility(View.GONE);
        }

    }

    private void onLoginButtonClicked() {
        Intent i = new Intent(this, RecordShow.class);
        String inputID = mLoginIDEditText.getText().toString();
        if (inputID == null || inputID.equalsIgnoreCase("")) {
            inputID = selectedID;
        }
        i.putExtra("ID", inputID);
        startActivity(i);
    }
}
