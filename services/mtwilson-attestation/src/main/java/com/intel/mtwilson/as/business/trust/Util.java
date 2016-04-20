package com.intel.mtwilson.as.business.trust;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

public class Util {
    public static XMLGregorianCalendar getCalendar(Date inputDate) {
        try {
            GregorianCalendar cal = new GregorianCalendar();
            cal.setTime(inputDate);
            return DatatypeFactory.newInstance().newXMLGregorianCalendar(cal);

        } catch (Exception e) {
            return null;
        }
    }

    public static String getDateString(Date today) {
        return new SimpleDateFormat("EEE MMM d HH:MM:ss yyyy").format(today);
    }

}
