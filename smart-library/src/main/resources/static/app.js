// --- 1. ADRESLER (Backend ile uyumlu endpointler) ---
const LOGIN_URL = "/api/v1/auth/authenticate";
const REGISTER_URL = "/api/v1/auth/register";
const BORROW_URL = "/api/v1/borrows"; // Ödünç alma işlemi için ana URL

// --- 2. SEÇİCİLER (Auth Sayfası İçin) ---
// Not: Bu elemanlar books.html sayfasında yoksa hata vermemesi için kontrol edilebilir
// ancak şimdilik mevcut yapıyı koruyoruz.
const loginForm = document.getElementById('loginForm');
const registerForm = document.getElementById('registerForm');
const loginBox = document.getElementById('loginBox');
const registerBox = document.getElementById('registerBox');

// --- 3. EKRAN DEĞİŞTİRME FONKSİYONLARI (Login <-> Register) ---
function showRegister() {
    if(loginBox && registerBox) {
        loginBox.style.display = 'none';
        registerBox.style.display = 'block';
    }
}

function showLogin() {
    if(loginBox && registerBox) {
        registerBox.style.display = 'none';
        loginBox.style.display = 'block';
    }
}

// --- 4. GİRİŞ YAPMA İŞLEMİ (Login) ---
if (loginForm) {
    loginForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        const email = document.getElementById('loginEmail').value;
        const password = document.getElementById('loginPassword').value;
        const msg = document.getElementById('loginMessage');

        msg.textContent = "Giriş yapılıyor...";
        msg.style.color = "black";

        try {
            const response = await fetch(LOGIN_URL, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ email, password })
            });

            const data = await response.json();

            if (response.ok) {
                msg.style.color = "green";
                msg.textContent = "Giriş Başarılı! Yönlendiriliyorsunuz...";
                
                // Token'ı kaydet
                localStorage.setItem('jwt_token', data.token);
                console.log("Token:", data.token);
                
                // Kitaplar sayfasına yönlendir
                setTimeout(() => {
                    window.location.href = "books.html"; 
                }, 1000); 

            } else {
                msg.style.color = "red";
                msg.textContent = "Hata: Bilgiler yanlış!";
            }
        } catch (error) {
            console.error(error);
            msg.textContent = "Sunucuya bağlanılamadı!";
            msg.style.color = "red";
        }
    });
}

// --- 5. KAYIT OLMA İŞLEMİ (Register) ---
if (registerForm) {
    registerForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        
        const name = document.getElementById('regName').value;
        const email = document.getElementById('regEmail').value;
        const password = document.getElementById('regPassword').value;
        
        const msg = document.getElementById('regMessage');
        msg.textContent = "Kayıt olunuyor...";
        msg.style.color = "black";

        try {
            const response = await fetch(REGISTER_URL, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ name, email, password }) 
            });

            // Başarılı kayıtta genelde response body dönmeyebilir, kontrol edelim:
            let data = {};
            if (response.headers.get("content-type")?.includes("application/json")) {
                data = await response.json();
            }

            if (response.ok) {
                msg.style.color = "green";
                msg.textContent = "Kayıt Başarılı! Şimdi giriş yapabilirsin.";
                
                setTimeout(() => {
                    showLogin();
                    const loginMsg = document.getElementById('loginMessage');
                    if(loginMsg) {
                        loginMsg.textContent = "Kayıt oldunuz, lütfen giriş yapın.";
                        loginMsg.style.color = "green";
                    }
                }, 2000);
                
            } else {
                msg.style.color = "red";
                msg.textContent = "Kayıt başarısız! (Email kullanılıyor olabilir)";
            }
        } catch (error) {
            console.error(error);
            msg.textContent = "Sunucu hatası!";
            msg.style.color = "red";
        }
    });
}

// ============================================================
// --- 6. KİTAP ÖDÜNÇ ALMA (BORROW) MODAL İŞLEMLERİ ---
// (Genellikle books.html sayfasında çalışır)
// ============================================================

// A. Ödünç Al Butonuna Basınca Modalı Aç
function openBorrowModal(bookId) {
    const modal = document.getElementById('borrowModal');
    const hiddenInput = document.getElementById('selectedBookId');
    
    if (modal && hiddenInput) {
        hiddenInput.value = bookId; // Kitap ID'sini hafızaya al
        modal.style.display = 'flex'; // Pencereyi aç
    } else {
        console.error("Modal veya hidden input bulunamadı! HTML'i kontrol et.");
    }
}

// B. Pencereden "Onayla"ya Basınca
async function confirmBorrow() {
    const bookId = document.getElementById('selectedBookId').value;
    const daysElement = document.getElementById('borrowDuration');
    const days = daysElement ? daysElement.value : 7; // Varsayılan 7 gün
    
    const token = localStorage.getItem('jwt_token');
    
    if (!token) {
        alert("Oturum süreniz dolmuş, lütfen tekrar giriş yapın.");
        window.location.href = "index.html";
        return;
    }

    try {
        // Backend'e ID ve GÜN bilgisini gönderiyoruz
        // URL Yapısı: /api/v1/borrows/{bookId}?days={days}
        const response = await fetch(`${BORROW_URL}/${bookId}?days=${days}`, {
            method: 'POST',
            headers: { 
                'Authorization': 'Bearer ' + token,
                // Bazı backendler POST isteğinde content-type bekleyebilir, gerekirse ekle:
                // 'Content-Type': 'application/json' 
            }
        });

        const msg = await response.text();
        
        if (response.ok) {
            alert("İşlem Başarılı: " + msg);
            closeBorrowModal();
            
            // Eğer sayfada fetchBooks fonksiyonu tanımlıysa listeyi yenile
            if (typeof fetchBooks === "function") {
                fetchBooks(); 
            }
        } else {
            alert("Hata: " + msg);
        }

    } catch (error) {
        console.error("Ödünç alma hatası:", error);
        alert("Sunucuya bağlanırken bir hata oluştu.");
    }
}

// C. Modalı Kapatma
function closeBorrowModal() {
    const modal = document.getElementById('borrowModal');
    if (modal) {
        modal.style.display = 'none';
    }
}

// D. Modal dışına tıklayınca kapatma (Opsiyonel UX geliştirmesi)
window.onclick = function(event) {
    const modal = document.getElementById('borrowModal');
    if (event.target == modal) {
        closeBorrowModal();
    }
}

// ============================================================
// --- 7. KÜTÜPHANESİ İSTATİSTİKLERİ ---
// ============================================================

// Kütüphane istatistiklerini getir ve modalı aç
async function openStatsModal() {
    const token = localStorage.getItem('jwt_token');
    
    if (!token) {
        alert("Oturum süreniz dolmuş, lütfen tekrar giriş yapın.");
        window.location.href = "index.html";
        return;
    }

    try {
        // Backend: GET /api/v1/books/stats/total-stock
        const response = await fetch("http://localhost:8081/api/v1/books/stats/total-stock", {
            headers: { 'Authorization': 'Bearer ' + token }
        });

        if (response.ok) {
            const stats = await response.json();
            
            // Toplam kitap ve stok bilgilerini göster
            document.getElementById('totalBooks').textContent = stats.totalBooks;
            document.getElementById('totalStock').textContent = stats.totalStock;
            
            // Kategoriye göre dağılımı tabloya doldur
            const categoryTable = document.getElementById('categoryTable');
            categoryTable.innerHTML = "";
            
            const categories = stats.byCategory;
            for (const [category, count] of Object.entries(categories)) {
                const row = document.createElement('tr');
                row.style.borderBottom = '1px solid rgba(212, 163, 115, 0.3)';
                row.innerHTML = `
                    <td style="padding: 8px; text-align: left;">${category}</td>
                    <td style="padding: 8px; text-align: right; color: #faedcd; font-weight: bold;">${count}</td>
                `;
                categoryTable.appendChild(row);
            }
            
            // Modalı aç
            document.getElementById('statsModal').style.display = 'flex';
        } else {
            alert("İstatistikler yüklenirken hata oluştu!");
        }
    } catch (error) {
        console.error("İstatistik hatası:", error);
        alert("Sunucuya bağlanırken bir hata oluştu.");
    }
}
