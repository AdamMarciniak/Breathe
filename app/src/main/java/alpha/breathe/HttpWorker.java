package alpha.breathe;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class HttpWorker extends Worker {


    public HttpWorker(
            @NonNull Context appContext,
            @NonNull WorkerParameters workerParams) {
        super(appContext, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {

        Context applicationContext = getApplicationContext();

        try{
            DatabaseService dbService = new DatabaseService();
            dbService.sendFromQueueDatabase(applicationContext);
            return Result.success();
        }catch(Throwable throwable){

            Log.e("Worker", "Error with worker.", throwable);
            return Result.failure();

        }

    }
}


