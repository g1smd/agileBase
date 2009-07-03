/*
 *  Copyright 2009 GT webMarque Ltd
 * 
 *  This file is part of GT portalBase.
 *
 *  GT portalBase is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *  GT portalBase is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with GT portalBase.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.gtwm.pb.model.manageData.fields;

import com.gtwm.pb.model.interfaces.fields.DurationValue;

public class DurationValueDefn implements DurationValue {

    /**
     * Constructs a duration of length 0
     */
    public DurationValueDefn() {
        // Leave all values at their defaults of zero
    }

    /**
     * Sets the value of the duration. Parts chain up, so if you set 70 seconds for example, the actual values
     * set will be one minute and 10 seconds. Note: This doesn't always act 100% as expected as certain
     * assumptions are made, e.g. that every month has 31 days
     * Note: You can pass null in for any parameter, this will be interpreted as zero
     */
    public DurationValueDefn(Integer years, Integer months, Integer days, Integer hours, Integer minutes, Integer seconds) {
        addSeconds(seconds);
        addMinutes(minutes);
        addHours(hours);
        addDays(days);
        addMonths(months);
        addYears(years);
    }

    public int getYears() {
        return this.years;
    }

    public int getMonths() {
        return this.months;
    }

    public int getDays() {
        return this.days;
    }

    public int getHours() {
        return this.hours;
    }

    public int getMinutes() {
        return this.minutes;
    }

    public int getSeconds() {
        return this.seconds;
    }

    public String getSqlFormatInterval() {
        StringBuffer sqlFormatInterval = new StringBuffer();
        boolean started = false;
        if (this.years > 0) {
            started = true;
            sqlFormatInterval.append(this.years);
            sqlFormatInterval.append(" years ");
            if (this.months == 0 && this.days == 0 && this.hours == 0 && this.minutes == 0 && this.seconds == 0) {
                return sqlFormatInterval.toString();
            }
        }
        if (started || this.months > 0) {
            started = true;
            sqlFormatInterval.append(this.months);
            sqlFormatInterval.append(" months ");
            if (this.days == 0 && this.hours == 0 && this.minutes == 0 && this.seconds == 0) {
                return sqlFormatInterval.toString();
            }
        }
        if (started || this.days > 0) {
            started = true;
            sqlFormatInterval.append(this.days);
            sqlFormatInterval.append(" days ");
            if (this.hours == 0 && this.minutes == 0 && this.seconds == 0) {
                return sqlFormatInterval.toString();
            }
        }
        if (started || this.hours > 0) {
            started = true;
            sqlFormatInterval.append(this.hours);
            sqlFormatInterval.append(" hours ");
            if (this.minutes == 0 && this.seconds == 0) {
                return sqlFormatInterval.toString();
            }            
        }
        if (started || this.minutes > 0) {
            started = true;
            sqlFormatInterval.append(this.minutes);
            sqlFormatInterval.append(" minutes ");
            if (this.seconds == 0) {
                return sqlFormatInterval.toString();
            }            
        }
        if (this.seconds > 0) {
            sqlFormatInterval.append(this.seconds);
            sqlFormatInterval.append(" seconds ");            
        }
        return sqlFormatInterval.toString();
    }
    
    public String toString() {
        return getSqlFormatInterval();
    }

    public boolean isNull() {
        // The way this class works, values can't be null, they are zero by default
        return false;
    }
    
    private void addSeconds(Integer seconds) {
        if (seconds != null) {
            int totalSeconds = seconds + this.seconds;
            int remainderSeconds = totalSeconds % 60;
            this.seconds = remainderSeconds;
            if (totalSeconds > 60) {
                int minutes = (totalSeconds - remainderSeconds) / 60;
                addMinutes(minutes);
            }
        }
    }

    private void addMinutes(Integer minutes) {
        if (minutes != null) {
            int totalMinutes = minutes + this.minutes;
            int remainderMinutes = totalMinutes % 60;
            this.minutes = remainderMinutes;
            if (totalMinutes > 60) {
                int hours = (totalMinutes - remainderMinutes) / 60;
                addHours(hours);
            }
        }
    }

    private void addHours(Integer hours) {
        if (hours != null) {
            int totalHours = hours + this.hours;
            int remainderHours = totalHours % 24;
            this.hours = remainderHours;
            if (totalHours > 24) {
                int days = (totalHours - remainderHours) / 24;
                addDays(days);
            }
        }
    }

    private void addDays(Integer days) {
        if (days != null) {
            int totalDays = days + this.hours;
            int remainderDays = totalDays % 31;
            this.days = remainderDays;
            if (totalDays > 31) {
                int months = (totalDays - remainderDays) / 31;
                addMonths(months);
            }
        }
    }

    private void addMonths(Integer months) {
        if (months != null) {
            int totalMonths = months + this.months;
            int remainderMonths = totalMonths % 12;
            this.months = remainderMonths;
            if (totalMonths > 12) {
                int years = (totalMonths - remainderMonths) / 12;
                addYears(years);
            }
        }
    }

    private void addYears(Integer years) {
        if (years != null) {
            this.years += years;            
        }
    }

    private int years = 0;

    private int months = 0;

    private int days = 0;

    private int hours = 0;

    private int minutes = 0;

    private int seconds = 0;
}
