package com.archervanderwaal;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class DateFormatUtils {

    public static final String YYYY_MM_DD = "yyyy-MM-dd";
    public static final String YYYYMMDD = "yyyyMMdd";
    public static final String YYYY_MM_DD_HH_MM_SS = "yyyy-MM-dd'T'HH:mm:ss";
    private static final Logger logger = LoggerFactory.getLogger(DateFormatUtils.class);
    private static final int MAX_DATE_PATTERN = 64;

    private static final ConcurrentHashMap<String, ThreadLocal<SimpleDateFormat>> DATE_FORMAT_MAP = new ConcurrentHashMap<>();

    static {
        registerDateConverter(YYYY_MM_DD);
        registerDateConverter(YYYYMMDD);
        registerDateConverter(YYYY_MM_DD_HH_MM_SS);
    }

    private static void registerDateConverter(String pattern) {
        DATE_FORMAT_MAP.put(pattern, ThreadLocal.withInitial(() -> new SimpleDateFormat(pattern)));
    }

    private static boolean tryRegisterDateConverter(String pattern) {
        if (DATE_FORMAT_MAP.containsKey(pattern)) {
             return false;
        }
        if (DATE_FORMAT_MAP.size() < MAX_DATE_PATTERN) {
            registerDateConverter(pattern);
            return false;
        }
        logger.error("The number of pattern supports exceeded the limit");
        return true;
    }

    public static Optional<String> format(String pattern, Date date) {
        if (StringUtils.isBlank(pattern) || date == null) {
            logger.error("pattern and date must be not null");
            return Optional.empty();
        }
        if (tryRegisterDateConverter(pattern)) {
            return Optional.empty();
        }
        return Optional.of(DATE_FORMAT_MAP.get(pattern).get().format(date));
    }

    public static Optional<Date> parse(String dateString, String pattern) {
        if (StringUtils.isBlank(dateString) || StringUtils.isBlank(pattern)) {
            logger.error("dateString and pattern must be not null");
            return Optional.empty();
        }
        if (tryRegisterDateConverter(pattern)) {
            return Optional.empty();
        }
        try {
            return Optional.of(DATE_FORMAT_MAP.get(pattern).get().parse(dateString));
        } catch (Exception e) {
            logger.error("error parsing date, ", e);
            return Optional.empty();
        }
    }
}
