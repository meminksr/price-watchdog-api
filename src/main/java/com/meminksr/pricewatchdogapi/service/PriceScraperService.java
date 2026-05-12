package com.meminksr.pricewatchdogapi.service;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal; // BigDecimal kütüphanesini import ettik

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
                String rawPriceText = priceElement.text(); // Örn: "£51.77" veya "1.250,00 TL"
                return parsePriceText(rawPriceText); // Temizleme metodumuza gönderiyoruz
            } else {
                throw new RuntimeException("Fiyat elementi bulunamadı!");
            }

        } catch (IOException e) {
            throw new RuntimeException("Sayfaya bağlanırken hata oluştu: " + e.getMessage());
        }
    }

    /**
     * Karmaşık fiyat metinlerini temizleyip matematiksel BigDecimal objesine çevirir.
     */
    private BigDecimal parsePriceText(String priceText) {
        if (priceText == null || priceText.isBlank()) {
            return BigDecimal.ZERO;
        }

        // 1. Harfleri, para birimi sembollerini ve boşlukları temizle (Sadece rakam, virgül ve nokta kalsın)
        // Regex (Düzenli İfade) kullanarak 0-9, nokta ve virgül dışındaki her şeyi siliyoruz.
        String cleaned = priceText.replaceAll("[^0-9.,]", "");

        // 2. Türk stili (1.250,50) ve Amerikan stili (1,250.50) karmaşasını çözme
        if (cleaned.contains(",") && cleaned.contains(".")) {
            int commaIndex = cleaned.indexOf(",");
            int dotIndex = cleaned.lastIndexOf(".");

            if (commaIndex > dotIndex) {
                // Format: 1.250,50 (Türk stili) -> Binlik noktayı sil, kuruş virgülünü noktaya çevir
                cleaned = cleaned.replace(".", "").replace(",", ".");
            } else {
                // Format: 1,250.50 (Amerikan stili) -> Binlik virgülü sil
                cleaned = cleaned.replace(",", "");
            }
        } else if (cleaned.contains(",")) {
            // Sadece virgül varsa (örn: 1250,50) -> Virgülü noktaya çevir
            cleaned = cleaned.replace(",", ".");
        }

        // 3. Artık elimizde "1250.50" formatında saf bir sayı metni var, bunu BigDecimal'e çeviriyoruz
        return new BigDecimal(cleaned);
    }
}