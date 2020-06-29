/*
 * Copyright 2012 The ASN1bean Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.beanit.asn1bean.ber.types;

import com.beanit.asn1bean.ber.BerTag;
import com.beanit.asn1bean.ber.types.string.BerVisibleString;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BerUtcTime extends BerVisibleString {

  public static final BerTag tag =
      new BerTag(BerTag.UNIVERSAL_CLASS, BerTag.PRIMITIVE, BerTag.UTC_TIME_TAG);
  private static final long serialVersionUID = 1L;

  /**
   * UTC time is one of the following (ITU-T X.680 08/2015): YYMMDDhhmm[ss]Z YYMMDDhhmm[ss](+|-)hhmm
   * Regexp: ^ (?<year>\\d{2}) YY (?<month>\\d{2}) MM (?<day>\\d{2}) DD (?<hour>\\d{2}) hh
   * (?<minute>\\d{2}) mm (?<second>\\d{2})? ss (?<timezone> Z | Z or (+|-)hhmm ( [+-]\\d{4}
   * (+|-)hhmm ) ) $
   */
  private static final String UTC_TIME_PATTERN =
      "^(?<year>\\d{2})(?<month>\\d{2})(?<day>\\d{2})(?<hour>\\d{2})(?<minute>\\d{2})(?<second>\\d{2})?(?<timezone>Z|([+-]\\d{4}))$";

  private static final Pattern utcTimePattern = Pattern.compile(UTC_TIME_PATTERN);

  public BerUtcTime() {}

  public BerUtcTime(byte[] value) {
    this.value = value;
  }

  public BerUtcTime(String valueAsString) {
    super(valueAsString);
  }

  @Override
  public int encode(OutputStream reverseOS, boolean withTag) throws IOException {

    int codeLength = super.encode(reverseOS, false);

    if (withTag) {
      codeLength += tag.encode(reverseOS);
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

  @SuppressWarnings("WeakerAccess")
  Calendar asCalendar() throws ParseException {

    Matcher matcher = utcTimePattern.matcher(toString());

    if (!matcher.find()) throw new ParseException("", 0);

    String mg;
    int year = Integer.parseInt(matcher.group("year"));
    int month = Integer.parseInt(matcher.group("month"));
    month -= 1; // java.util.Calendar's month goes from 0 to 11
    int day = Integer.parseInt(matcher.group("day"));
    int hour = Integer.parseInt(matcher.group("hour"));
    int minute = Integer.parseInt(matcher.group("minute"));
    mg = matcher.group("second");
    int second = mg == null ? 0 : Integer.parseInt(mg);

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
