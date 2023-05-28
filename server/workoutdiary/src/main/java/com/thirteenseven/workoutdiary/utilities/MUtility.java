package com.thirteenseven.workoutdiary.utilities;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/** Contains all kinds of general utility for easy processing data */
@Component
public class MUtility {
    private static final Logger logger = LoggerFactory.getLogger(MUtility.class);

    /** Format a date string "MM/dd/yyyy"
     * @param dateString a string in the format 
     * @return {@link Date} object if successful, else 'null'
    */
    public static Date formatDateStr(String dateString) {
        String dateformat = "MM/dd/yyyy";
        Date res = null;
        try {
            res = new SimpleDateFormat(dateformat).parse(dateString);
        } catch (ParseException e) {
            System.out.println(String.format("[WARN] Date provided not formattable (%s). Default to null.", dateformat));
            System.out.println(e);
        }
        return res;
    }


    /** Format a date time string "yyyy-mm-dd_hh-mm"
     * @param dtString a string in the format 
     * @return {@link Date} object if successful, else 'null'
    */
    public static Date formatDateTimeStr(String dtString) {
        String dateformat = "yyyy-MM-dd_hh-mm";
        Date res = null;
        try {
            res = new SimpleDateFormat(dateformat).parse(dtString);
        } catch (ParseException e) {
            System.out.println(String.format("[WARN] Date provided not formattable (%s). Default to null.", dateformat));
            System.out.println(e);
        }
        return res;
    }


    /** Convert a {@link Date} object to 30 minutes interval */
    public static Date to30MinInterval(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.MINUTE, cal.get(Calendar.MINUTE) <= 29 ? 0 : 30);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        date = cal.getTime();
        return date;
    }


    public static boolean isAllKeysNotNull(Map<String, Object> mp, String ...keys) {
        for (String k : keys) 
            if (mp.get(k) == null) return false;
        return true;
    }
}
