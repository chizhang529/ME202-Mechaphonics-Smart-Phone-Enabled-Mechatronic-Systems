package edu.stanford.me202.smartbike;

import android.app.Application;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import io.realm.Realm;
import io.realm.RealmConfiguration;

/**
 * Created by czhang on 4/16/17.
 */

public class SmarkBikeApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // Initialize realm once for all activities and services
        Realm.init(this);
        RealmConfiguration config = new RealmConfiguration.Builder()
                .name(Realm.DEFAULT_REALM_NAME)
                .deleteRealmIfMigrationNeeded()
                .build();
        Realm.setDefaultConfiguration(config);
    }
}
