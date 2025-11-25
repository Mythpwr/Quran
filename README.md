# Quran-Data

Bu repo, Kuran-ı Kerim’in sure ve ayetlerini JSON formatında içeren bir veri seti içerir.  
Kendi projelerimde kullanmak için oluşturuldu ve başkalarının da yararlanması amacıyla paylaşılmıştır.
 
## İçerik
- Tüm sureler
- Ayet numaraları
- Sure isimleri (Arapça ve Türkçe opsiyonel)
- JSON formatında temiz veri yapısı
- Sayfa düzeni formatında
  

## Dosya düzenleme
-json içersindeki anahtar kelimeleri değiştirebilirsiniz:
  ```
import json

inputFileName= "sayfa_duzenli_kuran_1.json"
outputFileName ="output.json"
# JSON dosyasını oku
with open(inputFileName, "r", encoding="utf-8") as f:
    data = json.load(f)

# Anahtarları değiştir
# pages anahtarını değiştir
data["new_name"] = data.pop("pages")

# Yeni anahtar üzerinden devam edebilirsin
for page in data["pages"]:
    # surahs anahtarını değiştir
    page["new_name"] = page.pop("surahs")
    
    for surah in page["surahs"]:
        # surah içindeki anahtarları değiştir
        surah["new_name"] = surah.pop("sureAdiTr")
        surah["new_name"] = surah.pop("sureAdiArapca")

        for ayet in surah["ayetler"]:
            ayet["new_name"] = ayet.pop("secdeAyeti")
            ayet["new_name"] = ayet.pop("ayetId")
            ayet["new_name"] = ayet.pop("ayetText")
            ayet["new_name"] = ayet.pop("meal")# Yeni JSON dosyasına kaydet
            
with open(outputFileName, "w", encoding="utf-8") as f:
    json.dump(data, f, ensure_ascii=False, indent=4)

print(f"JSON verisi '{outputFileName}' olarak kaydedildi.")

  ```
## Veri Kaynağı ve Referans

- Bu veri seti, **Diyanet İşleri Başkanlığı’nın resmi Kuran uygulamasındaki** sayfa ve ayet düzenine göre hazırlanmıştır.  
- Sayfa numaraları, sure ve ayet numaraları **Diyanet Mushafı** referans alınarak oluşturulmuştur.  
- Farklı Mushaflarda (ör. Medine Mushafı) **sayfa numaraları ve ayet dağılımı değişebilir**.  

## Kullanım
Apache License 2.0 ile lisanslanmıştır.  
Detaylar için LICENSE dosyasına bakabilirsiniz.
