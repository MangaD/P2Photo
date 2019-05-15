package pt.ulisboa.tecnico.cmov.p2photo.tasks;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.lang.ref.WeakReference;

import pt.ulisboa.tecnico.cmov.p2photo.GlobalClass;
import pt.ulisboa.tecnico.cmov.p2photo.R;
import pt.ulisboa.tecnico.cmov.p2photo.ServerConnection;
import pt.ulisboa.tecnico.cmov.p2photo.activities.MainMenuActivity;
import pt.ulisboa.tecnico.cmov.p2photo.activities.LoginActivity;

public class LogOutTask extends AsyncTask<Void, Void, Void> {

    public static final String TAG = "LogOutTask";

    private WeakReference<MainMenuActivity> activityReference;
    private ProgressDialog pd;
    GlobalClass context;

    public LogOutTask(GlobalClass ctx, MainMenuActivity activity) {

        activityReference = new WeakReference<>(activity);
        this.context = ctx;
        // Create Progress dialog
        pd = new ProgressDialog(activity);
        pd.setMessage(ctx.getString(R.string.logging_out));
        pd.setTitle("");
        pd.setIndeterminate(true);
        pd.setCancelable(false);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        pd.show();
    }

    @Override
    protected Void doInBackground(Void... values) {
        ServerConnection conn = context.getServerConnection();

        conn.disconnect();
        Log.d(TAG, "Disconnected");
        return null;
    }

    @Override
    protected void onPostExecute(Void v) {
        pd.dismiss();
        Log.d(TAG, context.getString(R.string.logged_out));
        Toast.makeText(activityReference.get().getApplicationContext(), context.getString(R.string.logged_out), Toast.LENGTH_LONG).show();
        Intent intent = new Intent(activityReference.get(), LoginActivity.class);
        activityReference.get().startActivity(intent);
    }
}