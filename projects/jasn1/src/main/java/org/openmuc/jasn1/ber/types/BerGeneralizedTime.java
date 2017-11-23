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

public class BerGeneralizedTime extends BerVisibleString {

    private static final long serialVersionUID = 1L;

    public final static BerTag tag = new BerTag(BerTag.UNIVERSAL_CLASS, BerTag.PRIMITIVE, BerTag.GENERALIZED_TIME_TAG);

    public BerGeneralizedTime() {
    }

    public BerGeneralizedTime(byte[] value) {
        super(value);
    }

    public BerGeneralizedTime(String valueAsString) {
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
     * Generalized time is one of the following (ITU-T X.680 08/2015): YYYYMMDDHH[MM[SS]][.fff] LocalTime
     * YYYYMMDDHH[MM[SS]][.fff]Z UTC YYYYMMDDHH[MM[SS]][.fff]+-HH[MM] local time with time zone
     * 
     * Regexp: ^ (?<year>\\d{4}) YYYY (?<month>\\d{2}) MM (?<day>\\d{2}) DD (?<hour>\\d{2}) HH ( [MM[SS]]
     * (?<minute>\\d{2}) MM (?<second>\\d{2})? [SS] )? ([.,](?<frac>\\d+))? [.fff] (or [,fff]) (?<timezone> "" or "Z" or
     * "+-HH[MM]" Z | ( "+-HH[MM]" [+-] "+-" \\d{2}(?<tzmin>\\d{2})? HH[MM] ) )? $
     */
    private final static String GENERALIZED_TIME_PATTERN = "^(?<year>\\d{4})(?<month>\\d{2})(?<day>\\d{2})(?<hour>\\d{2})((?<minute>\\d{2})(?<second>\\d{2})?)?([.,](?<frac>\\d+))?(?<timezone>Z|([+-]\\d{2}(?<tzmin>\\d{2})?))?$";

    private final static Pattern generalizedTimePattern = Pattern.compile(GENERALIZED_TIME_PATTERN);

    Calendar asCalendar() throws ParseException {

        Matcher matcher = generalizedTimePattern.matcher(toString());

        if (!matcher.find()) {
            throw new ParseException("", 0);
        }

        String mg, mgf;
        int year = Integer.valueOf(matcher.group("year"));
        int month = Integer.valueOf(matcher.group("month"));
        month -= 1; // java.util.Calendar's month goes from 0 to 11
        int day = Integer.valueOf(matcher.group("day"));
        int hour = Integer.valueOf(matcher.group("hour"));

        mg = matcher.group("minute");
        mgf = matcher.group("frac");
        int minute = 0, second = 0, millisec = 0;
        double frac = mgf == null ? 0 : Double.valueOf("0." + mgf);
        if (mg == null) {
            // Missing minutes and seconds
            if (mgf != null) {
                // frac is a fraction of a hour
                millisec = (int) Math.round(1000 * 60 * 60 * frac);
            }
        }
        else {
            minute = Integer.valueOf(mg);
            mg = matcher.group("second");
            if (mg == null) {
                // Missing seconds
                if (mgf != null) {
                    // frac is a fraction of a minute
                    millisec = (int) Math.round(1000 * 60 * frac);
                }
            }
            else {
                second = Integer.valueOf(mg);
                if (mgf != null) {
                    // frac is a fraction of a second
                    millisec = (int) Math.round(1000 * frac);
                }
            }
        }

        mg = matcher.group("timezone");
        String mgt = matcher.group("tzmin");
        String timeZoneStr = mg == null ? TimeZone.getDefault().getID()
                : (mg.equals("Z") ? "UTC" : (mgt == null ? "GMT" + mg + "00" : "GMT" + mg));
        TimeZone timeZone = TimeZone.getTimeZone(timeZoneStr);

        Calendar calendar = Calendar.getInstance();
        calendar.setLenient(true); // accept millisec greater than 999
        calendar.set(year, month, day, hour, minute, second);
        calendar.set(Calendar.MILLISECOND, millisec);
        calendar.setTimeZone(timeZone);

        return calendar;
    }

    Date asDate() throws ParseException {
        return asCalendar().getTime();
    }

}
