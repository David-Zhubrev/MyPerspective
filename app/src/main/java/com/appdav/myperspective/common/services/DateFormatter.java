package com.appdav.myperspective.common.services;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateFormatter {

    private static Locale locale = Locale.getDefault();
    private final static SimpleDateFormat dayFormat = new SimpleDateFormat("dd", locale);
    private final static SimpleDateFormat monthFormat = new SimpleDateFormat("MM", locale);
    private final static SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy", locale);
    private static DateFormatter instance;

    private DateFormatter() {
    }

    public static DateFormatter getInstance() {
        if (instance == null) {
            instance = new DateFormatter();
        }
        return instance;
    }

    public Formatter getFormatter(long epochMillis) {
        return new Formatter(epochMillis);
    }


    public class Formatter {

        Date date;

        Formatter(long time) {
            date = new Date(time);
        }

        public int getDay() {
            return Integer.parseInt(dayFormat.format(date));
        }

        public int getMonth() {
            return Integer.parseInt(monthFormat.format(date));
        }

        public int getYear() {
            return Integer.parseInt(yearFormat.format(date));
        }
    }

}
