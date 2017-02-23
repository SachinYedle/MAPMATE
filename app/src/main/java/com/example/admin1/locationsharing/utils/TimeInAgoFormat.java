package com.example.admin1.locationsharing.utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;


/**
 * Created by Sachin on 20/2/17.
 */

public class TimeInAgoFormat {
    private static TimeInAgoFormat instance = null;

    public static TimeInAgoFormat getInstance() {
        if (instance == null) {
            instance = new TimeInAgoFormat();
        }
        return instance;
    }

    public String timeInAgoFormat(String dateString) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        formatter.setTimeZone(TimeZone.getTimeZone("GMT+00:00"));
        Date previousDate = null;
        Date currentDate = null;
        try {
            previousDate = formatter.parse(dateString);
            String dateCurrent = formatter.format(new Date());
            formatter.setTimeZone(TimeZone.getTimeZone("GMT"));
            currentDate = formatter.parse(dateCurrent);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return parseDate(currentDate, previousDate);
    }

    public String parseDate(Date currentDate, Date createDate) {

        String result = "just now";
        long difference = Math.abs(currentDate.getTime() - createDate.getTime());

        long secondsInMilli = 1000;
        long minutesInMilli = secondsInMilli * 60;
        long hoursInMilli = minutesInMilli * 60;
        long daysInMilli = hoursInMilli * 24;

        long elapsedDays = difference / daysInMilli;
        difference = difference % daysInMilli;

        long elapsedHours = difference / hoursInMilli;
        difference = difference % hoursInMilli;

        long elapsedMinutes = difference / minutesInMilli;
        difference = difference % minutesInMilli;

        long elapsedSeconds = difference / secondsInMilli;

        if (elapsedDays == 0) {
            if (elapsedHours == 0) {
                if (elapsedMinutes == 0) {
                    if (elapsedSeconds < 0) {
                        return "0" + " secs ago";
                    } else {
                        if (elapsedDays > 0 && elapsedSeconds < 59) {
                            return "now";
                        }
                    }
                } else {
                    return String.valueOf(elapsedMinutes) + " minutes ago";
                }
            } else {
                return String.valueOf(elapsedHours) + " hours ago";
            }
        } else {
            if (elapsedDays <= 29) {
                return String.valueOf(elapsedDays) + " days ago";
            }
            if (elapsedDays > 29 && elapsedDays <= 58) {
                return "1 Month ago";
            }
            if (elapsedDays > 58 && elapsedDays <= 360) {
                return String.valueOf(elapsedDays / 29) + " Months ago";
            }
            if (elapsedDays > 360 && elapsedDays <= 720) {
                return "1 year ago";
            }
            if (elapsedDays > 720) {
                SimpleDateFormat formatterYear = new SimpleDateFormat("dd-MMM-yy hh:mm:ss");
                Calendar calendarYear = Calendar.getInstance();
                calendarYear.setTimeInMillis(createDate.getTime());
                return formatterYear.format(calendarYear.getTime()) + "";
            }
        }
        return result;
    }
}
