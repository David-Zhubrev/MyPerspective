package com.appdav.myperspective.common.decorators;

import android.content.Context;

import com.appdav.myperspective.common.services.DateFormatter.Formatter;
import com.appdav.myperspective.R;
import com.appdav.myperspective.common.data.EventData;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;
import com.prolificinteractive.materialcalendarview.spans.DotSpan;

import java.util.ArrayList;
import java.util.HashSet;


public class EventDecorator implements DayViewDecorator {

    private HashSet<CalendarDay> calendarDays;
    private int color;

    public EventDecorator(Context context, ArrayList<EventData> eventData) {
        calendarDays = convertToCalendarDays(eventData);
        color = context.getResources().getColor(R.color.colorPrimary);
    }

    private HashSet<CalendarDay> convertToCalendarDays(ArrayList<EventData> events) {
        if (events == null) return null;
        HashSet<CalendarDay> result = new HashSet<>();
        for (EventData event : events) {
            Formatter formatter = event.getDateFormatter();
            CalendarDay day = CalendarDay.from(formatter.getYear(), formatter.getMonth(), formatter.getDay());
            result.add(day);
        }
        return result;
    }


    @Override
    public boolean shouldDecorate(CalendarDay day) {
        return (calendarDays != null && calendarDays.contains(day));
    }

    @Override
    public void decorate(DayViewFacade view) {
        view.addSpan(new DotSpan(7, color));
    }
}
