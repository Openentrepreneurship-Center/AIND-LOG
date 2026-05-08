package com.backend.global.util;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import com.backend.global.util.DateTimeUtil;

public class UniqueNumberGenerator {

	private static final SecureRandom RANDOM = new SecureRandom();

	public static String generate() {
		LocalDateTime now = DateTimeUtil.now();
		int year = now.getYear() % 100;       // 2026 → 26
		int month = now.getMonthValue();      // 1~12
		int randomPart = RANDOM.nextInt(100_000_000); // 0~99999999

		return String.format("%02d%02d%08d", year, month, randomPart);
	}
}
