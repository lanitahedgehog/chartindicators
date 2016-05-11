package com.mobiletradingpartners.chartindicatorslib;

import android.os.Handler;
import android.os.Looper;


import org.apache.http.conn.ConnectTimeoutException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


/**
 * Loads json with indicators
 */
public class IndicatorsLoader {

    private static final String LOG_TAG = "IndicatorsLoader";

    private static final MediaType REQUEST_TYPE = MediaType.parse("application/x-www-form-urlencoded; charset=utf-8");
    private static final String EMPTY_RESPONSE = "";
    private static final int WEBSERVICE_CONNECTION_TIMEOUT 	=  10000;
    private static final String SESSION_STRING	= "JSESSIONID";

    private static OkHttpClient httpClient = new OkHttpClient();

    public IndicatorsLoader() {
        httpClient = httpClient.newBuilder().readTimeout(WEBSERVICE_CONNECTION_TIMEOUT, TimeUnit.MILLISECONDS)
                                            .connectTimeout(WEBSERVICE_CONNECTION_TIMEOUT, TimeUnit.MILLISECONDS).build();

    }

    public interface OnDataLoadListener {
        ServerResponse onLoadBackground(String data);
        void onLoad(ServerResponse serverResponse);
        void onError(ResponseCode errorCode);
    }

    public GetDataTask load( String service, OnDataLoadListener listener ) {

        return loadPost( service, listener, null );

    }

    public GetDataTask loadPost(String service, OnDataLoadListener listener, String[] args) {

        GetDataTask ttaw = new GetDataTask(service, listener, join(args, "&"));
        ttaw.start();
        return ttaw;

    }

    private String join(Object[] array, String separator) {
        if(array == null) {
            return null;
        } else {
            if(separator == null) {
                separator = "";
            }

            int startIndex = 0;
            int endIndex = array.length;

            int noOfItems = endIndex - startIndex;
            if(noOfItems <= 0) {
                return "";
            } else {
                StringBuilder buf = new StringBuilder(noOfItems * 16);

                for(int i = startIndex; i < endIndex; ++i) {
                    if(i > startIndex) {
                        buf.append(separator);
                    }

                    if(array[i] != null) {
                        buf.append(array[i]);
                    }
                }

                return buf.toString();
            }
        }
    }

    public class GetDataTask implements Runnable {

        private String service, args;
        private OnDataLoadListener listener;
        private ServerResponse response;
        private boolean isSuccess = true;
        private boolean isCancelled = false;
        private ResponseCode errorCode;  // was by default -1
        private ServerResponse backgroundResult;
        private Thread t = null;
        private Handler handler;

        public GetDataTask(String service, OnDataLoadListener listener, String args) {
            this.service = service;
            this.listener = listener;
            this.args = args;
            this.handler = new Handler(Looper.getMainLooper());
        }

        @Override
        public void run() {

            if (isCancelled || service == null) { //nb can be the case when isCancelled is true here??
                listener.onError(ResponseCode.ERROR_CALL_INTERRUPTED);
                return;
            }

            if (listener == null) return;

            if (isCancelled) {  //nb if isCancelled == true, then we will return from the 1st IF
                isSuccess = false;
                errorCode = ResponseCode.ERROR_CALL_INTERRUPTED;
            }

            if (isSuccess)
                response = makeRequest(service, args);

            if (response == null) return;

            String result = null;
            isSuccess = response.getResponseCode() == ResponseCode.RESULT_OK;
            if (!isSuccess) {
                errorCode = response.getResponseCode();
            }
            else {
                result = response.getJsonResponse();
            }

            if (isSuccess)
                backgroundResult = listener.onLoadBackground(result);

            handler.post(new Runnable() {
                @Override
                public void run() {

                    if (isCancelled) {
                        listener.onError(ResponseCode.ERROR_CALL_INTERRUPTED);
                        return;
                    }
                    if (!isSuccess) {
                        listener.onError(errorCode);
                        return;
                    }

                    if (!isSuccess) {
                        listener.onError(response.getResponseCode());
                        return;
                    }

                    listener.onLoad(backgroundResult);

                }
            });

        }

        public void start() {

            if (isCancelled || service == null) {
                listener.onError(ResponseCode.ERROR_CALL_INTERRUPTED);
                return;
            }

            if (t == null) {
                t = new Thread(this, "Service:" + service);
                t.start();
            }

        }

        public void cancel() {

            isCancelled = true;
            if (t != null) t.interrupt();

        }
    }

    private ServerResponse makeRequest(String url, String content) {

        Request.Builder request;
        try {
            request = new Request.Builder().url(url);
        } catch (Exception ex) {
            return new ServerResponse(ResponseCode.ERROR_CONNECTION_MALFORMEDURL, EMPTY_RESPONSE);
        }

        if (content != null) {
            RequestBody body = RequestBody.create(REQUEST_TYPE, content);
            request.post(body);

        }

        try {

            Response response = httpClient.newCall(request.build()).execute();

            return new ServerResponse(ResponseCode.RESULT_OK, response.body().string());

        } catch (UnknownHostException e) {
            e.printStackTrace();
            Logger.log(Logger.LogMode.ERROR, LOG_TAG, "UnknownHostException");
            return new ServerResponse(ResponseCode.ERROR_CONNECTION_NOTFOUND, EMPTY_RESPONSE);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Logger.log(Logger.LogMode.ERROR, LOG_TAG, "FileNotFoundException");
            return new ServerResponse(ResponseCode.ERROR_CONNECTION_NOTFOUND, EMPTY_RESPONSE);
        } catch (ConnectTimeoutException e) {
            e.printStackTrace();
            return new ServerResponse(ResponseCode.ERROR_CONNECTION_TIMEOUT, EMPTY_RESPONSE);
        } catch (IOException e) {
            e.printStackTrace();
            return new ServerResponse(ResponseCode.ERROR_CONNECTION_IO, EMPTY_RESPONSE);
        } catch (Exception e) {
            e.printStackTrace();
            return new ServerResponse(ResponseCode.ERROR_CONNECTION_UNKNOWN, EMPTY_RESPONSE);
        }
    }

    public class ServerResponse {
        private ResponseCode responseCode;
        private String jsonResponse;

        public ServerResponse(ResponseCode responseCode, String jsonResponse) {
            this.responseCode = responseCode;
            this.jsonResponse = jsonResponse;
        }

        public ResponseCode getResponseCode() {
            return responseCode;
        }

        public void setResponseCode(ResponseCode responseCode) {
            this.responseCode = responseCode;
        }

        public String getJsonResponse() {
            return jsonResponse;
        }

        public void setJsonResponse(String jsonResponse) {
            this.jsonResponse = jsonResponse;
        }
    }

    public enum ResponseCode {
        RESULT_OK,
        ERROR_CONNECTION_NOTFOUND,
        ERROR_CONNECTION_TIMEOUT,
        ERROR_CALL_INTERRUPTED,
        ERROR_CONNECTION_IO,
        ERROR_CONNECTION_UNKNOWN,
        ERROR_CONNECTION_MALFORMEDURL
    }
}
