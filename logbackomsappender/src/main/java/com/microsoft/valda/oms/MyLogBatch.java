package com.microsoft.valda.oms;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by vazvadsk on 2017-04-22.
 *
 * Copyright(c) 2017 Microsoft Corporation All rights reserved.
 * 
 * MIT License Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation files(the
 * "Software"), to deal in the Software without restriction, including without
 * limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and / or sell copies of the Software, and to permit persons to
 * whom the Software is furnished to do so, subject to the following conditions
 * :
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED *AS IS*, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 * 
 */
public class MyLogBatch{
    private Date lastTimestamp;
    private String json;

    public Date getLastTimestamp() {
        return lastTimestamp;
    }

    public String getJson() {
        return json;
    }

    public void setJson(String json) {
        this.json = json;
    }

    public void setLastTimestamp(Date lastTimestamp) {
        this.lastTimestamp = lastTimestamp;
    }

    public String getFileLastTimestamp(){
        SimpleDateFormat dateFormatISO = new SimpleDateFormat(
            "yyyyMMddHHmmssSSS", Locale.US);
        dateFormatISO.setTimeZone(TimeZone.getTimeZone("GMT"));
        return dateFormatISO.format(lastTimestamp);
    }

    public String getISOLastTimestamp(){
        SimpleDateFormat dateFormatISO = new SimpleDateFormat(
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
        dateFormatISO.setTimeZone(TimeZone.getTimeZone("GMT"));
        return dateFormatISO.format(lastTimestamp);
    }

    public void setLastTimestampFromFile(String tm){
        SimpleDateFormat dateFormatISO = new SimpleDateFormat(
            "yyyyMMddHHmmssSSS", Locale.US);
        dateFormatISO.setTimeZone(TimeZone.getTimeZone("GMT"));
        try{
            lastTimestamp = dateFormatISO.parse(tm);
        }catch(Exception e){
            lastTimestamp = new Date();
        }
    }
}