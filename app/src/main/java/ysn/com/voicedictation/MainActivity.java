package ysn.com.voicedictation;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.tbruyelle.rxpermissions2.RxPermissions;

import io.reactivex.android.schedulers.AndroidSchedulers;
import ysn.com.voicedictation.util.UShareEntry;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    @SuppressLint("CheckResult")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        new RxPermissions(this)
                .request(UShareEntry.PERMISSION_LIST)
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(granted -> {
                    if (granted) {
                        init();
                    } else {
                        Toast.makeText(this, "权限不足", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
    }

    private void init() {
        setContentView(R.layout.activity_main);
        findViewById(R.id.main_activity_custom).setOnClickListener(this);
        findViewById(R.id.main_activity_dialog).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.main_activity_custom:
                startActivity( new Intent(MainActivity.this, CustomActivity.class));
                break;
            case R.id.main_activity_dialog:
                startActivity(new Intent(MainActivity.this, DialogActivity.class));
                break;
            default:
        }
    }
}

