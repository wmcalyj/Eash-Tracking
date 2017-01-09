package wmcalyj.eashtracking;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.webkit.ClientCertRequest;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import fr.castorflex.android.circularprogressbar.CircularProgressBar;

public class ResultViewActivity extends AppCompatActivity {
    private static final String MyTag = "ResultViewActivityTag";
    WebView webView;
    CircularProgressBar progressBar;

    public String changedHeaderHtml(String htmlText) {

        String head = "<head><meta name=\"viewport\" content=\"width=device-width, " +
                "user-scalable=yes\" /></head>";

        String closedTag = "</body></html>";
        String changeFontHtml = head + htmlText + closedTag;
        return changeFontHtml;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result_view);
        Intent intent = getIntent();
        webView = (WebView) findViewById(R.id.activity_result_web_view);
        webView.setInitialScale(1);
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setBuiltInZoomControls(true);

        String url = intent.getStringExtra(Constants.REQUEST_URL);
        Log.d(MyTag, "URL: " + url);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                dismissProgressBar();
            }
        });
        webView.loadUrl(url);
    }

    private void displayProgressBar() {
        if (progressBar == null) {
            progressBar = (CircularProgressBar) findViewById(R.id.loading_progress_bar_for_web);
        }
        progressBar.setVisibility(View.VISIBLE);
    }

    private void dismissProgressBar() {
        if (progressBar == null) {
            progressBar = (CircularProgressBar) findViewById(R.id.loading_progress_bar_for_web);
        }
        progressBar.setVisibility(View.GONE);
    }

}
