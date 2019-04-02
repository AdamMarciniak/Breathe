package alpha.breathe;

import android.app.Application;
import android.content.res.Resources;
import com.crashlytics.android.Crashlytics;
import io.fabric.sdk.android.Fabric;

public class App extends Application {
    private static App mInstance;
    private static Resources res;

    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());
        mInstance = this;
        res = getResources();
    }

    public static App getInstance() {
        return mInstance;
    }

    public static Resources getResourses() {
        return res;
    }

}