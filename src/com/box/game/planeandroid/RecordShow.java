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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

import com.admob.android.ads.AdView;
import com.admob.android.ads.SimpleAdListener;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.util.Linkify;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TableRow;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Button;
import android.widget.ToggleButton;
import android.widget.TableRow.LayoutParams;

/*
 * The activity is used to present ranking information, news 
 * retrieved from the server, as well as en/disable sensor 
 * control of the game
 */
public class RecordShow extends Activity {

    private TelephonyManager telManager;
    private String TAG = "RecordShow";

    private String userid;
    private int controlMethod = 0;

    private final int level1FiveStar = 300;
    private final int level1FourStar = 200;
    private final int level1ThreeStar = 100;
    private final int level1TwoStar = 50;
    private final int level1OneStar = 20;

    private final int level2FiveStar = 150;
    private final int level2FourStar = 100;
    private final int level2ThreeStar = 50;
    private final int level2TwoStar = 30;
    private final int level2OneStar = 10;

    private final int level3FiveStar = 100;
    private final int level3FourStar = 50;
    private final int level3ThreeStar = 30;
    private final int level3TwoStar = 15;
    private final int level3OneStar = 5;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        telManager = (TelephonyManager) this
                .getSystemService(TELEPHONY_SERVICE);
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.record);

        TableLayout tl = (TableLayout) findViewById(R.id.recordTableLayout);
        tl.setColumnStretchable(0, true);
        tl.setColumnStretchable(1, true);
        tl.setColumnStretchable(2, true);
        tl.setColumnStretchable(3, true);
        tl.setColumnShrinkable(0, true);
        tl.setColumnShrinkable(1, true);
        tl.setColumnShrinkable(2, true);
        tl.setColumnShrinkable(3, true);

        // Add world record label
        TextView labelWorldRecord = new TextView(this);
        labelWorldRecord.setText("World Record:");
        labelWorldRecord.setTextColor(Color.rgb(0x00, 0xFF, 0x00));
        labelWorldRecord.setTextSize(TypedValue.COMPLEX_UNIT_PT, 6);
        tl.addView(labelWorldRecord, new TableLayout.LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));

        DBAdapter db = new DBAdapter(this);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            userid = extras.getString("ID");
            Log.v(TAG, "userid: " + userid);
            if (userid.equalsIgnoreCase("") || userid == null) {
                userid = telManager.getLine1Number();
            }

            String s = getWorldRecord(userid, telManager.getLine1Number());
            if (s == null || s.equalsIgnoreCase("")) {

                db.open();
                Cursor c = db.getRecord(userid);
                if (c.getCount() == 0) {
                    db.insertRecord(
                            userid,
                            telManager.getLine1Number(),
                            1,
                            0,
                            "0");

                    // Add world record row
                    TableRow tableRowWorldRecord = new TableRow(this);
                    TextView textWorldRecord = new TextView(this);
                    textWorldRecord.setText("No record yet.");
                    textWorldRecord.setTextColor(Color.rgb(0xFF, 0x00, 0x00));
                    tableRowWorldRecord.addView(
                            textWorldRecord,
                            new TableRow.LayoutParams(LayoutParams.FILL_PARENT,
                                    LayoutParams.WRAP_CONTENT));
                    tl.addView(
                            tableRowWorldRecord,
                            new TableLayout.LayoutParams(
                                    LayoutParams.FILL_PARENT,
                                    LayoutParams.WRAP_CONTENT));

                    // Add best record label
                    TableRow tableRowBestRecordLabel = new TableRow(this);
                    TextView labelBestRecord = new TextView(this);
                    labelBestRecord
                            .setText("My Best Record: (Five ROBOs Top Tier)");
                    labelBestRecord.setTextColor(Color.rgb(0x00, 0xFF, 0x00));
                    labelBestRecord.setTextSize(TypedValue.COMPLEX_UNIT_PT, 6);
                    tableRowBestRecordLabel.addView(
                            labelBestRecord,
                            new TableRow.LayoutParams(LayoutParams.FILL_PARENT,
                                    LayoutParams.WRAP_CONTENT));
                    tl.addView(
                            tableRowBestRecordLabel,
                            new TableLayout.LayoutParams(
                                    LayoutParams.FILL_PARENT,
                                    LayoutParams.WRAP_CONTENT));

                    // Add best record row
                    TableRow tableRowBestRecord = new TableRow(this);
                    TextView textBestRecord = new TextView(this);
                    textBestRecord.setText("You have no record yet.");
                    textBestRecord.setTextColor(Color.rgb(0xFF, 0x00, 0x00));
                    tableRowBestRecord.addView(
                            textBestRecord,
                            new TableRow.LayoutParams(LayoutParams.FILL_PARENT,
                                    LayoutParams.WRAP_CONTENT));
                    tl.addView(
                            tableRowBestRecord,
                            new TableLayout.LayoutParams(
                                    LayoutParams.FILL_PARENT,
                                    LayoutParams.WRAP_CONTENT));
                } else {
                    c.moveToFirst();
                    double r1 = 0;
                    double r2 = 0;
                    double r3 = 0;
                    do {

                        TableRow tableRowWorldRecord = new TableRow(this);

                        TextView textLevel = new TextView(this);
                        textLevel.setText("Level: " + c.getInt(3));
                        textLevel.setTextColor(Color.rgb(0xFF, 0x00, 0x00));
                        tableRowWorldRecord.addView(textLevel);

                        TextView textPilotName = new TextView(this);
                        if (userid
                                .equalsIgnoreCase(telManager.getLine1Number())) {
                            textPilotName.setText("Pilot: " + "Me");
                        } else {
                            textPilotName.setText("Pilot: " + userid);
                        }
                        textPilotName.setTextColor(Color.rgb(0xFF, 0x00, 0x00));
                        tableRowWorldRecord.addView(textPilotName);

                        TextView textRecord = new TextView(this);
                        textRecord.setText(c.getDouble(4) + " sec");
                        textRecord.setTextColor(Color.rgb(0xFF, 0x00, 0x00));
                        tableRowWorldRecord.addView(textRecord);

                        tl.addView(
                                tableRowWorldRecord,
                                new TableLayout.LayoutParams(
                                        LayoutParams.FILL_PARENT,
                                        LayoutParams.WRAP_CONTENT));

                        if (c.getInt(3) == 1 && c.getDouble(4) > r1) {
                            r1 = c.getDouble(4);
                        }
                        if (c.getInt(3) == 2 && c.getDouble(4) > r2) {
                            r2 = c.getDouble(4);
                        }
                        if (c.getInt(3) == 3 && c.getDouble(4) > r3) {
                            r3 = c.getDouble(4);
                        }
                    } while (c.moveToNext());

                    // Add best record label
                    TextView labelBestRecord = new TextView(this);
                    labelBestRecord
                            .setText("My Best Record: (Five ROBOs Top Tier)");
                    labelBestRecord.setTextColor(Color.rgb(0x00, 0xFF, 0x00));
                    labelBestRecord.setTextSize(TypedValue.COMPLEX_UNIT_PT, 6);
                    tl.addView(
                            labelBestRecord,
                            new TableLayout.LayoutParams(
                                    LayoutParams.FILL_PARENT,
                                    LayoutParams.WRAP_CONTENT));

                    for (int i = 1; i <= 3; i++) {

                        TableRow tableRowBestRecord = new TableRow(this);

                        TextView textLevel = new TextView(this);
                        textLevel.setText("Level: " + i);
                        textLevel.setTextColor(Color.rgb(0xFF, 0x00, 0x00));
                        tableRowBestRecord.addView(textLevel);

                        TextView textRank = new TextView(this);
                        textRank.setText("Local record.");
                        textRank.setTextColor(Color.rgb(0xFF, 0x00, 0x00));
                        tableRowBestRecord.addView(textRank);

                        Bitmap bitmapOrg = null;
                        int imageFlag = 0;
                        if (i == 1) {
                            if (r1 >= level1FiveStar) {
                                bitmapOrg = BitmapFactory.decodeResource(
                                        getResources(),
                                        R.drawable.android_normal5);
                                imageFlag = 5;
                            } else if (r1 >= level1FourStar
                                    && r1 < level1FiveStar) {
                                bitmapOrg = BitmapFactory.decodeResource(
                                        getResources(),
                                        R.drawable.android_normal4);
                                imageFlag = 4;
                            } else if (r1 >= level1ThreeStar
                                    && r1 < level1FourStar) {
                                bitmapOrg = BitmapFactory.decodeResource(
                                        getResources(),
                                        R.drawable.android_normal3);
                                imageFlag = 3;
                            } else if (r1 >= level1TwoStar
                                    && r1 < level1ThreeStar) {
                                bitmapOrg = BitmapFactory.decodeResource(
                                        getResources(),
                                        R.drawable.android_normal2);
                                imageFlag = 2;
                            } else if (r1 >= level1OneStar
                                    && r1 < level1TwoStar) {
                                bitmapOrg = BitmapFactory.decodeResource(
                                        getResources(),
                                        R.drawable.android_normal1);
                                imageFlag = 1;
                            } else {
                                bitmapOrg = BitmapFactory.decodeResource(
                                        getResources(),
                                        R.drawable.android_normal);
                                imageFlag = 0;
                            }
                        }

                        if (i == 2) {
                            if (r2 >= level2FiveStar) {
                                bitmapOrg = BitmapFactory.decodeResource(
                                        getResources(),
                                        R.drawable.android_normal5);
                                imageFlag = 5;
                            } else if (r2 >= level2FourStar
                                    && r2 < level2FiveStar) {
                                bitmapOrg = BitmapFactory.decodeResource(
                                        getResources(),
                                        R.drawable.android_normal4);
                                imageFlag = 4;
                            } else if (r2 >= level2ThreeStar
                                    && r2 < level2FourStar) {
                                bitmapOrg = BitmapFactory.decodeResource(
                                        getResources(),
                                        R.drawable.android_normal3);
                                imageFlag = 3;
                            } else if (r2 >= level2TwoStar
                                    && r2 < level2ThreeStar) {
                                bitmapOrg = BitmapFactory.decodeResource(
                                        getResources(),
                                        R.drawable.android_normal2);
                                imageFlag = 2;
                            } else if (r2 >= level2OneStar
                                    && r2 < level2TwoStar) {
                                bitmapOrg = BitmapFactory.decodeResource(
                                        getResources(),
                                        R.drawable.android_normal1);
                                imageFlag = 1;
                            } else {
                                bitmapOrg = BitmapFactory.decodeResource(
                                        getResources(),
                                        R.drawable.android_normal);
                                imageFlag = 0;
                            }
                        }

                        if (i == 3) {
                            if (r3 >= level3FiveStar) {
                                bitmapOrg = BitmapFactory.decodeResource(
                                        getResources(),
                                        R.drawable.android_normal5);
                                imageFlag = 5;
                            } else if (r3 >= level3FourStar
                                    && r3 < level3FiveStar) {
                                bitmapOrg = BitmapFactory.decodeResource(
                                        getResources(),
                                        R.drawable.android_normal4);
                                imageFlag = 4;
                            } else if (r3 >= level3ThreeStar
                                    && r3 < level3FourStar) {
                                bitmapOrg = BitmapFactory.decodeResource(
                                        getResources(),
                                        R.drawable.android_normal3);
                                imageFlag = 3;
                            } else if (r3 >= level3TwoStar
                                    && r3 < level3ThreeStar) {
                                bitmapOrg = BitmapFactory.decodeResource(
                                        getResources(),
                                        R.drawable.android_normal2);
                                imageFlag = 2;
                            } else if (r3 >= level3OneStar
                                    && r3 < level3TwoStar) {
                                bitmapOrg = BitmapFactory.decodeResource(
                                        getResources(),
                                        R.drawable.android_normal1);
                                imageFlag = 1;
                            } else {
                                bitmapOrg = BitmapFactory.decodeResource(
                                        getResources(),
                                        R.drawable.android_normal);
                                imageFlag = 0;
                            }
                        }
                        int width = bitmapOrg.getWidth();
                        int height = bitmapOrg.getHeight();
                        int newWidth = 0;
                        if (imageFlag == 1) {
                            newWidth = 16;
                        } else if (imageFlag == 2) {
                            newWidth = 16 * 2 + 1;
                        } else if (imageFlag == 3) {
                            newWidth = 16 * 3 + 2;
                        } else if (imageFlag == 4) {
                            newWidth = 16 * 4 + 2;
                        } else if (imageFlag == 5) {
                            newWidth = 16 * 5 + 2;
                        } else {
                            newWidth = 8;
                        }
                        int newHeight = 18;
                        float scaleWidth = ((float) newWidth) / width;
                        float scaleHeight = ((float) newHeight) / height;
                        Matrix matrix = new Matrix();
                        matrix.postScale(scaleWidth, scaleHeight);
                        Bitmap resizedBitmap = Bitmap.createBitmap(
                                bitmapOrg,
                                0,
                                0,
                                width,
                                height,
                                matrix,
                                true);
                        ImageView imageView = new ImageView(this);
                        imageView.setScaleType(ImageView.ScaleType.CENTER);
                        imageView.setPadding(1, 1, 1, 1);
                        imageView.setImageBitmap(resizedBitmap);
                        tableRowBestRecord.addView(imageView);

                        TextView textRecord = new TextView(this);
                        if (i == 1) {
                            textRecord.setText(r1 + " sec");
                        } else if (i == 2) {
                            textRecord.setText(r2 + " sec");
                        } else {
                            textRecord.setText(r3 + " sec");
                        }
                        textRecord.setTextColor(Color.rgb(0xFF, 0x00, 0x00));
                        tableRowBestRecord.addView(textRecord);

                        tl.addView(
                                tableRowBestRecord,
                                new TableLayout.LayoutParams(
                                        LayoutParams.FILL_PARENT,
                                        LayoutParams.WRAP_CONTENT));
                    }
                }

                c.close();
            } else {
                String entries[] = s.split(",");
                int recordsCount = 0;
                for (int i = 0; i < 3; i++) {
                    String items[] = entries[i].split("_");
                    if (items[0].equalsIgnoreCase("null")) {
                        recordsCount++;
                        continue;
                    }

                    TableRow tableRowWorldRecord = new TableRow(this);
                    tableRowWorldRecord
                            .setLayoutParams(new TableRow.LayoutParams(
                                    LayoutParams.WRAP_CONTENT,
                                    LayoutParams.WRAP_CONTENT));

                    TextView textLevel = new TextView(this);
                    textLevel.setText("Level: " + items[1]);
                    textLevel.setTextColor(Color.rgb(0xFF, 0x00, 0x00));
                    textLevel.setLayoutParams(new TableRow.LayoutParams(
                            LayoutParams.WRAP_CONTENT,
                            LayoutParams.WRAP_CONTENT));
                    tableRowWorldRecord.addView(textLevel);

                    TextView textPilotName = new TextView(this);
                    textPilotName.setText("Pilot: " + items[0]);
                    textPilotName.setTextColor(Color.rgb(0xFF, 0x00, 0x00));
                    textPilotName.setLayoutParams(new TableRow.LayoutParams(
                            LayoutParams.WRAP_CONTENT,
                            LayoutParams.WRAP_CONTENT));
                    tableRowWorldRecord.addView(textPilotName);

                    TextView textRecord = new TextView(this);
                    textRecord.setText(items[2] + " sec");
                    textRecord.setTextColor(Color.rgb(0xFF, 0x00, 0x00));
                    textRecord.setLayoutParams(new TableRow.LayoutParams(
                            LayoutParams.WRAP_CONTENT,
                            LayoutParams.WRAP_CONTENT));
                    tableRowWorldRecord.addView(textRecord);

                    tl.addView(
                            tableRowWorldRecord,
                            new TableLayout.LayoutParams(
                                    LayoutParams.WRAP_CONTENT,
                                    LayoutParams.WRAP_CONTENT));
                }
                if (recordsCount == 3) {
                    // Add world record row
                    TextView textWorldRecord = new TextView(this);
                    textWorldRecord.setText("No record yet.");
                    textWorldRecord.setTextColor(Color.rgb(0xFF, 0x00, 0x00));
                    tl.addView(
                            textWorldRecord,
                            new TableLayout.LayoutParams(
                                    LayoutParams.FILL_PARENT,
                                    LayoutParams.WRAP_CONTENT));
                }

                // Add best record label
                TextView labelBestRecord = new TextView(this);
                labelBestRecord
                        .setText("My Best Record: (Five ROBOs Top Tier)");
                labelBestRecord.setTextColor(Color.rgb(0x00, 0xFF, 0x00));
                labelBestRecord.setTextSize(TypedValue.COMPLEX_UNIT_PT, 6);
                tl.addView(labelBestRecord, new TableLayout.LayoutParams(
                        LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));

                int bestRecordsCount = 0;
                for (int i = 3; i < 6; i++) {
                    String items[] = entries[i].split("_");
                    if (items[2].equalsIgnoreCase("0")) {
                        bestRecordsCount++;
                        continue;
                    }

                    TableRow tableRowBestRecord = new TableRow(this);

                    TextView textLevel = new TextView(this);
                    textLevel.setText("Level: " + items[1]);
                    textLevel.setTextColor(Color.rgb(0xFF, 0x00, 0x00));
                    tableRowBestRecord.addView(textLevel);

                    TextView textRank = new TextView(this);
                    textRank.setText("RANK " + items[3]);
                    textRank.setTextColor(Color.rgb(0xFF, 0x00, 0x00));
                    tableRowBestRecord.addView(textRank);

                    Bitmap bitmapOrg = null;
                    int imageFlag = 0;
                    if (i == 3) {
                        if (Double.valueOf(items[2]) >= level1FiveStar) {
                            bitmapOrg = BitmapFactory.decodeResource(
                                    getResources(),
                                    R.drawable.android_normal5);
                            imageFlag = 5;
                        } else if (Double.valueOf(items[2]) >= level1FourStar
                                && Double.valueOf(items[2]) < level1FiveStar) {
                            bitmapOrg = BitmapFactory.decodeResource(
                                    getResources(),
                                    R.drawable.android_normal4);
                            imageFlag = 4;
                        } else if (Double.valueOf(items[2]) >= level1ThreeStar
                                && Double.valueOf(items[2]) < level1FourStar) {
                            bitmapOrg = BitmapFactory.decodeResource(
                                    getResources(),
                                    R.drawable.android_normal3);
                            imageFlag = 3;
                        } else if (Double.valueOf(items[2]) >= level1TwoStar
                                && Double.valueOf(items[2]) < level1ThreeStar) {
                            bitmapOrg = BitmapFactory.decodeResource(
                                    getResources(),
                                    R.drawable.android_normal2);
                            imageFlag = 2;
                        } else if (Double.valueOf(items[2]) >= level1OneStar
                                && Double.valueOf(items[2]) < level1TwoStar) {
                            bitmapOrg = BitmapFactory.decodeResource(
                                    getResources(),
                                    R.drawable.android_normal1);
                            imageFlag = 1;
                        } else {
                            bitmapOrg = BitmapFactory.decodeResource(
                                    getResources(),
                                    R.drawable.android_normal);
                            imageFlag = 0;
                        }
                    }

                    if (i == 4) {
                        if (Double.valueOf(items[2]) >= level2FiveStar) {
                            bitmapOrg = BitmapFactory.decodeResource(
                                    getResources(),
                                    R.drawable.android_normal5);
                            imageFlag = 5;
                        } else if (Double.valueOf(items[2]) >= level2FourStar
                                && Double.valueOf(items[2]) < level2FiveStar) {
                            bitmapOrg = BitmapFactory.decodeResource(
                                    getResources(),
                                    R.drawable.android_normal4);
                            imageFlag = 4;
                        } else if (Double.valueOf(items[2]) >= level2ThreeStar
                                && Double.valueOf(items[2]) < level2FourStar) {
                            bitmapOrg = BitmapFactory.decodeResource(
                                    getResources(),
                                    R.drawable.android_normal3);
                            imageFlag = 3;
                        } else if (Double.valueOf(items[2]) >= level2TwoStar
                                && Double.valueOf(items[2]) < level2ThreeStar) {
                            bitmapOrg = BitmapFactory.decodeResource(
                                    getResources(),
                                    R.drawable.android_normal2);
                            imageFlag = 2;
                        } else if (Double.valueOf(items[2]) >= level2OneStar
                                && Double.valueOf(items[2]) < level2TwoStar) {
                            bitmapOrg = BitmapFactory.decodeResource(
                                    getResources(),
                                    R.drawable.android_normal1);
                            imageFlag = 1;
                        } else {
                            bitmapOrg = BitmapFactory.decodeResource(
                                    getResources(),
                                    R.drawable.android_normal);
                            imageFlag = 0;
                        }
                    }

                    if (i == 5) {
                        if (Double.valueOf(items[2]) >= level3FiveStar) {
                            bitmapOrg = BitmapFactory.decodeResource(
                                    getResources(),
                                    R.drawable.android_normal5);
                            imageFlag = 5;
                        } else if (Double.valueOf(items[2]) >= level3FourStar
                                && Double.valueOf(items[2]) < level3FiveStar) {
                            bitmapOrg = BitmapFactory.decodeResource(
                                    getResources(),
                                    R.drawable.android_normal4);
                            imageFlag = 4;
                        } else if (Double.valueOf(items[2]) >= level3ThreeStar
                                && Double.valueOf(items[2]) < level3FourStar) {
                            bitmapOrg = BitmapFactory.decodeResource(
                                    getResources(),
                                    R.drawable.android_normal3);
                            imageFlag = 3;
                        } else if (Double.valueOf(items[2]) >= level3TwoStar
                                && Double.valueOf(items[2]) < level3ThreeStar) {
                            bitmapOrg = BitmapFactory.decodeResource(
                                    getResources(),
                                    R.drawable.android_normal2);
                            imageFlag = 2;
                        } else if (Double.valueOf(items[2]) >= level3OneStar
                                && Double.valueOf(items[2]) < level3TwoStar) {
                            bitmapOrg = BitmapFactory.decodeResource(
                                    getResources(),
                                    R.drawable.android_normal1);
                            imageFlag = 1;
                        } else {
                            bitmapOrg = BitmapFactory.decodeResource(
                                    getResources(),
                                    R.drawable.android_normal);
                            imageFlag = 0;
                        }
                    }
                    int width = bitmapOrg.getWidth();
                    int height = bitmapOrg.getHeight();
                    int newWidth = 0;
                    if (imageFlag == 1) {
                        newWidth = 16;
                    } else if (imageFlag == 2) {
                        newWidth = 16 * 2 + 1;
                    } else if (imageFlag == 3) {
                        newWidth = 16 * 3 + 2;
                    } else if (imageFlag == 4) {
                        newWidth = 16 * 4 + 2;
                    } else if (imageFlag == 5) {
                        newWidth = 16 * 5 + 2;
                    } else {
                        newWidth = 8;
                    }
                    int newHeight = 18;
                    float scaleWidth = ((float) newWidth) / width;
                    float scaleHeight = ((float) newHeight) / height;
                    Matrix matrix = new Matrix();
                    matrix.postScale(scaleWidth, scaleHeight);
                    Bitmap resizedBitmap = Bitmap.createBitmap(
                            bitmapOrg,
                            0,
                            0,
                            width,
                            height,
                            matrix,
                            true);
                    ImageView imageView = new ImageView(this);
                    imageView.setScaleType(ImageView.ScaleType.CENTER);
                    imageView.setPadding(1, 1, 1, 1);
                    imageView.setImageBitmap(resizedBitmap);
                    tableRowBestRecord.addView(imageView);

                    TextView textRecord = new TextView(this);
                    textRecord.setText(items[2] + " sec");
                    textRecord.setTextColor(Color.rgb(0xFF, 0x00, 0x00));
                    tableRowBestRecord.addView(textRecord);

                    tl.addView(
                            tableRowBestRecord,
                            new TableLayout.LayoutParams(
                                    LayoutParams.FILL_PARENT,
                                    LayoutParams.WRAP_CONTENT));
                }
                if (bestRecordsCount == 3) {
                    // Add best record row
                    TextView textBestRecord = new TextView(this);
                    textBestRecord.setText("You have no record yet.");
                    textBestRecord.setTextColor(Color.rgb(0xFF, 0x00, 0x00));
                    tl.addView(
                            textBestRecord,
                            new TableLayout.LayoutParams(
                                    LayoutParams.FILL_PARENT,
                                    LayoutParams.WRAP_CONTENT));
                }
            }

        }
        db.close();

        View delimitor1 = new View(this);
        delimitor1.setBackgroundColor(Color.rgb(0x90, 0x90, 0x90));
        tl.addView(delimitor1, new TableLayout.LayoutParams(
                LayoutParams.FILL_PARENT, 2));

        TextView chooseControlLabel = new TextView(this);
        chooseControlLabel.setText("Choose control method BELOW");
        chooseControlLabel.setTextColor(Color.rgb(0x00, 0xFF, 0x00));
        tl.addView(chooseControlLabel, new TableLayout.LayoutParams(
                LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));

        final ToggleButton orientationOrDPADToggleButton = new ToggleButton(
                this);
        orientationOrDPADToggleButton.setChecked(false);
        orientationOrDPADToggleButton.setTextColor(Color.rgb(0xFF, 0x00, 0x00));
        orientationOrDPADToggleButton.setText("Orientation DISabled");
        orientationOrDPADToggleButton.setTextOn("Orientation ENabled");
        orientationOrDPADToggleButton.setTextOff("Orientation DISabled");
        orientationOrDPADToggleButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (orientationOrDPADToggleButton.isChecked()) {
                    controlMethod = 1;
                } else {
                    controlMethod = 0;
                }
            }
        });
        tl.addView(orientationOrDPADToggleButton, new TableLayout.LayoutParams(
                LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));

        TextView startLabel = new TextView(this);
        startLabel.setText("Press button to START!");
        startLabel.setTextColor(Color.rgb(0x00, 0xFF, 0x00));
        tl.addView(startLabel, new TableLayout.LayoutParams(
                LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));

        Button startButton = new Button(this);
        startButton.setTextColor(Color.rgb(0xFF, 0x00, 0x00));
        startButton.setText("Go! Go! Go!");
        tl.addView(startButton, new TableLayout.LayoutParams(
                LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
        startButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                onLoginButtonClicked();
            }
        });

        View delimitor2 = new View(this);
        delimitor2.setBackgroundColor(Color.rgb(0x90, 0x90, 0x90));
        tl.addView(delimitor2, new TableLayout.LayoutParams(
                LayoutParams.FILL_PARENT, 2));

        TextView news = new TextView(this);
        news.setTextColor(Color.rgb(0x00, 0xFF, 0x00));
        news.setText(getNews(userid));
        Linkify.addLinks(news, Linkify.ALL);
        tl.addView(news, new TableLayout.LayoutParams(LayoutParams.FILL_PARENT,
                LayoutParams.WRAP_CONTENT));

        // AdManager.setTestDevices(new String[] { AdManager.TEST_EMULATOR,
        // "xxxxxxxxxxxxx",});
        AdView ad = new AdView(this);
        ad.setBackgroundColor(Color.rgb(0x00, 0x00, 0x00));
        ad.setPrimaryTextColor(Color.rgb(0xFF, 0xFF, 0xFF));
        ad.setSecondaryTextColor(Color.rgb(0xCC, 0xCC, 0xCC));
        ad.setAdListener(new TopGunListener());
        ad.setVisibility(View.VISIBLE);
        tl.addView(ad, new TableLayout.LayoutParams(LayoutParams.FILL_PARENT,
                LayoutParams.WRAP_CONTENT));

    }

    private void onLoginButtonClicked() {
        Intent i = new Intent(this, PlaneAndroid.class);
        i.putExtra("ID", userid);
        i.putExtra("Control", controlMethod);
        startActivity(i);
        finish();
    }

    public String getWorldRecord(String userid, String isbn) {

        String r = null;
        try {
            URL url = new URL(PlaneAndroidConstants.SERVER_URL
                    + "?command=record&userid=" + userid + "&isbn=" + isbn);

            BufferedReader in = new BufferedReader(new InputStreamReader(
                    url.openStream()));
            String str;
            while ((str = in.readLine()) != null) {
                r = str;
            }
            in.close();
        } catch (MalformedURLException e) {
        } catch (IOException e) {
        }
        return r;
    }

    public String getNews(String userid) {

        StringBuffer r = new StringBuffer();
        try {
            URL url = new URL(PlaneAndroidConstants.SERVER_URL
                    + "?command=news&userid=" + userid);
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    url.openStream()));
            String str;
            while ((str = in.readLine()) != null) {
                r.append(str);
                r.append("\n");
            }
            in.close();
        } catch (MalformedURLException e) {
        } catch (IOException e) {
        }
        return r.toString();
    }

    private class TopGunListener extends SimpleAdListener {

        /*
         * (non-Javadoc)
         * 
         * @see
         * com.admob.android.ads.AdView.SimpleAdListener#onFailedToReceiveAd
         * (com.admob.android.ads.AdView)
         */
        @Override
        public void onFailedToReceiveAd(AdView adView) {
            // TODO Auto-generated method stub
            super.onFailedToReceiveAd(adView);
        }

        /*
         * (non-Javadoc)
         * 
         * @seecom.admob.android.ads.AdView.SimpleAdListener#
         * onFailedToReceiveRefreshedAd(com.admob.android.ads.AdView)
         */
        @Override
        public void onFailedToReceiveRefreshedAd(AdView adView) {
            // TODO Auto-generated method stub
            super.onFailedToReceiveRefreshedAd(adView);
        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * com.admob.android.ads.AdView.SimpleAdListener#onReceiveAd(com.admob
         * .android.ads.AdView)
         */
        @Override
        public void onReceiveAd(AdView adView) {
            // TODO Auto-generated method stub
            super.onReceiveAd(adView);
        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * com.admob.android.ads.AdView.SimpleAdListener#onReceiveRefreshedAd
         * (com.admob.android.ads.AdView)
         */
        @Override
        public void onReceiveRefreshedAd(AdView adView) {
            // TODO Auto-generated method stub
            super.onReceiveRefreshedAd(adView);
        }

    }

    public void onFailedToReceiveAd(AdView adView) {
        Log.d("Lunar", "onFailedToReceiveAd");
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.admob.android.ads.AdView.AdListener#onFailedToReceiveRefreshedAd(
     * com.admob.android.ads.AdView)
     */
    public void onFailedToReceiveRefreshedAd(AdView adView) {
        Log.d("Lunar", "onFailedToReceiveRefreshedAd");
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.admob.android.ads.AdView.AdListener#onReceiveAd(com.admob.android
     * .ads.AdView)
     */
    public void onReceiveAd(AdView adView) {
        Log.d("Lunar", "onReceiveAd");
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.admob.android.ads.AdView.AdListener#onReceiveRefreshedAd(com.admob
     * .android.ads.AdView)
     */
    public void onReceiveRefreshedAd(AdView adView) {
        Log.d("Lunar", "onReceiveRefreshedAd");
    }
}
