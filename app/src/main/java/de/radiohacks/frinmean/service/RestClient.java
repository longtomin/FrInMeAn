package de.radiohacks.frinmean.service;

import android.util.Base64;
import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
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
import java.net.URL;
import java.net.URLEncoder;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import de.radiohacks.frinmean.FrinmeanApplication;
import de.radiohacks.frinmean.myssl.CustomSSLSocketFactory;
import de.radiohacks.frinmean.myssl.CustomTrustManager;

/**
 * Created by thomas on 24.08.14.
 */
public class RestClient {

    private static final String TAG = RestClient.class.getSimpleName();
    private static final String HEADER_ACCEPT_ENCODING = "Accept-Encoding";
    private static final String ENCODING_GZIP = "gzip";
    private ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
    private ArrayList<NameValuePair> headers = new ArrayList<NameValuePair>();
    private String url;
    private int responseCode;
    private String message;
    private String responseXML;
    private String SaveDirectory;
    private String filename;
    private boolean usehttps;
    private int port;

    public RestClient(String urlin, boolean inhttps, int inport) {
        this.url = urlin;
        this.usehttps = inhttps;
        this.port = inport;
        headers.add(new BasicNameValuePair(HEADER_ACCEPT_ENCODING, ENCODING_GZIP));

    }

    private static String convertStreamToString(InputStream is) {
        Log.d(TAG, "start convertStreamToString");

        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
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

    public void setSaveDirectory(String in) {
        this.SaveDirectory = in;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String in) {
        this.filename = in;
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

    public HttpDelete BevorExecuteDeleteQuery() throws Exception {
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

        HttpDelete request = new HttpDelete(url + combinedParams);

        //add headers
        for (NameValuePair h : headers) {
            request.addHeader(h.getName(), h.getValue());
        }
        Log.d(TAG, "end BevorExecuteGetQuery");
        return request;
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

        byte[] datauser;
        datauser = uid.getBytes("UTF-8");
        String b64uid = Base64.encodeToString(datauser, Base64.NO_WRAP);
        datauser = pw.getBytes("UTF-8");
        String b64pw = Base64.encodeToString(datauser, Base64.NO_WRAP);

        if (url.endsWith("/")) {
            combinedParams += b64uid + "/" + b64pw + "/" + URLEncoder.encode(String.valueOf(id), "UTF-8");
        } else {
            combinedParams += "/" + b64uid + "/" + b64pw + "/" + URLEncoder.encode(String.valueOf(id), "UTF-8");
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

        Log.d(TAG, "start BevorExecutePost");
        return request;
    }


    public String ExecuteRequestUploadXML(HttpPost... httpposts) throws ClientProtocolException {
        Log.d(TAG, "start ExecuteRequestUploadXML");

        HttpClient client = null;

        if (usehttps) {
            client = getSSLClient();
        } else {
            client = new DefaultHttpClient();
        }

        HttpResponse response;

        try {
            HttpPost httppost = httpposts[0];

            File f = new File(getFilename());
            if (f.exists()) {

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
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "end ExecuteRequestUploadXML ");
        return responseXML;
    }


    public String ExecuteRequestXML(HttpUriRequest... httpUriRequests) {
        Log.d(TAG, "start ExecuteRequestXML");

        HttpClient client = null;

        if (usehttps) {
            client = getSSLClient();
        } else {
            client = new DefaultHttpClient();
        }

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
        HttpClient client = null;

        if (usehttps) {
            client = getSSLClient();
        } else {
            client = new DefaultHttpClient();
        }

        try {
            HttpResponse httpResponse;
            httpResponse = client.execute(httpUriRequests[0]);
            responseCode = httpResponse.getStatusLine().getStatusCode();

            if (responseCode == 200) {
                message = httpResponse.getStatusLine().getReasonPhrase();
                filename = httpResponse.getFirstHeader("filename").getValue();
                HttpEntity entity = httpResponse.getEntity();

                InputStream instream1 = entity.getContent();

                OutputStream output = new FileOutputStream(SaveDirectory + filename);

                int read;

                byte[] bytes = new byte[1024];
                while ((read = instream1.read(bytes)) != -1) {
                    output.write(bytes, 0, read);
                }
                output.close();
                instream1.close();
                ret = filename;
            } else {
                Log.d(TAG, "HTTP Responsecode = " + String.valueOf(responseCode));
            }
        } catch (IOException e) {
            client.getConnectionManager().shutdown();
            e.printStackTrace();
        }
        Log.d(TAG, "start ExecuteRequestImage");
        return ret;
    }

    private HttpClient getSSLClient() {
        HttpClient client = null;
        SSLContext ctx;

        try {
            // Load CAs from an InputStream
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            InputStream caInput = FrinmeanApplication.loadCertAsInputStream();
            Certificate ca;
            try {
                ca = cf.generateCertificate(caInput);
                System.out.println("ca=" + ((X509Certificate) ca).getSubjectDN());
            } finally {
                caInput.close();
            }

            // Create a KeyStore containing our trusted CAs
            String keyStoreType = KeyStore.getDefaultType();
            KeyStore keyStore = KeyStore.getInstance(keyStoreType);
            keyStore.load(null, null);
            keyStore.setCertificateEntry("ca", ca);

            // Create a TrustManager that trusts the CAs in our KeyStore
            String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);

            tmf.init(keyStore);

            // Create an SSLContext that uses our TrustManager
            ctx = SSLContext.getInstance("TLS");
            ctx.init(null, tmf.getTrustManagers(), null);

            HttpClient tmpclient = new DefaultHttpClient();

            SSLSocketFactory ssf = new CustomSSLSocketFactory(ctx);
            ssf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
            ClientConnectionManager ccm = tmpclient.getConnectionManager();
            SchemeRegistry sr = ccm.getSchemeRegistry();
            sr.register(new Scheme("https", ssf, port));
            client = new DefaultHttpClient(ccm,
                    tmpclient.getParams());

        } catch (NoSuchAlgorithmException | KeyManagementException | UnrecoverableKeyException | KeyStoreException | CertificateException | IOException e) {
            e.printStackTrace();
        }

        return client;
    }

    public String BuildURLString() throws Exception {
        Log.d(TAG, "start BuildURLString");
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
        return this.url + combinedParams;
    }

    public String testDirect() {

        try {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            InputStream caInput = FrinmeanApplication.loadCertAsInputStream();
            // InputStream caInput = new BufferedInputStream(new FileInputStream("load-der.crt"));
            Certificate ca;
            try {
                ca = cf.generateCertificate(caInput);
                System.out.println("ca=" + ((X509Certificate) ca).getSubjectDN());
            } finally {
                caInput.close();
            }

// Create a KeyStore containing our trusted CAs
            String keyStoreType = KeyStore.getDefaultType();
            KeyStore keyStore = KeyStore.getInstance(keyStoreType);
            keyStore.load(null, null);
            keyStore.setCertificateEntry("ca", ca);

// Create a TrustManager that trusts the CAs in our KeyStore
            String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
            tmf.init(keyStore);

            CustomTrustManager ctm = new CustomTrustManager(keyStore);

// Create an SSLContext that uses our TrustManager
            SSLContext context = SSLContext.getInstance("TLS");
            // context.init(null, tmf.getTrustManagers(), null);
            context.init(null, new TrustManager[]{ctm}, null);

// Tell the URLConnection to use a SocketFactory from our SSLContext
            URL url = new URL(BuildURLString());
            HttpsURLConnection urlConnection =
                    (HttpsURLConnection) url.openConnection();
            urlConnection.setSSLSocketFactory(context.getSocketFactory());
            InputStream in = urlConnection.getInputStream();

            responseXML = convertStreamToString(in);

        } catch (NoSuchAlgorithmException | KeyManagementException | KeyStoreException | CertificateException | IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return responseXML;
    }
}