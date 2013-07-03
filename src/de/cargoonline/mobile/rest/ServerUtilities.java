package de.cargoonline.mobile.rest;
 
import static de.cargoonline.mobile.push.CommonUtilities.displayMessage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
 
import android.content.Context;
import android.util.Log; 
import com.google.android.gcm.GCMRegistrar;
import de.cargoonline.mobile.R; 
import de.cargoonline.mobile.push.CommonUtilities;
 
public final class ServerUtilities {
    public static final int MAX_ATTEMPTS = 5;
    public static final int BACKOFF_MILLI_SECONDS = 2000;
    public static final Random random = new Random();    

    public static final String TAG = "CO ServerUtilities";
    public static final String EXTRA_MESSAGE = "message";
    public static final String PROPERTY_REG_ID = "regId"; 
    public static final String PROPERTY_USER_NAME = "name"; 
    public static final String PROPERTY_USER_GOOGLEMAIL = "email"; 
    public static final String PROPERTY_USER_PHONE = "phone_no";  
    
    /**
     * Register this account/device pair within the server.
     *
     */
    public static void register(final Context context, String name, String email, String phoneNo, final String regId) {

    	WebExtClient webExt = WebExtClient.getInstance(context);
        
    	Log.i(TAG, "registering device (regId = " + regId + ")");
        Map<String, String> params = new HashMap<String, String>();

        params.put(WebExtClient.KEY_REQUEST, WebExtClient.KEY_REQUEST_REGISTER);
        params.put(PROPERTY_REG_ID, regId);
        params.put(PROPERTY_USER_NAME, name);
        params.put(PROPERTY_USER_GOOGLEMAIL, email);
        params.put(PROPERTY_USER_PHONE, phoneNo);
         
        long backoff = BACKOFF_MILLI_SECONDS + random.nextInt(1000);
        // Once GCM returns a registration id, we need to register on our server
        // As the server might be down, we will retry it a couple
        // times.
        for (int i = 1; i <= MAX_ATTEMPTS; i++) {
            Log.d(TAG, "Attempt #" + i + " to register");
            try {
                displayMessage(context, context.getString(
                        R.string.server_registering, i, MAX_ATTEMPTS));
                post(webExt.getMobileUserRestService(), params); 
                GCMRegistrar.setRegisteredOnServer(context, true);
                String message = context.getString(R.string.server_registered);
                CommonUtilities.displayMessage(context, message);
                return;
            } catch (IOException e) {
                // should retry only on unrecoverable errors (like HTTP error code 503).
                Log.e(TAG, "Failed to register on attempt " + i + ":" + e);
                if (i == MAX_ATTEMPTS) {
                    break;
                }
                try {
                    Log.d(TAG, "Sleeping for " + backoff + " ms before retry");
                    Thread.sleep(backoff);
                } catch (InterruptedException e1) {
                    // Activity finished before we complete - exit.
                    Log.d(TAG, "Thread interrupted: abort remaining retries!");
                    Thread.currentThread().interrupt();
                    return;
                }
                // increase backoff exponentially
                backoff *= 2;
            }
        }
        String message = context.getString(R.string.server_register_error,  MAX_ATTEMPTS);
        Log.e(TAG, message);
        //CommonUtilities.displayMessage(context, message); // would send push notification after registering
    }
 
    /**
     * Unregister this account/device pair within the server.
     */
    public static void unregister(Context context, String regId) {

    	WebExtClient webExt = WebExtClient.getInstance(context);
        String email = WebExtClient.getGoogleAccount(context);
    	
        Log.i(TAG, "unregistering device (regID = " + regId + ", mail = " + email + ")");
        Map<String, String> params = new HashMap<String, String>();

        params.put(WebExtClient.KEY_REQUEST, WebExtClient.KEY_REQUEST_UNREGISTER);
        params.put(PROPERTY_REG_ID, regId);
        params.put(PROPERTY_USER_GOOGLEMAIL, email);
        try {
            post(webExt.getMobileUserRestService(), params);
            GCMRegistrar.setRegisteredOnServer(context, false);
            String message = context.getString(R.string.server_unregistered);
            CommonUtilities.displayMessage(context, message);
        } catch (IOException e) {
            // At this point the device is unregistered from GCM, but still
            // registered in the server.
            // We could try to unregister again, but it is not necessary:
            // if the server tries to send a message to the device, it will get
            // a "NotRegistered" error message and should unregister the device.
            String message = context.getString(R.string.server_unregister_error,
                    e.getMessage());
            CommonUtilities.displayMessage(context, message);
        }
    }
 
    /**
     * Issue a POST request to the server.
     *
     * @param endpoint POST address.
     * @param params request parameters.
     *
     * @throws IOException propagated from POST.
     */
    public static int post(String endpoint, Map<String, String> params)
            throws IOException {    
         
        URL url;
        try {
            url = new URL(endpoint);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("invalid url: " + endpoint);
        }
        String body = buildHttpRequestBody(params);
        Log.v(TAG, "Posting '" + body + "' to " + url);
        byte[] bytes = body.getBytes();
        HttpURLConnection conn = null;
        try {
            Log.e("URL", "> " + url);
            conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setUseCaches(false);
            conn.setFixedLengthStreamingMode(bytes.length);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type",
                    "application/x-www-form-urlencoded;charset=UTF-8");
            // post the request
            OutputStream out = conn.getOutputStream();
            out.write(bytes);
            out.close();
            // handle the response
            int status = conn.getResponseCode();
            if (status != 200) {
              throw new IOException("Post failed with error code " + status);
            }
            return status;
            
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
      }
    
    /**
     * Issue a GET request to the server.
     *
     * @param endpoint GET address.
     * @param params request parameters.
     *
     * @throws IOException propagated from GET.
     */
    public static String get(String endpoint, Map<String, String> params)
            throws IOException {    
         
        URL url;
        try {
            url = new URL(endpoint);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("invalid url: " + endpoint);
        }
       
        String body = buildHttpRequestBody(params);
        Log.v(TAG, "GET: '" + body + "' from " + url);
        //byte[] bytes = body.getBytes();
        
        HttpClient httpclient = new DefaultHttpClient(); 
        
        HttpGet httpget = new HttpGet(url + "?" + body);  
        HttpResponse response;
        InputStream instream = null;
    	String result = null;
        
        try {
            response = httpclient.execute(httpget); 
            Log.i(TAG,response.getStatusLine().toString());
            
            HttpEntity entity = response.getEntity();  
            if (entity != null) {            	 
                instream = entity.getContent();
                result = convertStreamToString(instream);   
            }
        } catch (ClientProtocolException e) { 
            throw new IOException("Client Protocol Error");
        } 
        finally {
            if (instream != null) {
            	instream.close();
            }
        }
		return result;  
      }
    
    private static String buildHttpRequestBody(Map<String,String> params) {
    	StringBuilder bodyBuilder = new StringBuilder();
        Iterator<Entry<String, String>> iterator = params.entrySet().iterator();
        // constructs the POST body using the parameters
        while (iterator.hasNext()) {
            Entry<String, String> param = iterator.next();
            bodyBuilder.append(param.getKey()).append('=')
                    .append(param.getValue());
            if (iterator.hasNext()) {
                bodyBuilder.append('&');
            }
        }
        return bodyBuilder.toString();        
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
        String s = sb.toString();
        return s;
    } 
}