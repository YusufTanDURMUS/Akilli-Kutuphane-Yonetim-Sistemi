-- Smart Library - Duplicate Temizleme Scripti
-- Amaç: Aynı ISBN'e sahip kitapların tekrarlarını silmek ve gelecekte önlemek
-- Güvenli: Sadece tekrarları kaldırır, bir kaydı (en küçük id) bırakır

USE library_db;

-- 1) ISBN'i dolu olan tekrarlar: en küçük id'yi bırak
DELETE b1
FROM books b1
JOIN books b2 ON b1.isbn = b2.isbn AND b1.id > b2.id
WHERE b1.isbn IS NOT NULL;

-- 2) ISBN'i NULL olan tekrarlar: title+author eşleşenleri tekilleştir
DELETE b1
FROM books b1
JOIN books b2 
  ON b1.title = b2.title 
 AND COALESCE(b1.author,'') = COALESCE(b2.author,'')
 AND b1.id > b2.id
WHERE b1.isbn IS NULL;

-- 3) Gelecekte tekrar eklemeyi engelle: ISBN'i benzersiz yap (UNIQUE)
-- Not: MySQL UNIQUE index birden çok NULL'a izin verir, bu yüzden 2. adım gerekli
ALTER TABLE books ADD CONSTRAINT uq_books_isbn UNIQUE (isbn);

-- 4) Kontrol
SELECT isbn, COUNT(*) AS adet FROM books GROUP BY isbn HAVING COUNT(*)>1;
SELECT title, author, COUNT(*) AS adet FROM books WHERE isbn IS NULL GROUP BY title, author HAVING COUNT(*)>1;

-- 5) Örnek çıktı
-- "adet" sütunu boş geliyorsa temizlik tamam demektir.
