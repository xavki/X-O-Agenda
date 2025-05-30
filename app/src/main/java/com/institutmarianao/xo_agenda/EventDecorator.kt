package com.institutmarianao.xo_agenda

import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade
import com.prolificinteractive.materialcalendarview.spans.DotSpan

class EventDecorator(
    private val color: Int,
    private val dates: List<CalendarDay>
) : DayViewDecorator {

    override fun shouldDecorate(day: CalendarDay): Boolean =
        dates.contains(day)

    override fun decorate(view: DayViewFacade) {
        // pinta un puntito de 8px con el color elegido
        view.addSpan(DotSpan(8f, color))
    }
}
