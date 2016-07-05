package com.xyt.card;

import android.app.PendingIntent;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.toolbox.HttpHeaderParser;
import com.google.gson.Gson;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private NfcAdapter mAdapter;
    private PendingIntent mPendingIntent;
    private EditText et1;
    private EditText et2;
    private RadioButton rb1;
    private RadioButton rb2;
    private RadioButton rb3;
    private RadioButton rbt1;
    private RadioButton rbt2;
    private String url1 = "http://10.10.11.159:8080/hwms/api/";
    private String url2 = "http://eadqas.whchem.com:8080/hwms/api/";
    private String url3 = "http://eadqas.whchem.com:8080/hwms/api/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        et1 = (EditText) findViewById(R.id.et1);
        et2 = (EditText) findViewById(R.id.et2);

        rb1 = (RadioButton) findViewById(R.id.rb1);
        rb2 = (RadioButton) findViewById(R.id.rb2);
        rb3 = (RadioButton) findViewById(R.id.rb3);

        rbt1 = (RadioButton) findViewById(R.id.rbt1);
        rbt2 = (RadioButton) findViewById(R.id.rbt2);

        resolveIntent(getIntent());
        mAdapter = NfcAdapter.getDefaultAdapter(this);
        mPendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mAdapter != null) {
            mAdapter.enableForegroundDispatch(this, mPendingIntent, null, null);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mAdapter != null) {
            mAdapter.disableForegroundDispatch(this);
        }
    }

    @Override
    public void onNewIntent(Intent intent) {
        setIntent(intent);
        resolveIntent(intent);
    }

    private void resolveIntent(Intent intent) {
        String action = intent.getAction();
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action) || NfcAdapter.ACTION_TECH_DISCOVERED.equals(action) || NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {
            request(getHex(((Tag) intent.getParcelableExtra(NfcAdapter.EXTRA_TAG)).getId()));
        }
    }

    private String getHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (int i = bytes.length - 1; i >= 0; --i) {
            int b = bytes[i] & 0xff;
            if (b < 0x10) {
                sb.append('0');
            }
            sb.append(Integer.toHexString(b));
        }
//        for (byte aByte : bytes) {
//            int b = aByte & 0xff;
//            if (b < 0x10){
//                sb.append('0');
//            }
//            sb.append(Integer.toHexString(b));
//        }
        return sb.toString();
    }

    public void request(String tagId) {
        String url = (rb1.isChecked() ? url1 : rb2.isChecked() ? url2 : rb3.isChecked() ? url3 : url2) + (rbt1.isChecked() ? "em-car-card" : rbt2.isChecked() ? "em-person-card" : "em-car-card");
        Map<String, Object> params = new HashMap<>();
        params.put("card_id", tagId);
        params.put("card_code", TextUtils.isEmpty(et1.getText().toString()) ? tagId : et1.getText().toString());
        params.put("card_name", et2.getText().toString());
        ApplicationController.getInstance().addToRequestQueue(new GsonObjectRequest<>(Request.Method.POST, url, BaseBean.class, new Gson().toJson(params),
                response -> Toast.makeText(getBaseContext(), "success", Toast.LENGTH_SHORT).show(),
                error -> {
                    try {
                        Toast.makeText(getBaseContext(), new Gson().fromJson(new String(error.networkResponse.data, HttpHeaderParser.parseCharset(error.networkResponse.headers)), BaseBean.class).getContent(), Toast.LENGTH_SHORT).show();
                    } catch (NullPointerException e) {
                        Toast.makeText(getBaseContext(), "error", Toast.LENGTH_SHORT).show();
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }));
    }
}
