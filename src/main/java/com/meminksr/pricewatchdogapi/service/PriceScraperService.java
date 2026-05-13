package com.meminksr.pricewatchdogapi.service;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;

@Service
public class PriceScraperService {

    public BigDecimal scrapePrice(String url, String cssSelector) {
        try {
            Document document = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                    .timeout(5000)
                    .get();

            Element priceElement = document.selectFirst(cssSelector);

            if (priceElement != null) {
                String rawPriceText = priceElement.text();
                return parsePriceText(rawPriceText); // We are sending it to our cleaning department
            } else {
                throw new RuntimeException("The price element could not be found!");
            }

        } catch (IOException e) {
            throw new RuntimeException("An error occurred while loading the page: " + e.getMessage());
        }
    }

    /**
     * Karmaşık fiyat metinlerini temizleyip matematiksel BigDecimal objesine çevirir.
     */
    private BigDecimal parsePriceText(String priceText) {
        if (priceText == null || priceText.isBlank()) {
            return BigDecimal.ZERO;
        }
        String cleaned = priceText.replaceAll("[^0-9.,]", "");

        if (cleaned.contains(",") && cleaned.contains(".")) {
            int commaIndex = cleaned.indexOf(",");
            int dotIndex = cleaned.lastIndexOf(".");

            if (commaIndex > dotIndex) {
                cleaned = cleaned.replace(".", "").replace(",", ".");
            } else {
                cleaned = cleaned.replace(",", "");
            }
        } else if (cleaned.contains(",")) {
            cleaned = cleaned.replace(",", ".");
        }

        return new BigDecimal(cleaned);
    }
}