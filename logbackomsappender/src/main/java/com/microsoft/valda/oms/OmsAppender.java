package com.microsoft.valda.oms;

import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.classic.spi.ThrowableProxyUtil;
import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.core.status.ErrorStatus;
import org.apache.commons.codec.binary.Base64;
import org.json.simple.JSONObject;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.HttpsURLConnection;
import javax.xml.bind.DatatypeConverter;
import javax.xml.ws.http.HTTPException;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Created by vazvadsk on 2017-04-22.
 *
 Copyright(c) 2017 Microsoft Corporation
 All rights reserved.

 MIT License
 Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files(the "Software"),
 to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense,
 and / or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions :

 The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED *AS IS*, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
 IN THE SOFTWARE.

 */
public class OmsAppender  extends AppenderBase<LoggingEvent> {

    private static InetAddress inetAddress = null;
    private static OmsAppender instance;
    private static final BlockingQueue<LoggingEvent> loggingEventQueue = new LinkedBlockingQueue<LoggingEvent>();

    private String customerId = null;
    private String sharedKey = null;
    private String logType = null;

    static {
        try {
            inetAddress = InetAddress.getLocalHost();
        }
        catch (UnknownHostException e) {
            inetAddress = null;
        }

        Thread thread = new Thread(new Runnable() {
            public void run() {
                processQueue();
            }
        });

        thread.setDaemon(true);
        thread.start();
    }

    private static void processQueue() {
        while (true) {
            try {
                LoggingEvent event = loggingEventQueue.poll(1L, TimeUnit.SECONDS);
                if (event != null) {
                    instance.processEvent(event);
                }
            }
            catch (InterruptedException e) {
                // No operations.
            }
        }
    }

    public OmsAppender() {
        super();
        instance = this;
    }
    protected void append(LoggingEvent loggingEvent) {
        loggingEventQueue.add(loggingEvent);
    }

    private void processEvent(LoggingEvent event) {
        try {
            sendLog(event);
        }
        catch (Exception e) {
            addStatus(new ErrorStatus("Failed to process", this, e));
        }
    }

    @Override
    public void stop() {
        super.stop();
    }

    private void sendLog(LoggingEvent event) throws NoSuchAlgorithmException, InvalidKeyException, IOException, HTTPException {
        //create JSON message
        JSONObject obj = new JSONObject();
        obj.put("LOGBACKLoggerName", event.getLoggerName());
        obj.put("LOGBACKLogLevel", event.getLevel().toString());
        obj.put("LOGBACKMessage", event.getFormattedMessage());
        obj.put("LOGBACKThread", event.getThreadName());
        if (event.getCallerData()!=null && event.getCallerData().length>0) {
            obj.put("LOGBACKCallerData", event.getCallerData()[0].toString());
        }
        else {
            obj.put("LOGBACKCallerData", "");
        }
        if (event.getThrowableProxy()!=null) {
            obj.put("LOGBACKStackTrace", ThrowableProxyUtil.asString(event.getThrowableProxy()));
        }
        else {
            obj.put("LOGBACKStackTrace", "");
        }
        if (inetAddress != null) {
            obj.put("LOGBACKIPAddress", inetAddress.getHostAddress());
        }
        else {
            obj.put("LOGBACKIPAddress", "0.0.0.0");
        }
        String json = obj.toJSONString();

        String Signature = "";
        String encodedHash = "";
        String url = "";

        // Todays date input for OMS Log Analytics
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        String timeNow = dateFormat.format(calendar.getTime());

        // String for signing the key
        String stringToSign="POST\n" + json.length() + "\napplication/json\nx-ms-date:"+timeNow+"\n/api/logs";
        byte[] decodedBytes = Base64.decodeBase64(sharedKey);
        Mac hasher = Mac.getInstance("HmacSHA256");
        hasher.init(new SecretKeySpec(decodedBytes, "HmacSHA256"));
        byte[] hash = hasher.doFinal(stringToSign.getBytes());

        encodedHash = DatatypeConverter.printBase64Binary(hash);
        Signature = "SharedKey " + customerId + ":" + encodedHash;

        url = "https://" + customerId + ".ods.opinsights.azure.com/api/logs?api-version=2016-04-01";
        URL objUrl = new URL(url);
        HttpsURLConnection con = (HttpsURLConnection) objUrl.openConnection();
        con.setDoOutput(true);
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/json");
        con.setRequestProperty("Log-Type",logType);
        con.setRequestProperty("x-ms-date", timeNow);
        con.setRequestProperty("Authorization", Signature);

        DataOutputStream wr = new DataOutputStream(con.getOutputStream());
        wr.writeBytes(json);
        wr.flush();
        wr.close();

        int responseCode = con.getResponseCode();
        if(responseCode != 200){
            throw new HTTPException(responseCode);
        }
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getSharedKey() {
        return sharedKey;
    }

    public void setSharedKey(String sharedKey) {
        this.sharedKey = sharedKey;
    }

    public String getLogType() {
        return logType;
    }

    public void setLogType(String logType) {
        this.logType = logType;
    }
}
