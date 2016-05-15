package de.radiohacks.frinmean.service;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Base64;
import android.util.Log;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;

import org.apache.http.ParseException;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

import de.radiohacks.frinmean.Constants;
import de.radiohacks.frinmean.FrinmeanApplication;
import de.radiohacks.frinmean.myssl.NoSSLv3SocketFactory;

/**
 * Created by thomas on 24.08.14.
 */
public class RestClient {

    private static final String TAG = RestClient.class.getSimpleName();
    private static final String HEADER_ACCEPT_ENCODING = "Accept-Encoding";
    private static final String ENCODING_GZIP = "gzip";
    private static final String LINE_FEED = "\r\n";
    final String basicAuth;
    //private Multimap<String, String> params = new Multimap<>();
    private ListMultimap<String, String> params = ArrayListMultimap.create();
    private HashMap<String, String> headers = new HashMap<>();
    private String url;
    private int responseCode;
    private String message;
    private String responseXML;
    private String SaveDirectory;
    private String filename;
    private String boundary;
    private String putContent;
    private String username;
    private String password;

    public RestClient(String urlin) {
        System.setProperty("http.keepAlive", "false");
        this.url = urlin;
        headers.put(HEADER_ACCEPT_ENCODING, ENCODING_GZIP);
        headers.put("User-Agent", Constants.USER_AGENT);
        getPreferenceInfo();
        if (this.password != "NULL" && this.username != "NULL") {
            String combine = convertB64(username) + ":" + convertB64(password);
            basicAuth = "Basic " + Base64.encodeToString(combine.getBytes(), Base64.NO_WRAP);
        } else {
            basicAuth = "";
        }
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

    protected void getPreferenceInfo() {
        Log.d(TAG, "start getPreferenceInfo");
        SharedPreferences sharedPrefs = PreferenceManager
                .getDefaultSharedPreferences(FrinmeanApplication.getAppContext());

        this.username = sharedPrefs.getString(Constants.PrefUsername, "NULL");
        this.password = sharedPrefs.getString(Constants.PrefPassword, "NULL");
    }

    private String convertB64(String in) {
        byte[] datauser = new byte[0];
        try {
            datauser = in.getBytes(Constants.CHARSET);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return Base64.encodeToString(datauser, Base64.NO_WRAP);
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

    public String getBoundary() {
        return boundary;
    }

    public void setBoundary(String in) {
        this.boundary = in;
    }

    public int getResponseCode() {
        return responseCode;
    }

    public String getPutContent() {
        return this.putContent;
    }

    public void setPutContent(String in) {
        this.putContent = in;
    }

    public void AddParam(String name, String value) {
        params.put(name, value);
    }

    public void AddHeader(String name, String value) {
        Log.d(TAG, "start AddHeader");
        headers.put(name, value);
        Log.d(TAG, "end AddHeader");
    }

    public String ExecuteHTTPXML(String type) {
        HttpURLConnection urlcon = null;
        String combinedParams = "";

        try {
            if (!params.isEmpty()) {
                combinedParams += "?";

                Set keySet = params.keySet();
                Iterator keyIterator = keySet.iterator();
                while (keyIterator.hasNext()) {
                    String key = (String) keyIterator.next();
                    List values = params.get(key);
                    for (Object x : values) {
                        String paramString = key + "=" + URLEncoder.encode(x.toString(), Constants.CHARSET);
                        if (combinedParams.length() > 1) {
                            combinedParams += "&" + paramString;
                        } else {
                            combinedParams += paramString;
                        }
                    }
                }
            }
            URL u = new URL(url + combinedParams);
            urlcon = (HttpURLConnection) u.openConnection();
            if (!headers.isEmpty()) {
                for (HashMap.Entry<String, String> entry : headers.entrySet()) {
                    urlcon.setRequestProperty(entry.getKey(), entry.getValue());
                }
            }
            if (basicAuth != "") {
                urlcon.setRequestProperty("Authorization", basicAuth);
            }
            urlcon.setInstanceFollowRedirects(false);
            urlcon.setRequestMethod(type);
            if (type.equalsIgnoreCase("POST") || type.equalsIgnoreCase("PUT")) {
                urlcon.setDoInput(true);
                urlcon.setRequestMethod(type);
                OutputStreamWriter out = new OutputStreamWriter(
                        urlcon.getOutputStream());
                out.write(putContent);
                out.flush();
                out.close();
            }

            urlcon.setConnectTimeout(60 * 1000);
            urlcon.setReadTimeout(60 * 1000);


            responseCode = urlcon.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                message = urlcon.getResponseMessage();
                InputStream instream = urlcon.getInputStream();
                responseXML = convertStreamToString(instream);
            }


        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            urlcon.disconnect();
        }
        return responseXML;
    }

    public String ExecuteHTTPSXML(String type) {
        HttpsURLConnection urlcon = null;
        String combinedParams = "";

        try {

            // Load CAs from an InputStream
            // (could be from a resource or ByteArrayInputStream or ...)
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            // From https://www.washington.edu/itconnect/security/ca/load-der.crt
            InputStream caInput = FrinmeanApplication.loadCertAsInputStream();
            //InputStream caInput = new BufferedInputStream(new FileInputStream("load-der.crt"));
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
            SSLContext context = SSLContext.getInstance("TLS");
            context.init(null, tmf.getTrustManagers(), null);

            SSLSocketFactory NoSSLv3Factory = new NoSSLv3SocketFactory(context.getSocketFactory());

            HttpsURLConnection.setDefaultSSLSocketFactory(NoSSLv3Factory);

            if (!params.isEmpty()) {
                combinedParams += "?";

                Set keySet = params.keySet();
                Iterator keyIterator = keySet.iterator();
                while (keyIterator.hasNext()) {
                    String key = (String) keyIterator.next();
                    List values = params.get(key);
                    for (Object x : values) {
                        String paramString = key + "=" + URLEncoder.encode(x.toString(), Constants.CHARSET);
                        if (combinedParams.length() > 1) {
                            combinedParams += "&" + paramString;
                        } else {
                            combinedParams += paramString;
                        }
                    }
                }
            }
            URL u = new URL(url + combinedParams);
            urlcon = (HttpsURLConnection) u.openConnection();
            if (!headers.isEmpty()) {
                for (HashMap.Entry<String, String> entry : headers.entrySet()) {
                    urlcon.setRequestProperty(entry.getKey(), entry.getValue());
                }
            }
            //urlcon.setSSLSocketFactory(context.getSocketFactory());
            urlcon.setHostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    HostnameVerifier hv =
                            HttpsURLConnection.getDefaultHostnameVerifier();
                    return hv.verify("frinme.org", session);
                }
            });
            urlcon.setInstanceFollowRedirects(false);
            if (basicAuth != "") {
                urlcon.setRequestProperty("Authorization", basicAuth);
            }
            urlcon.setRequestMethod(type);
            if (type.equalsIgnoreCase("POST") || type.equalsIgnoreCase("PUT")) {
                urlcon.setDoInput(true);
                urlcon.setRequestMethod(type);
                OutputStreamWriter out = new OutputStreamWriter(
                        urlcon.getOutputStream());
                out.write(putContent);
                out.close();
            }
            urlcon.setConnectTimeout(60 * 1000);
            urlcon.setReadTimeout(60 * 1000);
            responseCode = urlcon.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                message = urlcon.getResponseMessage();
                InputStream instream = urlcon.getInputStream();
                responseXML = convertStreamToString(instream);
            }

        } catch (MalformedURLException | CertificateException | NoSuchAlgorithmException | KeyStoreException | KeyManagementException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            urlcon.disconnect();
        }
        return responseXML;
    }

    public String ExecuteHTTPPostXMLMultipart(String post, String filepath, String filefield, String MimeTyp) throws ParseException, IOException {
        HttpURLConnection urlcon = null;
        DataOutputStream outputStream = null;
        InputStream inputStream = null;

        String twoHyphens = "--";
        String boundary = "*****" + Long.toString(System.currentTimeMillis()) + "*****";
        String lineEnd = "\r\n";

        String result = "";

        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int maxBufferSize = 1 * 1024 * 1024;

        String[] q = filepath.split("/");
        int idx = q.length - 1;

        String combinedParams = "";
        if (!params.isEmpty()) {
            combinedParams += "?";

            Set keySet = params.keySet();
            Iterator keyIterator = keySet.iterator();
            while (keyIterator.hasNext()) {
                String key = (String) keyIterator.next();
                List values = params.get(key);
                for (Object x : values) {
                    String paramString = key + "=" + URLEncoder.encode(x.toString(), Constants.CHARSET);
                    if (combinedParams.length() > 1) {
                        combinedParams += "&" + paramString;
                    } else {
                        combinedParams += paramString;
                    }
                }
            }
        }

        try {
            File file = new File(filepath);
            FileInputStream fileInputStream = new FileInputStream(file);

            URL u = new URL(this.url + combinedParams);
            urlcon = (HttpURLConnection) u.openConnection();

            urlcon.setDoInput(true);
            urlcon.setDoOutput(true);
            urlcon.setUseCaches(false);

            if (basicAuth != "") {
                urlcon.setRequestProperty("Authorization", basicAuth);
            }
            urlcon.setRequestMethod("POST");
            urlcon.setChunkedStreamingMode(1024);
            urlcon.setRequestProperty("Connection", "Keep-Alive");
            urlcon.setRequestProperty("User-Agent", Constants.USER_AGENT);
            urlcon.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

            outputStream = new DataOutputStream(urlcon.getOutputStream());
            outputStream.writeBytes(twoHyphens + boundary + lineEnd);
            outputStream.writeBytes("Content-Disposition: form-data; name=\"" + filefield + "\"; filename=\"" + q[idx] + "\"" + lineEnd);
            outputStream.writeBytes("Content-Type: " + MimeTyp + lineEnd);
            outputStream.writeBytes("Content-Transfer-Encoding: binary" + lineEnd);
            outputStream.writeBytes(lineEnd);

            bytesAvailable = fileInputStream.available();
            bufferSize = Math.min(bytesAvailable, maxBufferSize);
            buffer = new byte[bufferSize];

            bytesRead = fileInputStream.read(buffer, 0, bufferSize);
            while (bytesRead > 0) {
                outputStream.write(buffer, 0, bufferSize);
                bytesAvailable = fileInputStream.available();
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);
            }

            outputStream.writeBytes(lineEnd);

            // Upload POST Data
            String[] posts = post.split("&");
            int max = posts.length;
            for (int i = 0; i < max; i++) {
                outputStream.writeBytes(twoHyphens + boundary + lineEnd);
                String[] kv = posts[i].split("=");
                outputStream.writeBytes("Content-Disposition: form-data; name=\"" + kv[0] + "\"" + lineEnd);
                outputStream.writeBytes("Content-Type: text/plain" + lineEnd);
                outputStream.writeBytes(lineEnd);
                outputStream.writeBytes(kv[1]);
                outputStream.writeBytes(lineEnd);
            }

            outputStream.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

            inputStream = urlcon.getInputStream();
            result = convertStreamToString(inputStream);
            responseCode = urlcon.getResponseCode();

            fileInputStream.close();
            inputStream.close();
            outputStream.flush();
            outputStream.close();

            return result;
        } catch (Exception e) {
            Log.e("MultipartRequest", "Multipart Form Upload Error");
            e.printStackTrace();
            return "error";
        } finally {
            urlcon.disconnect();
        }
    }

    public String ExecuteHTTPSPostXMLMultipart(String post, String filepath, String filefield, String MimeTyp) throws ParseException, IOException {
        HttpsURLConnection urlcon = null;
        DataOutputStream outputStream = null;
        InputStream inputStream = null;

        String twoHyphens = "--";
        String boundary = "*****" + Long.toString(System.currentTimeMillis()) + "*****";
        String lineEnd = "\r\n";

        String result = "";

        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int maxBufferSize = 1 * 1024 * 1024;

        String[] q = filepath.split("/");
        int idx = q.length - 1;

        String combinedParams = "";
        if (!params.isEmpty()) {
            combinedParams += "?";

            Set keySet = params.keySet();
            Iterator keyIterator = keySet.iterator();
            while (keyIterator.hasNext()) {
                String key = (String) keyIterator.next();
                List values = params.get(key);
                for (Object x : values) {
                    String paramString = key + "=" + URLEncoder.encode(x.toString(), Constants.CHARSET);
                    if (combinedParams.length() > 1) {
                        combinedParams += "&" + paramString;
                    } else {
                        combinedParams += paramString;
                    }
                }
            }
        }

        try {
            // Load CAs from an InputStream
            // (could be from a resource or ByteArrayInputStream or ...)
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            // From https://www.washington.edu/itconnect/security/ca/load-der.crt
            InputStream caInput = FrinmeanApplication.loadCertAsInputStream();
            //InputStream caInput = new BufferedInputStream(new FileInputStream("load-der.crt"));
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
            SSLContext context = SSLContext.getInstance("TLS");
            context.init(null, tmf.getTrustManagers(), null);

            SSLSocketFactory NoSSLv3Factory = new NoSSLv3SocketFactory(context.getSocketFactory());

            HttpsURLConnection.setDefaultSSLSocketFactory(NoSSLv3Factory);

            File file = new File(filepath);
            FileInputStream fileInputStream = new FileInputStream(file);

            URL u = new URL(this.url + combinedParams);
            urlcon = (HttpsURLConnection) u.openConnection();
            urlcon.setSSLSocketFactory(context.getSocketFactory());
            urlcon.setHostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    HostnameVerifier hv =
                            HttpsURLConnection.getDefaultHostnameVerifier();
                    return hv.verify("frinme.org", session);
                }
            });

            urlcon.setDoInput(true);
            urlcon.setDoOutput(true);
            urlcon.setUseCaches(false);

            if (basicAuth != "") {
                urlcon.setRequestProperty("Authorization", basicAuth);
            }
            urlcon.setRequestMethod("POST");
            urlcon.setChunkedStreamingMode(maxBufferSize);
            urlcon.setRequestProperty("Connection", "Keep-Alive");
            urlcon.setRequestProperty("User-Agent", Constants.USER_AGENT);
            urlcon.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

            outputStream = new DataOutputStream(urlcon.getOutputStream());
            outputStream.writeBytes(twoHyphens + boundary + lineEnd);
            outputStream.writeBytes("Content-Disposition: form-data; name=\"" + filefield + "\"; filename=\"" + q[idx] + "\"" + lineEnd);
            outputStream.writeBytes("Content-Type: " + MimeTyp + lineEnd);
            outputStream.writeBytes("Content-Transfer-Encoding: binary" + lineEnd);
            outputStream.writeBytes(lineEnd);

            bytesAvailable = fileInputStream.available();
            bufferSize = Math.min(bytesAvailable, maxBufferSize);
            buffer = new byte[bufferSize];

            bytesRead = fileInputStream.read(buffer, 0, bufferSize);
            while (bytesRead > 0) {
                outputStream.write(buffer, 0, bufferSize);
                bytesAvailable = fileInputStream.available();
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);
            }

            outputStream.writeBytes(lineEnd);

            // Upload POST Data
            String[] posts = post.split("&");
            int max = posts.length;
            for (int i = 0; i < max; i++) {
                outputStream.writeBytes(twoHyphens + boundary + lineEnd);
                String[] kv = posts[i].split("=");
                outputStream.writeBytes("Content-Disposition: form-data; name=\"" + kv[0] + "\"" + lineEnd);
                outputStream.writeBytes("Content-Type: text/plain" + lineEnd);
                outputStream.writeBytes(lineEnd);
                outputStream.writeBytes(kv[1]);
                outputStream.writeBytes(lineEnd);
            }

            outputStream.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

            inputStream = urlcon.getInputStream();
            result = convertStreamToString(inputStream);
            responseCode = urlcon.getResponseCode();

            fileInputStream.close();
            inputStream.close();
            outputStream.flush();
            outputStream.close();

            return result;
        } catch (Exception e) {
            Log.e("MultipartRequest", "Multipart Form Upload Error");
            e.printStackTrace();
            return "error";
        } finally {
            urlcon.disconnect();
        }
    }

    public String ExecuteHTTPContent(String type) {
        Log.d(TAG, "start ExecuteRequestImage");
        String ret = null;

        HttpURLConnection urlcon = null;
        String combinedParams = "";

        try {

            if (!params.isEmpty()) {
                combinedParams += "?";

                Set keySet = params.keySet();
                Iterator keyIterator = keySet.iterator();
                while (keyIterator.hasNext()) {
                    String key = (String) keyIterator.next();
                    List values = params.get(key);
                    for (Object x : values) {
                        String paramString = key + "=" + URLEncoder.encode(x.toString(), Constants.CHARSET);
                        if (combinedParams.length() > 1) {
                            combinedParams += "&" + paramString;
                        } else {
                            combinedParams += paramString;
                        }
                    }
                }
            }
            URL u = new URL(url + combinedParams);
            urlcon = (HttpURLConnection) u.openConnection();
            urlcon.setInstanceFollowRedirects(false);
            if (basicAuth != "") {
                urlcon.setRequestProperty("Authorization", basicAuth);
            }
            urlcon.setRequestMethod(type);
            if (type.equalsIgnoreCase("POST")) {
                urlcon.setDoOutput(true);
            }
            urlcon.setDoInput(true);
            urlcon.setConnectTimeout(60 * 1000);
            urlcon.setReadTimeout(60 * 1000);
            if (!headers.isEmpty()) {
                for (HashMap.Entry<String, String> entry : headers.entrySet()) {
                    urlcon.setRequestProperty(entry.getKey(), entry.getValue());
                }
            }
            responseCode = urlcon.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                message = urlcon.getResponseMessage();
                filename = urlcon.getHeaderField("filename");
                // httpResponse.getFirstHeader("filename").getValue();
                // HttpEntity entity = httpResponse.getEntity();

                InputStream instream1 = urlcon.getInputStream();

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

        } catch (MalformedURLException | ProtocolException | UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            urlcon.disconnect();
        }
        return ret;
    }

    public String ExecuteHTTPSContent(String type) {
        Log.d(TAG, "start ExecuteRequestImage");
        String ret = null;

        HttpsURLConnection urlcon = null;
        String combinedParams = "";

        try {
            // Load CAs from an InputStream
            // (could be from a resource or ByteArrayInputStream or ...)
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            // From https://www.washington.edu/itconnect/security/ca/load-der.crt
            InputStream caInput = FrinmeanApplication.loadCertAsInputStream();
            //InputStream caInput = new BufferedInputStream(new FileInputStream("load-der.crt"));
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
            SSLContext context = SSLContext.getInstance("TLS");
            context.init(null, tmf.getTrustManagers(), null);

            SSLSocketFactory NoSSLv3Factory = new NoSSLv3SocketFactory(context.getSocketFactory());

            HttpsURLConnection.setDefaultSSLSocketFactory(NoSSLv3Factory);

            if (!params.isEmpty()) {
                combinedParams += "?";

                Set keySet = params.keySet();
                Iterator keyIterator = keySet.iterator();
                while (keyIterator.hasNext()) {
                    String key = (String) keyIterator.next();
                    List values = params.get(key);
                    for (Object x : values) {
                        String paramString = key + "=" + URLEncoder.encode(x.toString(), Constants.CHARSET);
                        if (combinedParams.length() > 1) {
                            combinedParams += "&" + paramString;
                        } else {
                            combinedParams += paramString;
                        }
                    }
                }
            }
            URL u = new URL(url + combinedParams);
            urlcon = (HttpsURLConnection) u.openConnection();
            if (!headers.isEmpty()) {
                for (HashMap.Entry<String, String> entry : headers.entrySet()) {
                    urlcon.setRequestProperty(entry.getKey(), entry.getValue());
                }
            }
            // urlcon.setSSLSocketFactory(context.getSocketFactory());
            urlcon.setHostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    HostnameVerifier hv =
                            HttpsURLConnection.getDefaultHostnameVerifier();
                    return hv.verify("frinme.org", session);
                }
            });

            urlcon.setInstanceFollowRedirects(false);
            if (basicAuth != "") {
                urlcon.setRequestProperty("Authorization", basicAuth);
            }
            urlcon.setRequestMethod(type);
            if (type.equalsIgnoreCase("POST") || type.equalsIgnoreCase("PUT")) {
                urlcon.setRequestMethod(type);
                OutputStreamWriter out = new OutputStreamWriter(
                        urlcon.getOutputStream());
                out.write(putContent);
                out.close();
            }
//            urlcon.setDoInput(true);
            urlcon.setConnectTimeout(60 * 1000);
            urlcon.setReadTimeout(60 * 1000);

            responseCode = urlcon.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                message = urlcon.getResponseMessage();
                filename = urlcon.getHeaderField("filename");
                // httpResponse.getFirstHeader("filename").getValue();
                // HttpEntity entity = httpResponse.getEntity();

                InputStream instream1 = urlcon.getInputStream();

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

        } catch (MalformedURLException | ProtocolException | UnsupportedEncodingException | CertificateException | KeyStoreException | NoSuchAlgorithmException | KeyManagementException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            urlcon.disconnect();
        }
        return ret;
    }
}