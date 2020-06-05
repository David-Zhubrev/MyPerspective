package com.appdav.myperspective.common.decorators;

import android.content.Context;

import com.appdav.myperspective.common.services.DateFormatter;
import com.appdav.myperspective.R;
import com.appdav.myperspective.common.data.EventData;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;
import com.prolificinteractive.materialcalendarview.spans.DotSpan;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Locale;

public class BirthdayDecorator implements DayViewDecorator {

    private HashSet<CalendarDay> calendarDays;
    private final int CURRENT_YEAR = Calendar.getInstance(Locale.getDefault()).get(Calendar.YEAR);
    private int color;

    public BirthdayDecorator(Context context, ArrayList<EventData> birthdays) {
        this.calendarDays = convertBirthdaysToCalendarDay(birthdays);
        color = context.getResources().getColor(R.color.colorBirthdayDecorator);
    }

    private HashSet<CalendarDay> convertBirthdaysToCalendarDay(ArrayList<EventData> birthdayData) {
        HashSet<CalendarDay> result = new HashSet<>();
        for (EventData data : birthdayData) {
            DateFormatter.Formatter formatter = data.getDateFormatter();
            CalendarDay day = CalendarDay.from(CURRENT_YEAR, formatter.getMonth(), formatter.getDay());
            result.add(day);
        }
        return result;
    }

    @Override
    public boolean shouldDecorate(CalendarDay day) {
        return calendarDays.contains(day);
    }

    @Override
    public void decorate(DayViewFacade view) {
        view.addSpan(new DotSpan(7, color));
    }
}
