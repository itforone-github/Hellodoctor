package ai.hellodoctor;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;
import android.webkit.JavascriptInterface;

class WebviewJavainterface {
    Activity activity;
    ai.hellodoctor.MainActivity mainActivity;

    WebviewJavainterface(ai.hellodoctor.MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    WebviewJavainterface(Activity activity) {
        this.activity = activity;
    }


    // 기기에서 제공하는 공유하기 사용
    @JavascriptInterface
    public void doShare(final String arg1, final String arg2) {
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_SUBJECT, arg1);       // 제목
                shareIntent.putExtra(Intent.EXTRA_TEXT, arg2);          // 내용

                Intent chooser = Intent.createChooser(shareIntent, "공유하기");
                mainActivity.startActivity(chooser);
            }
        });
    }

    // 현재위치 호출
    @JavascriptInterface
    public void callCurrentPosition() {
        mainActivity.webView.post(new Runnable() {
            @Override
            public void run() {
                ClientManager clientManager = new ClientManager(mainActivity);
                clientManager.locationHandler.sendEmptyMessage(0);
                Log.d("로그:현재위치 웹뷰에서 호출", "ㅇㅇ");
            }
        });
    }


    // 로그인데이터저장/삭제
    @JavascriptInterface
    public void updateLoginInfo(final String mb_id) {
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                mainActivity.pEditor.putString("appLoginId", mb_id);
                mainActivity.pEditor.apply();

                Log.d("로그:updateLoginInfo()", mb_id);
            }
        });
    }

    // 안드로이드 로딩이미지호출
    /*@JavascriptInterface
    public void callAppLoading(final String arg) {
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                if (arg.equals("show")) {
                    mainActivity.imgGif.setVisibility(View.VISIBLE);
                } else {
                    mainActivity.imgGif.setVisibility(View.GONE);
                }
            }
        });
    }*/

}
