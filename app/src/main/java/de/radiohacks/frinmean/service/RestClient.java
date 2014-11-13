package de.radiohacks.frinmean.service;

import android.graphics.Bitmap;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.ByteArrayBuffer;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.util.ArrayList;

//import java.net.URL;

/**
 * Created by thomas on 24.08.14.
 */
public class RestClient {

    public MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
    private ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
    private ArrayList<NameValuePair> headers = new ArrayList<NameValuePair>();
    private String url;
    private int responseCode;
    private String message;
    private String responseXML;
    private String SaveDirectory;
    private Bitmap responseImage;
    private String filename;
    private HttpUriRequest request;

    public RestClient(String urlin) {
        this.url = urlin;
    }

    private static String convertStreamToString(InputStream is) {

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
        return sb.toString();
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

    public Bitmap getResponseImage() {
        return responseImage;
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
        headers.add(new BasicNameValuePair(name, value));
    }

    public HttpGet BevorExecuteGet() throws Exception {
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

        return request;

    }

    public HttpPost BevorExecutePost() throws Exception {
        HttpPost request = new HttpPost(url);

        //add headers
        for (NameValuePair h : headers) {
            request.addHeader(h.getName(), h.getValue());
        }

        if (!params.isEmpty()) {
            request.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
        }

        if (reqEntity != null) {
            request.setEntity(reqEntity);
        }

        return request;
    }

    public String ExecuteRequestXML(HttpUriRequest... httpUriRequests) {

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
        return responseXML;
    }

    public Bitmap ExecuteRequestImage(HttpUriRequest... httpUriRequests) {

 /*       try {
            HttpClient httpClient = new DefaultHttpClient();
            HttpResponse httpResponse = null;
            httpResponse = httpClient.execute(httpUriRequests[0]);
            StatusLine statusLine = httpResponse.getStatusLine();
            int statusCode = statusLine.getStatusCode();
            if (statusCode == 200) {
                filename = httpResponse.getFirstHeader("filename").getValue();
                HttpEntity entity = httpResponse.getEntity();
                byte[] bytes = EntityUtils.toByteArray(entity);

                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0,
                        bytes.length);
                return bitmap;
            } else {
                throw new IOException("Download failed, HTTP response code "
                        + statusCode + " - " + statusLine.getReasonPhrase());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null; */

        HttpClient httpClient = new DefaultHttpClient();
        try {
            HttpResponse httpResponse = null;
            httpResponse = httpClient.execute(httpUriRequests[0]);
            responseCode = httpResponse.getStatusLine().getStatusCode();
            message = httpResponse.getStatusLine().getReasonPhrase();
            filename = httpResponse.getFirstHeader("filename").getValue();

            File file = new File(SaveDirectory + filename);
            HttpEntity entity = httpResponse.getEntity();

            if (entity != null) {
                // BufferedHttpEntity bufHttpEntity = new BufferedHttpEntity(entity);

                BufferedInputStream bis = new BufferedInputStream(httpResponse.getEntity().getContent());

                ByteArrayBuffer baf = new ByteArrayBuffer(50);
                int current = 0;
                while ((current = bis.read()) != -1) {
                    baf.append((byte) current);
                }

                FileOutputStream fos = new FileOutputStream(file);
                fos.write(baf.toByteArray());
                fos.close();


                // Bitmap s = BitmapFactory.decodeStream(instream, null, null);

                //responseImage = BitmapFactory.decodeStream(instream);

                // Closing the input stream will trigger connection release
                bis.close();


            }
        } catch (ClientProtocolException e) {
            httpClient.getConnectionManager().shutdown();
            e.printStackTrace();
        } catch (IOException e) {
            httpClient.getConnectionManager().shutdown();
            e.printStackTrace();
        }
        return responseImage;
    }

    public enum RequestMethod {
        GET, POST
    }
}