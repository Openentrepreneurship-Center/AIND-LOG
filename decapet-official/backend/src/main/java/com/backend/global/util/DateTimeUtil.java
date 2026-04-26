package com.backend.global.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * Date and time utility methods for Korean Standard Time (KST).
 */
public final class DateTimeUtil {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");
    private static final DateTimeFormatter YEAR_MONTH_FORMAT = DateTimeFormatter.ofPattern("yyyy년 MM월");

    private DateTimeUtil() {
        // Utility class
    }

    /**
     * Gets the current date-time in KST.
     */
    public static LocalDateTime now() {
        return LocalDateTime.now(KST);
    }

    /**
     * Gets the current date in KST.
     */
    public static LocalDate today() {
        return LocalDate.now(KST);
    }

    /**
     * Gets the current time in KST.
     */
    public static LocalTime currentTime() {
        return LocalTime.now(KST);
    }

    /**
     * Formats a date as "YYYY년 MM월" for Korean display.
     */
    public static String formatYearMonth(LocalDate date) {
        return date.format(YEAR_MONTH_FORMAT);
    }

    /**
     * Checks if a date is within the past N days from now.
     */
    public static boolean isWithinDays(LocalDateTime dateTime, int days) {
        LocalDateTime threshold = now().minusDays(days);
        return dateTime.isAfter(threshold);
    }

    /**
     * Checks if the given date is in the future.
     */
    public static boolean isFuture(LocalDate date) {
        return date.isAfter(today());
    }

    /**
     * Calculates the number of days between two dates.
     */
    public static long daysBetween(LocalDateTime from, LocalDateTime to) {
        return java.time.Duration.between(from, to).toDays();
    }
}
