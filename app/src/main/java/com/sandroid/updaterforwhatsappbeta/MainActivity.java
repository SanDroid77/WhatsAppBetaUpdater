package com.sandroid.updaterforwhatsappbeta;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.Permission;
import java.util.jar.Manifest;


public class MainActivity extends AppCompatActivity {

    public String installedVersion;
    public String latestVersion;
    private String webUrl = "http://www.whatsapp.com/android/";
    private String apkUrl = "http://www.whatsapp.com/android/current/WhatsApp.apk";
    private String filename = "WhatsApp_";
    private File sdcard = Environment.getExternalStorageDirectory();
    private File file;
    private Boolean FilenameExist;

    private TextView textView;
    private TextView txtInstalledVersion;
    private TextView txtLatestVersion;
    private Button btnDownload;
    private ProgressDialog pDialog;

    private static final String DEBUG_TAG = "HttpExample";
    private static final String COMPARE_TAG = "Compare";
    private Boolean newVersion = false;
    final private int REQUEST_CODE_ASK_PERMISSIONS = 123;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        inicializarElementos();

        // Obtiene la versión de WhatsApp instalada en el dispositivo
        try {
            installedVersion = getPackageManager().getPackageInfo("com.whatsapp", PackageManager.GET_META_DATA).versionName;
            txtInstalledVersion.setText(installedVersion);
        } catch (PackageManager.NameNotFoundException e) {
            textView.setText(this.getString(R.string.textViewNoPackageNameFound));
        }

        crearConexion();
    }

    public void inicializarElementos() {
        textView = (TextView) findViewById(R.id.textView);
        txtInstalledVersion = (TextView) findViewById(R.id.installedVersion);
        txtLatestVersion = (TextView) findViewById(R.id.latestVersion);
        btnDownload = (Button) findViewById(R.id.btnDownload);
        pDialog = new ProgressDialog(this);
    }

    public void crearConexion() {
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            textView.setText(this.getString(R.string.textViewLoading));
            new DownloadWebpageTask().execute(webUrl);
        } else {
            textView.setText(this.getString(R.string.textViewNoNET));
        }
    }

    // se ejecuta al presionar el botón "Descargar"
    public void downloadFile (View view) {
        int hasWriteSdcardPermission = ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (hasWriteSdcardPermission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{
                            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                    },
                    REQUEST_CODE_ASK_PERMISSIONS
            );
            return;
        }
        descargar();
    }

    public void descargar(){
        try {
            setProgressDialog();
            setFileToSdcard();
            new DownloadFileTask().execute(new URL(apkUrl));
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    public void setProgressDialog () {
        pDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        pDialog.setMessage(this.getString(R.string.ProgressDialogDownloading));
    }

    public void setFileToSdcard () {
        filename = filename + latestVersion + ".apk";
        file = new File(sdcard, filename);
        if (file.exists()) {
            FilenameExist = true;
            Log.d("File", "archivo creado");
        } else {
            FilenameExist = false;
        }
    }

    public void habilitarBoton() {
        if (CompareVersion.comparar(installedVersion, latestVersion)) {
            btnDownload.setEnabled(true);
        }
    }

    private class DownloadWebpageTask extends AsyncTask<String, Void, String> {
        private String htmlAndroid;

        @Override
        protected String doInBackground(String... urls) {
            try {
                // se descarga la página HTML de whatsapp.com/android para obtener el
                // número de versión disponible
                htmlAndroid = downloadUrl(urls[0]);
                String[] split = htmlAndroid.split(">");
                int i = 0;
                while (i < split.length) {
                    if (split[i].startsWith("Version")) {
                        split = split[i].split(" ");
                        latestVersion = split[1];
                        latestVersion = latestVersion.split("<")[0];
                        break;
                    }
                    i++;
                }

                //installedVersion = "2.12.96";
                if (CompareVersion.comparar(installedVersion, latestVersion)) {
                    // si hay nueva versión disponible de WhatsApp
                    newVersion = true;
                    return getResources().getString(R.string.textViewNewVersionAvailable);
                } else {
                    // si se utiliza la última versión
                    newVersion = false;
                    return getResources().getString(R.string.textViewUsingLatestVersion);
                }
            } catch (IOException e) {
                return getResources().getString(R.string.URLinvalid);
            }
        }

        @Override
        protected void onPostExecute(String result) {
            textView.setText(result);
            txtLatestVersion.setText(latestVersion);
            habilitarBoton();
        }

        private String downloadUrl(String myUrl) throws IOException {
            InputStream is = null;

            try {
                URL url = new URL(myUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(10000 /* Milliseconds */);
                conn.setConnectTimeout(15000);
                conn.setRequestMethod("GET");
                conn.setDoInput(true);
                // Starts the query
                conn.connect();
                int response = conn.getResponseCode();
                Log.d(DEBUG_TAG, "The response is: " + response);
                is = conn.getInputStream();

                // Convert the Input Stream into a string
                String contentAsString = readIt(is);
                return contentAsString;
            } finally {
                if (is != null) {
                    is.close();
                }
            }
        }

        public String readIt(InputStream stream) throws IOException {
            BufferedReader reader;
            reader = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
            StringBuilder total = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                total.append(line);
            }
            return total.toString();
        }

    }

    private class DownloadFileTask extends AsyncTask<URL, Integer, Boolean> {
        @Override
        protected Boolean doInBackground(URL... urls) {
            try {
                URL url = new URL(apkUrl);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.setDoOutput(true);
                urlConnection.connect();
                Log.d("URL Connection", "conectado");

                if (FilenameExist) return true;

                FileOutputStream fileOutput = new FileOutputStream(file);
                InputStream inputStream = urlConnection.getInputStream();

                // total size of the file
                int totalSize = urlConnection.getContentLength();
                // variable to store total downloaded bytes
                int downloadedSize = 0;
                pDialog.setMax(totalSize);

                byte[] buffer = new byte[1024];
                int bufferLength = 0;

                while ( (bufferLength = inputStream.read(buffer)) > 0 ) {
                    fileOutput.write(buffer, 0, bufferLength);
                    downloadedSize += bufferLength;
                    publishProgress(downloadedSize);
                    if (isCancelled()) break;
                }
                fileOutput.close();
                Log.d("End", "Se termino de ejecutar");
                return true;
            } catch (MalformedURLException e) {
                e.printStackTrace();
                return false;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            int progreso = values[0];
//          Log.d("Descarga", "Progreso: " + String.valueOf(progreso));
            pDialog.setProgress(progreso);
        }

        @Override
        protected void onPreExecute() {
            pDialog.setProgress(0);
            pDialog.show();
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (result) {
                pDialog.dismiss();
                installFile();
            }
            Log.d("Download", "deacarga lista");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_PERMISSIONS:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    descargar();
                } else {
                    Toast.makeText(this, "No tiene permisos para escribir en la SDCard", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void installFile () {
        Intent installApk = new Intent(Intent.ACTION_VIEW);
        installApk.setDataAndType(Uri.parse("file://" + sdcard.getPath() + "/" + filename), "application/vnd.android.package-archive");
//      Log.d("SDCARD", "sdcard path: " + sdcard.getPath() );
        installApk.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            startActivity(installApk);
        } catch (ActivityNotFoundException e) {
            AlertDialog errorInstalled = new AlertDialog.Builder(this).create();
            errorInstalled.setTitle("Algo salió mal");
            errorInstalled.setMessage("WhatsApp " + latestVersion + " no pudo instalarse. Por favor intente de nuevo.");
            errorInstalled.show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
