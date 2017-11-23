/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.openmuc.jasn1.ber.types;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openmuc.jasn1.ber.BerTag;
import org.openmuc.jasn1.ber.types.string.BerVisibleString;

public class BerUtcTime extends BerVisibleString {

    private static final long serialVersionUID = 1L;

    public final static BerTag tag = new BerTag(BerTag.UNIVERSAL_CLASS, BerTag.PRIMITIVE, BerTag.UTC_TIME_TAG);

    public BerUtcTime() {
    }

    public BerUtcTime(byte[] value) {
        this.value = value;
    }

    public BerUtcTime(String valueAsString) {
        super(valueAsString);
    }

    @Override
    public int encode(OutputStream os, boolean withTag) throws IOException {

        int codeLength = super.encode(os, false);

        if (withTag) {
            codeLength += tag.encode(os);
        }

        return codeLength;
    }

    @Override
    public int decode(InputStream is, boolean withTag) throws IOException {

        int codeLength = 0;

        if (withTag) {
            codeLength += tag.decodeAndCheck(is);
        }

        codeLength += super.decode(is, false);

        return codeLength;
    }

    /*
     * UTC time is one of the following (ITU-T X.680 08/2015): YYMMDDhhmm[ss]Z YYMMDDhhmm[ss](+|-)hhmm Regexp: ^
     * (?<year>\\d{2}) YY (?<month>\\d{2}) MM (?<day>\\d{2}) DD (?<hour>\\d{2}) hh (?<minute>\\d{2}) mm
     * (?<second>\\d{2})? ss (?<timezone> Z | Z or (+|-)hhmm ( [+-]\\d{4} (+|-)hhmm ) ) $
     */
    private final static String UTC_TIME_PATTERN = "^(?<year>\\d{2})(?<month>\\d{2})(?<day>\\d{2})(?<hour>\\d{2})(?<minute>\\d{2})(?<second>\\d{2})?(?<timezone>Z|([+-]\\d{4}))$";

    private final static Pattern utcTimePattern = Pattern.compile(UTC_TIME_PATTERN);

    @SuppressWarnings("WeakerAccess")
    Calendar asCalendar() throws ParseException {

        Matcher matcher = utcTimePattern.matcher(toString());

        if (!matcher.find())
            throw new ParseException("", 0);

        String mg;
        int year = Integer.valueOf(matcher.group("year"));
        int month = Integer.valueOf(matcher.group("month"));
        month -= 1; // java.util.Calendar's month goes from 0 to 11
        int day = Integer.valueOf(matcher.group("day"));
        int hour = Integer.valueOf(matcher.group("hour"));
        int minute = Integer.valueOf(matcher.group("minute"));
        mg = matcher.group("second");
        int second = mg == null ? 0 : Integer.valueOf(mg);

        mg = matcher.group("timezone");
        String timeZoneStr = mg.equals("Z") ? "UTC" : "GMT" + mg;
        TimeZone timeZone = TimeZone.getTimeZone(timeZoneStr);

        Calendar calendar = Calendar.getInstance(timeZone);

        // Add 2000 to the year
        int century = (calendar.get(Calendar.YEAR) / 100) * 100;
        year += century;

        // noinspection MagicConstant
        calendar.set(year, month, day, hour, minute, second);
        calendar.set(Calendar.MILLISECOND, 0);

        return calendar;
    }

    Date asDate() throws ParseException {
        return asCalendar().getTime();
    }

}
