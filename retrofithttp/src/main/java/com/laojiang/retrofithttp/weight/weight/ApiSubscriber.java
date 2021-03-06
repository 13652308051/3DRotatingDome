package com.laojiang.retrofithttp.weight.weight;

import android.app.AlertDialog;

import com.jakewharton.retrofit2.adapter.rxjava2.HttpException;
import com.laojiang.retrofithttp.weight.ui.RJRetrofitHttp;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.net.ConnectException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Locale;


/**
 * 类介绍（必填）：网络异常处理
 * Created by Jiang on 2017/3/9 8:26.
 */
public abstract class ApiSubscriber<T> implements Subscriber<T> {

    //HTTP的状态码
    private static final int HTTP_BAD_REQUEST = 400;
    private static final int HTTP_FORBIDDEN = 403;
    private static final int HTTP_NOT_FOUND = 404;
    private static final int HTTP_TIMEOUT = 408;
    private static final int HTTP_INTERNAL_SERVER_ERROR = 500;
    private static final int HTTP_RESULT_ISNULL = -999;
    //出错提示
    private static final String MSG_NETWORK_ERROR = "网络错误";
    private static final String MSG_NETWORK_CONNECTION_ERROR = "网络连接不可用，请检查或稍后重试";
    private static final String MSG_UNKNOWN_ERROR = "Ops，好像出错了~";
    private static final String MSG_TIME_OUT = "网络请求超时";
    private static final String MSG_SERVER_ERROR = "服务器错误";
    private static final String MSG_NOT_FOUND = "访问的地址不存在";
    private static final String MSG_FORBIDDEN = "服务器拒绝请求";
    private static final String MSG_BAD_REQUEST = "请求参数错误";
    private static final String MSG_RESULT_ISNULL = "请求结果为空";
    private HttpException httpException;

    private Subscription subscription;
    @Override
    public void onSubscribe(Subscription s) {
        this.subscription = s;
        s.request(Long.MAX_VALUE);

    }

    @Override
    public void onNext(T t) {
        onSuceess(t);
    }
    public void cancel(){
        if (subscription!=null){
            subscription.cancel();
        }
    }


    @Override
    public void onError(Throwable e) {

        resolveException(e);

        onFinally();
    }

    @Override
    public void onComplete() {
        onFinally();
    }

    public void onFinally() {
        AlertDialog alertDialog = RJRetrofitHttp.load().getmDownloadDialog();
        if (alertDialog!=null){
            alertDialog.dismiss();
            alertDialog=null;
        }
    }

    private void resolveException(Throwable e) {
        Throwable throwable = e;
        //获取最根源的异常
        while (throwable.getCause() != null) {
            e = throwable;
            throwable = throwable.getCause();
        }

        if (e instanceof ApiException) {
            ApiException e1 = (ApiException) e;
            String msg = ((ApiException) e).getMsg();//msg
            int code = ((ApiException) e).getCode();//code
            if (msg == null || msg.isEmpty()) {
                msg = String.format(Locale.CHINA, "出错了！错误代码：%d", ((ApiException) e).getCode());
            }

            if (code!=1){
                onError(msg,code);
            }else {//result为null的情况
                onError(MSG_RESULT_ISNULL,HTTP_RESULT_ISNULL);
            }

        } else if (e instanceof HttpException) {
            HttpException httpException = (HttpException) e;

            switch (httpException.code()) {
                case HTTP_BAD_REQUEST:
                    onError(MSG_BAD_REQUEST,HTTP_BAD_REQUEST);
                case HTTP_FORBIDDEN:
                    onError(MSG_FORBIDDEN,HTTP_FORBIDDEN);
                case HTTP_NOT_FOUND:
                    onError(MSG_NOT_FOUND,HTTP_NOT_FOUND);
                case HTTP_INTERNAL_SERVER_ERROR:
                    onError(MSG_SERVER_ERROR,HTTP_INTERNAL_SERVER_ERROR);
                    break;
                case HTTP_TIMEOUT:
                    onError(MSG_TIME_OUT,HTTP_TIMEOUT);
                    break;
                default:
                    onError(MSG_NETWORK_ERROR,-1);
                    break;
            }
        } else if (e instanceof SocketTimeoutException) {
            onError(MSG_TIME_OUT,-1);
        } else if (e instanceof ConnectException) {
            onError(MSG_NETWORK_ERROR,-2);
        } else if (e instanceof UnknownHostException) {
            onError(MSG_NETWORK_CONNECTION_ERROR,404);
        } else if (e instanceof SocketException) {
            onError(MSG_SERVER_ERROR,-4);
        } else if (e.getMessage()!=null){
            onError(e.getMessage(),-5);
        }
    }

    protected abstract void onError(String msg,int code);
    protected abstract void onSuceess(T t);

}
