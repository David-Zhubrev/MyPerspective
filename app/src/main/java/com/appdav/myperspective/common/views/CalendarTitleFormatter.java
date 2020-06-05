package com.appdav.myperspective.common.views;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.format.TitleFormatter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CalendarTitleFormatter implements TitleFormatter {

    private static CalendarTitleFormatter instance;


    public static CalendarTitleFormatter getInstance() {
        if (instance == null) {
            instance = new CalendarTitleFormatter();
        }
        return instance;
    }

    @Override
    public CharSequence format(CalendarDay day) {
        String calendarDayText = day.toString();
        String format = "CalendarDay{";
        calendarDayText = calendarDayText.substring(format.length(), format.length() + 8);
        Date date;
        try {
            date = new SimpleDateFormat("yyyy-M", Locale.getDefault()).parse(calendarDayText);
        } catch (ParseException e) {
            date = null;
        }
        if (date == null) return "";
        return new SimpleDateFormat("LLLL yyyy", Locale.getDefault()).format(date);
    }
}
