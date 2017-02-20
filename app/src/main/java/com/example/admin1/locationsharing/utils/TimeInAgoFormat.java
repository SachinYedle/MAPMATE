package com.example.admin1.locationsharing.utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


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
        DateFormat readFormat = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z");
        Date date = null;
        try {
            date = readFormat.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return parseDate(date.getTime() + "");
    }

    public String parseDate(String timeAtMiliseconds) {
        if (timeAtMiliseconds.equalsIgnoreCase("")) {
            return "";
        }
        String result = "now";
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MMM-yy hh:mm:ss");
        String dateCurrent = formatter.format(new Date());
        Calendar calendar = Calendar.getInstance();

        long dayagolong = Long.valueOf(timeAtMiliseconds);
        calendar.setTimeInMillis(dayagolong);
        String agoformater = formatter.format(calendar.getTime());

        Date CurrentDate = null;
        Date CreateDate = null;

        try {
            CurrentDate = formatter.parse(dateCurrent);
            CreateDate = formatter.parse(agoformater);

            long difference = Math.abs(CurrentDate.getTime() - CreateDate.getTime());

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

            difference = difference % secondsInMilli;
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
                    calendarYear.setTimeInMillis(dayagolong);
                    return formatterYear.format(calendarYear.getTime()) + "";
                }
            }

        } catch (java.text.ParseException e) {
            e.printStackTrace();
        }
        return result;
    }
}
