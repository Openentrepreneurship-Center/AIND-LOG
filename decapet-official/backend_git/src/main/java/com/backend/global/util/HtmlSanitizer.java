package com.backend.global.util;

import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.springframework.stereotype.Component;

@Component
public class HtmlSanitizer {

	public String sanitize(String html) {
		if (html == null || html.isBlank()) {
			return html;
		}
		return Jsoup.clean(html, Safelist.relaxed());
	}
}
