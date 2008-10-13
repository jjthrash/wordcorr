package org.wordcorr.gui;

import java.io.*;
import java.text.*;
import java.util.*;

/**
 * MessageHandler manages localization.
 **/
public final class Messages {

    public static final String NA = "N/A";

    /**
     * Get the instance of MessageHandler associated with language.
     *
     * @param language The language
     **/
    public static final Messages getInstance(String code, String country) {
        Messages mh;
        synchronized(_index) {
            String key = code + "_" + country;
            mh = (Messages) _index.get(key);
            if (mh == null) {
                mh = new Messages(code, country);
                _index.put(key, mh);
            }
        }
        return mh;
    }

    /**
     * Get the site default instance of Messages.
     **/
    public static final Messages getInstance() {
        return Messages.getInstance("en", "US");
    }

    /**
     * Get the language string.
     *
     * @param key The properties file key
     **/
    public String getString(String key) {
        try {
            return _bundle.getString(key);
        } catch (Exception e) { }
        return NA;
    }

    /**
     * Get the first character of the language string.
     **/
    public char getChar(String key) {
        return getString(key).charAt(0);
    }

    /**
     * Utility method to get compound message.
     *
     * @param key The properties file key
     * @param arguments The arguments to apply
     **/
    public String getCompoundMessage(String key, Object[] arguments) {
        MessageFormat formatter = new MessageFormat(getString(key));
        formatter.setLocale(_locale);
        String compoundValue = formatter.format(arguments);
        return compoundValue;
    }

    /**
     * Utility method to get compound message.
     *
     * @param key The properties file key
     * @param argument The single string argument
     **/
    public String getCompoundMessage(String key, Object argument) {
        return getCompoundMessage(key, new Object[] { argument });
    }

    /**
     * Utility method to get date string based on locale.
     *
     * @param value The date value
     * @param dateFormatType The type of date format
     **/
    public String getFormattedDate(Date value, int dateFormatType) {
        return DateFormat.getDateInstance(dateFormatType, _locale).format(value);
    }

    /**
     * Utility method to get time string based on locale.
     *
     * @param value The time value
     * @param timeFormatType The type of time format
     **/
    public String getFormattedTime(Date value, int timeFormatType) {
        return DateFormat.getTimeInstance(timeFormatType, _locale).format(value);
    }

    /**
     * Utility method to get date-time string based on locale.
     *
     * @param value The date-time value
     * @param dateFormatType The type of date format
     * @param timeFormatType The type of time format
     **/
    public String getFormattedDateTime(Date value, int dateFormatType, int timeFormatType) {
        return DateFormat.getDateTimeInstance(dateFormatType, timeFormatType, _locale).format(value);
    }

    /**
     * Utility method to get date string based on locale.
     *
     * @param value The string value
     **/
    public Date getDate(String value, Date def) {
        try {
            return DateFormat.getDateInstance(DateFormat.FULL, _locale).parse(value);
        } catch (ParseException ignored) {
            return def;
        }
    }

    /**
     * Utility method to get date/time string based on locale.
     *
     * @param value The string value
     **/
    public Date getDateTime(String value, Date def) {
        try {
            return DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.FULL, _locale).parse(value);
        } catch (ParseException ignored) {
            return def;
        }
    }

    /**
     * Utility method to get numeric string based on locale.
     *
     * @param value The number
     **/
    public String getFormattedNumber(double value) {
        return NumberFormat.getNumberInstance(_locale).format(value);
    }

    /**
     * Utility method to get numeric string based on locale.
     *
     * @param value The number
     **/
    public String getFormattedNumber(Double value) {
        return getFormattedNumber(value.doubleValue());
    }

    /**
     * Utility method to get currency string based on locale.
     *
     * @param value The dollar amount
     **/
    public String getFormattedCurrency(double value) {
        return NumberFormat.getCurrencyInstance(_locale).format(value);
    }

    /**
     * Utility method to get currency string based on locale.
     *
     * @param value The dollar amount
     **/
    public String getFormattedCurrency(Double value) {
        return getFormattedCurrency(value.doubleValue());
    }

    /**
     * Utility method to get percentage string based on locale.
     *
     * @param value The percentage value
     **/
    public String getFormattedPercent(double value) {
        return NumberFormat.getPercentInstance(_locale).format(value);
    }

    /**
     * Utility method to get percentage string based on locale.
     *
     * @param value The percentage value
     **/
    public String getFormattedPercent(Double value) {
        return getFormattedPercent(value.doubleValue());
    }

    /**
     * Utility method to get numeric string based on pattern.
     *
     * @param value The number
     * @param pattern The pattern
     **/
    public String getFormattedDecimal(double value, String pattern) {
        DecimalFormat df = new DecimalFormat(pattern);
        return df.format(value);
    }

    /**
     * Utility method to get numeric string based on pattern.
     *
     * @param value The number
     * @param pattern The pattern
     **/
    public String getFormattedDecimal(Double value, String pattern) {
        return getFormattedDecimal(value.doubleValue(), pattern);
    }

    /**
     * Construct an instance of Messages with the given language.
     *
     * @param language The language
     **/
    private Messages(String code, String country) {
        _locale = new Locale(code, country);
        try {
            _bundle = ResourceBundle.getBundle("org.wordcorr.messages", _locale);
        } catch (MissingResourceException e) {
            e.printStackTrace();
        }
    }

    private static Hashtable _index = new Hashtable();
    private Locale _locale;
    private ResourceBundle _bundle;
}
