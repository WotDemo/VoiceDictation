package ysn.com.voicedictation.util;

import android.Manifest;

public class UShareEntry {
    public static final String[] PERMISSION_LIST = new String[]{
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.READ_CONTACTS,
    };
}
