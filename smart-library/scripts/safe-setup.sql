-- Güvenli ve idempotent kurulum (DROP yok)
CREATE DATABASE IF NOT EXISTS library_db 
  CHARACTER SET utf8mb4 
  COLLATE utf8mb4_unicode_ci;
USE library_db;

-- Users
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255),
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role ENUM('USER','ADMIN','STUDENT') DEFAULT 'USER',
    created_at DATETIME(6) DEFAULT CURRENT_TIMESTAMP,
    reset_token VARCHAR(255) NULL UNIQUE,
    reset_token_expiry TIMESTAMP NULL,
    verified BOOLEAN DEFAULT FALSE,
    verification_token VARCHAR(255) NULL UNIQUE,
    verification_token_expiry TIMESTAMP NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Books (UNIQUE isbn ile tekilleştirme)
CREATE TABLE IF NOT EXISTS books (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    author VARCHAR(255),
    category VARCHAR(255),
    isbn VARCHAR(255),
    page_count INT,
    stock INT DEFAULT 5,
    image_url VARCHAR(1000),
    created_by VARCHAR(255),
    created_at DATETIME(6) DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uq_books_isbn (isbn)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Borrows
CREATE TABLE IF NOT EXISTS borrows (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    borrow_date DATE,
    due_date DATE,
    return_date DATE DEFAULT NULL,
    book_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    FOREIGN KEY (book_id) REFERENCES books(id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Örnek Kitap Verisi (idempotent - ISBN unique olduğundan tekrar yazım günceller)
INSERT INTO books (title, author, category, isbn, page_count, stock, image_url) VALUES
('Nutuk','Mustafa Kemal Atatürk','Tarih','9789751020001',599,100,'https://i.dr.com.tr/cache/600x600-0/originals/0000000064038-1.jpg'),
('Suç ve Ceza','Fyodor Dostoyevski','Dünya Klasikleri','9789750719387',687,20,'https://i.dr.com.tr/cache/600x600-0/originals/0000000064112-1.jpg'),
('Sefiller','Victor Hugo','Dünya Klasikleri','9789750739927',1724,15,'https://i.dr.com.tr/cache/600x600-0/originals/0001792758001-1.jpg'),
('Kürk Mantolu Madonna','Sabahattin Ali','Türk Edebiyatı','9789753638029',160,50,'https://i.dr.com.tr/cache/600x600-0/originals/0000000064031-1.jpg'),
('1984','George Orwell','Bilim Kurgu','9789750718533',352,40,'https://i.dr.com.tr/cache/600x600-0/originals/0000000064035-1.jpg'),
('Simyacı','Paulo Coelho','Roman','9789750726439',188,35,'https://i.dr.com.tr/cache/600x600-0/originals/0000000064042-1.jpg'),
('Harry Potter ve Felsefe Taşı','J.K. Rowling','Fantastik','9789750802942',274,60,'https://i.dr.com.tr/cache/600x600-0/originals/0000000064036-1.jpg'),
('Dönüşüm','Franz Kafka','Modern Klasikler','9789750719356',104,25,'https://i.dr.com.tr/cache/600x600-0/originals/0000000064040-1.jpg'),
('Şeker Portakalı','Jose Mauro de Vasconcelos','Roman','9789750738609',182,45,'https://i.dr.com.tr/cache/600x600-0/originals/0000000064033-1.jpg'),
('Saatleri Ayarlama Enstitüsü','Ahmet Hamdi Tanpınar','Türk Edebiyatı','9789754700078',382,30,'https://i.dr.com.tr/cache/600x600-0/originals/0000000064045-1.jpg'),
('İnce Memed 1','Yaşar Kemal','Türk Edebiyatı','9789750807084',436,80,'https://i.dr.com.tr/cache/600x600-0/originals/0000000064032-1.jpg'),
('Tutunamayanlar','Oğuz Atay','Türk Edebiyatı','9789754700115',724,45,'https://i.dr.com.tr/cache/600x600-0/originals/0000000064037-1.jpg'),
('Çalıkuşu','Reşat Nuri Güntekin','Roman','9789751026782',544,60,'https://i.dr.com.tr/cache/600x600-0/originals/0000000064039-1.jpg'),
('Aşk-ı Memnu','Halit Ziya Uşaklıgil','Türk Klasikleri','9789750739958',508,30,'https://i.dr.com.tr/cache/600x600-0/originals/0001792760001-1.jpg'),
('Beyaz Diş','Jack London','Dünya Klasikleri','9786053609375',256,90,'https://i.dr.com.tr/cache/600x600-0/originals/0000000650917-1.jpg'),
('Hayvan Çiftliği','George Orwell','Politik Kurgu','9789750719388',152,120,'https://i.dr.com.tr/cache/600x600-0/originals/0000000064034-1.jpg'),
('Satranç','Stefan Zweig','Modern Klasikler','9786053606114',84,150,'https://i.dr.com.tr/cache/600x600-0/originals/0000000411850-1.jpg'),
('Fareler ve İnsanlar','John Steinbeck','Dünya Klasikleri','9789755705859',111,75,'https://i.dr.com.tr/cache/600x600-0/originals/0000000362899-1.jpg'),
('Küçük Prens','Antoine de Saint-Exupéry','Çocuk/Felsefe','9789750724817',112,200,'https://i.dr.com.tr/cache/600x600-0/originals/0000000632345-1.jpg'),
('Da Vinci Şifresi','Dan Brown','Gerilim','9789752104052',520,40,'https://i.dr.com.tr/cache/600x600-0/originals/0000000147321-1.jpg'),
('Yüzüklerin Efendisi - Yüzük Kardeşliği','J.R.R. Tolkien','Fantastik','9789753420600',496,55,'https://i.dr.com.tr/cache/600x600-0/originals/0000000064560-1.jpg'),
('Uçurtma Avcısı','Khaled Hosseini','Roman','9789752891365',375,65,'https://i.dr.com.tr/cache/600x600-0/originals/0000000176885-1.jpg'),
('Sol Ayağım','Christy Brown','Biyografi','9789759992386',192,70,'https://i.dr.com.tr/cache/600x600-0/originals/0000000236052-1.jpg'),
('Don Kişot','Cervantes','Dünya Klasikleri','9789750806667',908,25,'https://i.dr.com.tr/cache/600x600-0/originals/0000000064114-1.jpg'),
('Monte Kristo Kontu','Alexandre Dumas','Macera','9789751419737',1500,20,'https://i.dr.com.tr/cache/600x600-0/originals/0000000669176-1.jpg'),
('Martin Eden','Jack London','Dünya Klasikleri','9789750720499',520,35,'https://i.dr.com.tr/cache/600x600-0/originals/0000000562473-1.jpg'),
('Fahrenheit 451','Ray Bradbury','Bilim Kurgu','9786053757816',208,50,'https://i.dr.com.tr/cache/600x600-0/originals/0001742686001-1.jpg'),
('Bülbülü Öldürmek','Harper Lee','Roman','9789755706788',360,40,'https://i.dr.com.tr/cache/600x600-0/originals/0000000574136-1.jpg'),
('Aylak Adam','Yusuf Atılgan','Türk Edebiyatı','9789750807169',192,55,'https://i.dr.com.tr/cache/600x600-0/originals/0000000058284-1.jpg'),
('Puslu Kıtalar Atlası','İhsan Oktay Anar','Tarihi Kurgu','9789750501623',238,85,'https://i.dr.com.tr/cache/600x600-0/originals/0000000052733-1.jpg')
ON DUPLICATE KEY UPDATE 
  title=VALUES(title), author=VALUES(author), category=VALUES(category), 
  page_count=VALUES(page_count), stock=VALUES(stock), image_url=VALUES(image_url);

-- Admin kullanıcı (bcrypt hash'i değiştirin!)
INSERT INTO users (name, email, password, role, verified)
VALUES ('admin', 'yusuftandurmus61@icloud.com', '$2a$10$DUMMYHASHREPLACEME', 'ADMIN', TRUE)
ON DUPLICATE KEY UPDATE 
  role=VALUES(role), verified=VALUES(verified);

-- Hızlı doğrulama
SELECT COUNT(*) AS total_books FROM books;
SELECT id, name, email, role, verified FROM users;
