package knf.mangabox;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.GravityEnum;
import com.afollestad.materialdialogs.MaterialDialog;
import com.thin.downloadmanager.DownloadRequest;
import com.thin.downloadmanager.DownloadStatusListener;
import com.thin.downloadmanager.ThinDownloadManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class Main extends AppCompatActivity {
    WebView webView;
    File descarga = new File(Environment.getExternalStorageDirectory() + "/MangaBox/cache", "NewApk.apk");
    MaterialDialog dialog;
    String act_link="https://raw.githubusercontent.com/jordyamc/Manga_Box/Github/app/version.txt";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        new checkVersion(this).execute(act_link);
        webView=(WebView) findViewById(R.id.wv_main);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setBuiltInZoomControls(true);
        webView.getSettings().setDisplayZoomControls(false);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setUseWideViewPort(true);
        webView.setWebViewClient(new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return super.shouldOverrideUrlLoading(view, url);
            }
        });
        webView.loadUrl("http://mangabox.cf/");
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (webView.canGoBack()){webView.goBack();}else {finish();}
    }

    public class checkVersion extends AsyncTask<String,String,String>{
        Context context;
        String _response;
        public checkVersion(Context context) {
            this.context=context;
        }

        @Override
        protected String doInBackground(String... params) {
            HttpURLConnection urlConnection;
            try {
                URL url=new URL(params[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestProperty("Content-length", "0");
                urlConnection.setRequestProperty("User-Agent", "Mozilla/5.0 ( compatible ) ");
                urlConnection.setRequestProperty("Accept", "*/*");
                urlConnection.setUseCaches(false);
                urlConnection.setConnectTimeout(20000);
                urlConnection.setAllowUserInteraction(false);
                urlConnection.connect();
                BufferedReader br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line="";
                while ((line = br.readLine()) != null) {
                    sb.append(line + "\n");
                }
                br.close();
                _response=sb.toString();
            }catch (Exception e){
                e.printStackTrace();
                _response="-1";
            }
            return _response;
        }

        @Override
        protected void onPostExecute(final String s) {
            super.onPostExecute(s);
            int versionCode=-1;
            try {
                versionCode = getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (versionCode<Integer.parseInt(s)){
                dialog=new MaterialDialog.Builder(context)
                        .title("Nueva Version")
                        .titleGravity(GravityEnum.CENTER)
                        .content("Desea actualizar?")
                        .positiveText("OK")
                        .negativeText("Omitit")
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull final MaterialDialog dialog, @NonNull DialogAction which) {
                                final NotificationManager notificationManager=(NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                                final NotificationCompat.Builder builder=new NotificationCompat.Builder(context);
                                builder.setContentTitle("Descargando Actualizacion")
                                        .setContentText(s)
                                        .setOngoing(true)
                                        .setSmallIcon(android.R.drawable.stat_sys_download);
                                notificationManager.notify(6991,builder.build());
                                final ThinDownloadManager downloadManager = new ThinDownloadManager();
                                Uri download = Uri.parse("https://github.com/jordyamc/Manga_Box/blob/Github/app/app-release.apk?raw=true");
                                final DownloadRequest downloadRequest = new DownloadRequest(download)
                                        .setDestinationURI(Uri.fromFile(descarga))
                                        .setDownloadListener(new DownloadStatusListener() {
                                            @Override
                                            public void onDownloadComplete(int id) {
                                                notificationManager.cancel(6991);
                                                Intent promptInstall = new Intent(Intent.ACTION_VIEW)
                                                        .setDataAndType(Uri.fromFile(descarga),
                                                                "application/vnd.android.package-archive");
                                                dialog.dismiss();
                                                finish();
                                                startActivity(promptInstall);
                                            }

                                            @Override
                                            public void onDownloadFailed(int id, int errorCode, String errorMessage) {
                                                builder.setOngoing(false)
                                                        .setContentTitle("Error al descargar")
                                                        .setProgress(0,0,false);
                                                notificationManager.notify(6991,builder.build());
                                            }

                                            @Override
                                            public void onProgress(int id, long totalBytes, int progress) {
                                                builder.setProgress(100,progress,false);
                                                notificationManager.notify(6991,builder.build());
                                            }
                                        });
                                downloadManager.add(downloadRequest);
                                dialog.dismiss();
                            }
                        })
                        .onNegative(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                dialog.dismiss();
                            }
                        })
                        .build();
                dialog.show();
            }else {Log.d("Actualizar","No Update");}
        }
    }
}
