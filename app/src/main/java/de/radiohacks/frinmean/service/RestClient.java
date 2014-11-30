package de.radiohacks.frinmean.service;

import android.content.Context;
import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

//import java.net.URL;

/**
 * Created by thomas on 24.08.14.
 */
public class RestClient {

    private static final String TAG = RestClient.class.getSimpleName();
    private ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
    private ArrayList<NameValuePair> headers = new ArrayList<NameValuePair>();
    private String url;
    private int responseCode;
    private String message;
    private String responseXML;
    private String SaveDirectory;
    private String filename;
    private Context mContext;

    public RestClient(String urlin) {
        this.url = urlin;
    }

    private static String convertStreamToString(InputStream is) {
        Log.d(TAG, "start convertStreamToString");

        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Log.d(TAG, "end convertStreamToString");
        return sb.toString();
    }

    public void setContext(Context in) {
        this.mContext = in;
    }

    public Context getContext() {
        return mContext;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getSaveDirectory() {
        return SaveDirectory;
    }

    public void setSaveDirectory(String in) {
        this.SaveDirectory = in;
    }

    public String getResponseXML() {
        return responseXML;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String in) {
        this.filename = in;
    }

    public String getErrorMessage() {
        return message;
    }

    public int getResponseCode() {
        return responseCode;
    }

    public void AddParam(String name, String value) {
        params.add(new BasicNameValuePair(name, value));
    }

    public void AddHeader(String name, String value) {
        Log.d(TAG, "start AddHeader");
        headers.add(new BasicNameValuePair(name, value));
        Log.d(TAG, "end AddHeader");
    }

    public HttpGet BevorExecuteGetQuery() throws Exception {
        Log.d(TAG, "start BevoreExecuteGetQuery");
        //add parameters
        String combinedParams = "";
        if (!params.isEmpty()) {
            combinedParams += "?";
            for (NameValuePair p : params) {
                String paramString = p.getName() + "=" + URLEncoder.encode(p.getValue(), "UTF-8");
                if (combinedParams.length() > 1) {
                    combinedParams += "&" + paramString;
                } else {
                    combinedParams += paramString;
                }
            }
        }

        HttpGet request = new HttpGet(url + combinedParams);

        //add headers
        for (NameValuePair h : headers) {
            request.addHeader(h.getName(), h.getValue());
        }
        Log.d(TAG, "end BevorExecuteGetQuery");
        return request;
    }

    public HttpGet BevorExecuteGetPath(String uid, String pw, int id) throws Exception {
        Log.d(TAG, "start BevorExecuteGetPath");
        //add parameters
        String combinedParams = "";

        if (url.endsWith("/")) {
            combinedParams += URLEncoder.encode(String.valueOf(uid), "UTF-8") + "/" + URLEncoder.encode(String.valueOf(pw), "UTF-8") + "/" + URLEncoder.encode(String.valueOf(id), "UTF-8");
        } else {
            combinedParams += "/" + URLEncoder.encode(String.valueOf(uid), "UTF-8") + "/" + URLEncoder.encode(String.valueOf(pw), "UTF-8") + "/" + URLEncoder.encode(String.valueOf(id), "UTF-8");
        }

        HttpGet request = new HttpGet(url + combinedParams);

        //add headers
        for (NameValuePair h : headers) {
            request.addHeader(h.getName(), h.getValue());
        }
        Log.d(TAG, "end BevorExecuteGetPath");
        return request;
    }


    public HttpPost BevorExecutePost() throws Exception {
        Log.d(TAG, "start BevorExecutePost");
        String combinedParams = "";
        if (!params.isEmpty()) {
            combinedParams += "?";
            for (NameValuePair p : params) {
                String paramString = p.getName() + "=" + URLEncoder.encode(p.getValue(), "UTF-8");
                if (combinedParams.length() > 1) {
                    combinedParams += "&" + paramString;
                } else {
                    combinedParams += paramString;
                }
            }
        }
        HttpPost request = new HttpPost(url + combinedParams);

        //add headers
        for (NameValuePair h : headers) {
            request.addHeader(h.getName(), h.getValue());
        }

        /* if (!params.isEmpty()) {
            request.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
        }

        if (reqEntity != null) {
            request.setEntity(reqEntity);
        } */
        Log.d(TAG, "start BevorExecutePost");
        return request;
    }


    public String ExecuteRequestUploadXML(HttpPost... httpposts) {
        Log.d(TAG, "start ExecuteRequestUploadXML");

        HttpClient client = new DefaultHttpClient();

        HttpResponse response;

        try {
            HttpPost httppost = httpposts[0];

            File f = new File(getFilename());

            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
            builder.addBinaryBody("file", f, ContentType.MULTIPART_FORM_DATA, f.getName());
            HttpEntity multipart = builder.build();

            httppost.setEntity(multipart);

            response = client.execute(httppost);
            responseCode = response.getStatusLine().getStatusCode();
            HttpEntity entity = response.getEntity();

            if (entity != null) {

                InputStream instream = entity.getContent();
                responseXML = convertStreamToString(instream);

                // Closing the input stream will trigger connection release
                instream.close();
            }
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "end ExecuteRequestUploadXML ");
        return responseXML;
    }


    public String ExecuteRequestXML(HttpUriRequest... httpUriRequests) {
        Log.d(TAG, "start ExecuteRequestXML");
        HttpClient client = new DefaultHttpClient();

        HttpResponse httpResponse;

        try {
            httpResponse = client.execute(httpUriRequests[0]);
            responseCode = httpResponse.getStatusLine().getStatusCode();
            message = httpResponse.getStatusLine().getReasonPhrase();

            HttpEntity entity = httpResponse.getEntity();

            if (entity != null) {

                InputStream instream = entity.getContent();
                responseXML = convertStreamToString(instream);

                // Closing the input stream will trigger connection release
                instream.close();
            }

        } catch (ClientProtocolException e) {
            client.getConnectionManager().shutdown();
            e.printStackTrace();
        } catch (IOException e) {
            client.getConnectionManager().shutdown();
            e.printStackTrace();
        }
        Log.d(TAG, "end ExecuteRequestXML");
        return responseXML;
    }

    public String ExecuteRequestImage(HttpUriRequest... httpUriRequests) {
        Log.d(TAG, "start ExecuteRequestImage");
        String ret = null;
        HttpClient httpClient = new DefaultHttpClient();
        try {
            HttpResponse httpResponse = null;
            httpResponse = httpClient.execute(httpUriRequests[0]);
            responseCode = httpResponse.getStatusLine().getStatusCode();

            if (responseCode == 200) {
                message = httpResponse.getStatusLine().getReasonPhrase();
                filename = httpResponse.getFirstHeader("filename").getValue();
                File file = new File(SaveDirectory + filename);
                HttpEntity entity = httpResponse.getEntity();

                InputStream instream1 = entity.getContent();

                OutputStream output = new FileOutputStream(SaveDirectory + filename);

                int read = 0;

                byte[] bytes = new byte[1024];
                while ((read = instream1.read(bytes)) != -1) {
                    output.write(bytes, 0, read);
                }
                output.close();
                instream1.close();
                ret = filename;
            } else {
                // TODO Log Eintrag schreiben
            }
        } catch (ClientProtocolException e) {
            httpClient.getConnectionManager().shutdown();
            e.printStackTrace();
        } catch (IOException e) {
            httpClient.getConnectionManager().shutdown();
            e.printStackTrace();
        }
        Log.d(TAG, "start ExecuteRequestImage");
        return ret;
    }

    public enum RequestMethod {
        GET, POST
    }
}