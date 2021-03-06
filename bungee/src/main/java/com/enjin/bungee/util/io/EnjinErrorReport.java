package com.enjin.bungee.util.io;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class EnjinErrorReport {

    Throwable e                = null;
    String    info             = "";
    String    otherinformation = "";
    long      timethrown       = System.currentTimeMillis();

    public EnjinErrorReport(Throwable e, String otherinformation) {
        this.e = e;
        this.otherinformation = otherinformation;
    }

    public EnjinErrorReport(String data, String otherinformation) {
        info = data;
        this.otherinformation = otherinformation;
    }

    @Override
    public String toString() {
        StringBuilder errorstring = new StringBuilder();
        DateFormat    dateFormat  = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss z");
        Date          date        = new Date(timethrown);
        errorstring.append("Enjin plugin error report. Error generated on: ")
                   .append(dateFormat.format(date))
                   .append(":\n");
        errorstring.append("Extra data: ").append(otherinformation).append("\n");
        if (e != null) {
            errorstring.append("Stack trace:\n");
            errorstring.append(e.toString()).append("\n");
            StackTraceElement[] stacktrace = e.getStackTrace();
            for (int i = 0; i < stacktrace.length; i++) {
                errorstring.append(stacktrace[i].toString()).append("\n");
            }
        } else {
            errorstring.append("More Info:\n");
            errorstring.append(info);
        }
        return errorstring.toString();
    }

}
