package ai.hellodoctor;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.net.URISyntaxException;

import androidx.annotation.NonNull;

class ClientManager extends WebViewClient {
    Activity activity;
    MainActivity mainActivity;
    private boolean sendToken = false;

    private GpsTracker gpsTracker;
    private int gpsCount = 0;
    private double curLat, curLng;
    public LocationHandler locationHandler = new LocationHandler();
    public boolean gps_enabled = false;

    ClientManager(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    ClientManager(Activity activity, MainActivity mainActivity) {
        this.mainActivity = mainActivity;
        this.activity = activity;
    }

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        super.onPageStarted(view, url, favicon);
    }

    // 로딩종료시
    @Override
    public void onPageFinished(WebView view, String url) {
        super.onPageFinished(view, url);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            CookieSyncManager.getInstance().sync();
        } else {
            CookieManager.getInstance().flush();
        }

        // 이전 히스토리 삭제
        if (url.equals(mainActivity.getString(R.string.indexPath))) {
            view.clearHistory();
            Log.d("로그:히스토리삭제", url);
        }

        // fcm 토큰전달
       if (!sendToken && mainActivity.TOKEN != "") {
            mainActivity.webView.loadUrl("javascript:fcmKey('" + mainActivity.TOKEN + "', 'AOS')");
            sendToken = true;
            Log.d("로그 js:fcmKey()", mainActivity.TOKEN);
        }

        // 현재위치 호출
        if (gpsCount == 0) locationHandler.sendEmptyMessage(0);
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
        String url = request.getUrl().toString();
        Log.d("로그:url", url);

        // 외부앱 실행
        if (url.startsWith("intent:")) {
            Intent intent = null;
            try {
                intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME);
                if (intent != null) {
                    mainActivity.startActivity(intent);
                }
            } catch (URISyntaxException e) {
                //URI 문법 오류 시 처리 구간
            } catch (ActivityNotFoundException e) {
                String packageName = intent.getPackage();
                if (!packageName.equals("")) {
                    // 앱이 설치되어 있지 않을 경우 구글마켓 이동
                    mainActivity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + packageName)));
                }
            }
            return true;

        } else if (url.startsWith("https://play.google.com/store/apps/details?id=") || url.startsWith("market://details?id=")) {
            //표준창 내 앱설치하기 버튼 클릭 시 PlayStore 앱으로 연결하기 위한 로직
            Uri uri = Uri.parse(url);
            String packageName = uri.getQueryParameter("id");
            if (packageName != null && !packageName.equals("")) {
                // 구글마켓 이동
                mainActivity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + packageName)));
            }
            return true;

        }
        //전화걸기
        else if (url.startsWith("tel:")) {
            Intent i = new Intent(Intent.ACTION_DIAL, Uri.parse(url));
            mainActivity.startActivity(i);
            return true;
        }
        // 카카오
        else if (url.startsWith("kakaotalk:")) {
            Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            // Log.d("Uri.parse(url)", Uri.parse(url) + "");
            mainActivity.startActivity(i);
            return true;
        }

        /*
        if (url.contains(mainActivity.getString(R.string.inAppPath))) {
        } else {
            // 외부링크 연결
            Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            mainActivity.startActivity(i);
            return true;
        }
        */

        view.loadUrl(url);
        //view.clearCache(true);
        return true;
    }

    // 현재위치 호출
    public class LocationHandler extends Handler {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            gpsTracker = new GpsTracker(mainActivity);
            curLat = gpsTracker.getLatitude();
            curLng = gpsTracker.getLongitude();

            // 기기 gps on/off 확인
            LocationManager lm = (LocationManager) mainActivity.getSystemService(Context.LOCATION_SERVICE);
            try {
                gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
            } catch (Exception e) {
            }
            Log.d("로그:LocationHandler 진입", "curLat=" + curLat + "&curLng" + curLng + "&gps_enabled=" + gps_enabled);

            if (gps_enabled) {
                if (curLng == 0.0) {
                    sendEmptyMessageDelayed(0, 1000);
                } else {
                    removeMessages(0);
                    gpsCount++;
                    //String androidId = Settings.Secure.getString(mainActivity.getContentResolver(), Settings.Secure.ANDROID_ID);
                    mainActivity.webView.loadUrl("javascript:getMyLocation('" + curLat + "', '" + curLng + "')");
                    Log.d("로그:javascript:getMyLocation", "1");
                }
            } else {
                removeMessages(0);
                //mainActivity.webView.loadUrl("javascript:savsSession('gps_enabled', false)");
            }

        }
    }
}
