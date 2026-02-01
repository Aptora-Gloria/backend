# GitHub Sync Modülü – Analiz (Kod Yazmadan)

**Tarih:** 2026-01-28  
**Amaç:** Mevcut durumu netleştirmek, hedefi tanımlamak ve auth modülü gibi temiz hexagonal bir sync modülü ile nasıl gidilebileceğini analiz etmek. **Kod yazılmadan sadece analiz.**

---

## 1. Mevcut Durum Özeti

### 1.1 Ne İstedik?
- Kullanıcının verdiği tüm repoların bilgilerinin DB’ye kaydedilmesi.
- Dashboard’da kullanıcı **tüm veriyi** görebilsin; başka sayfaya gidip “Sync” butonuna basmak zorunda kalmasın.

### 1.2 Ne Oldu?
- **Job servisi:** İstediğimiz gibi yazılamadı; commit’lerle ilgili yanlış/eksik sonuçlar.
- **Response süreleri:** Çok uzadı; kullanıcı deneyimi ve proje verimliliği düştü.
- **Frontend bağımlılığı:** Dashboard’da veriyi göstermek için frontend’in init controller’ları tetiklemesi (veya analyze + polling) gerekiyor; kullanıcı “sync butonuna tıklamadan” tüm veriyi göremiyor.

### 1.3 Mevcut Akış (Kısa)
- **Repo listesi:** `POST /api/github/repositories/init/user` → `InitializeUserRepositoriesUseCase` → GitHub’dan repo listesi çekilir, DB’ye yazılır (sadece repo meta; branch/commit/PR/language yok).
- **Veri doldurma:**  
  - **Eski yol:** Frontend ayrı endpoint’lere gidiyor: `/branches/init`, `/commits/init`, `/languages/init`, `/pull-requests/sync/initiate` vb.  
  - **Yeni yol (dokümanda):** Tek tetikleyici `POST /api/github/repositories/analyze`; arka planda runner job’ları çalıştırıyor; frontend `POST /api/github/repositories/sync/status` ile polling yapıyor.
- **Sorun:** Analyze + runner karmaşık hale geldi (job sırası, commit pagination, retry, süre); frontend hâlâ “bir yerde sync/analyze tetiklemek” zorunda; kullanıcı dashboard’a gelince veri hazır olmayabiliyor.

---

## 2. Hedef (Net)

**Tek hedef (tek cümle):**  
**Kullanıcının seçtiği repoya dair tüm bilgilerin eksiksiz, hızlı, basit ve en temiz yolla DB’ye kaydedilmesi.**

| Kriter | Anlamı |
|--------|--------|
| **Eksiksiz** | Branch, commit, PR, language vb. o repoya ait tüm veri tipleri DB’de. |
| **Hızlı** | Kayıt süreci makul sürede tamamlansın; gereksiz bekleme ve uzun response’lar olmasın. |
| **Basit** | Az sayıda adım, az karmaşa; frontend’in birçok init/sync endpoint’ine ihtiyacı kalmasın. |
| **Temiz** | Hexagonal, tek sorumluluk; auth modülü gibi sade mimari. |

- **Scope:** Kullanıcının **seçtiği repo** (tek repo odaklı akış; “tüm repolar” istenirse aynı mekanizma repo başına uygulanabilir).
- **Frontend:** Init controller tetiklemesine ihtiyaç kalmasın; tetikleme backend’de (repo seçildiğinde veya tek “sync bu repo” ile).
- **Kısıt:** Entity’ler aynı kalacak (mevcut GitHub entity’leri kullanılmaya devam).

---

## 3. Yaklaşım / Strateji

- **Yeni modül olarak başlıyoruz.** Eski GitHub modülüne (init controller’lar, analyze, runner, mevcut adapter’lar) şimdilik **dokunulmayacak**; paralel çalışacak.
- **Başarılı olursa eski modül temizlenecek.** Yeni modül hedefe ulaşıp stabil çalışınca: eski init/analyze/runner yapısı kademeli olarak **deprecate** edilip kaldırılacak veya yeni modüle taşınacak; tek, temiz akış kalacak.
- **Entity’ler aynı.** Yeni modül kendi entity’si üretmeyecek; mevcut GitHub entity’leri (Repository, Branch, Commit, PR, Language, SyncJob vb.) kullanacak. Böylece geçiş sırasında veri ve şema tutarlı kalır.

| Aşama | Yapılacak |
|-------|-----------|
| **Şimdi** | Yeni sync modülü (hexagonal) geliştir; hedef: seçilen repo için tüm veriyi eksiksiz, hızlı, basit ve temiz yolla DB’ye kaydet. |
| **Sonra** | Yeni modül başarılı ve stabil ise eski GitHub sync/init/analyze yapısını temizle (deprecate → kaldır veya yeni modüle taşı). |

---

## 4. Auth Modülü Referansı (Hexagonal) — Yeni Modül İçin Örnek

Auth modülü temiz hexagonal yapıda:

- **api:** Controller, request/response DTO’lar.
- **application:**  
  - **port (out):** `GitHubOAuthPort`, `JwtTokenPort`, `UserPersistencePort` (dış dünyaya çıkış).  
  - **usecase:** `LoginUseCase` (giriş orkestrasyonu).  
  - **dto:** `LoginCommand`, `LoginResult`, `UserInfo`.
- **domain:** `AuthenticationDomainService` (iş kuralları: login = code → token → user bilgisi → save → JWT).
- **infrastructure:**  
  - **http:** GitHub OAuth adapter.  
  - **persistence:** User kayıt/adapter.  
  - **security:** JWT adapter.

Application ve domain, infrastructure’a doğrudan bağımlı değil; sadece port’lar üzerinden konuşuyor. Use case ince, domain service iş mantığını taşıyor.

---

## 5. Öneri: Yeni “Sync” Modülü (Hexagonal, Entity’ler Aynı)

### 5.1 Fikir
- **Ayrı, sade bir modül:** Sorumluluğu tek: “Kullanıcının repoları için sync’i tetikle ve durumunu bil.”
- **Entity’ler:** Mevcut GitHub entity’leri (Repository, Branch, Commit, PullRequest, Language, SyncJob vb.) aynen kullanılır; yeni modül kendi entity’si yaratmaz, sadece “ne zaman sync, hangi repo” kararını ve tetiklemeyi yönetir.
- **Auth gibi katmanlar:**  
  - **api:** Mümkünse tek/sade endpoint’ler (ör. “sync’i tetikle”, “sync durumu”).  
  - **application:** Port’lar + use case’ler.  
  - **domain:** Sync tetikleme kuralları (örn. “user repo listesi yüklendiyse bu repolar için sync planla”).  
  - **infrastructure:** Mevcut GitHub altyapısını kullanan adapter’lar (job enqueue, readiness/status okuma).

### 5.2 Akış Hedefi (Kavramsal) — Seçilen Repo Odaklı

1. **Kullanıcı bir repo seçer** (dashboard’da veya repo listesinde).  
   → Backend: “Bu repoya dair tüm bilgileri DB’ye eksiksiz, hızlı ve temiz yolla kaydet.”
2. **Tetikleme:** Tek nokta (örn. “sync bu repo” use case): branch, commit, PR, language vb. için gerekli işler kuyruğa atılır veya doğrudan çalıştırılır; response kısa (“tetikledim” veya “hazır”).
3. **Dashboard:** Seçilen repo için hazır veriyi okur (metrics, readiness); init controller’ları çağırmaz.

Odak: **Seçilen tek repoya** ait verinin eksiksiz, hızlı ve basit şekilde DB’ye yazılması.

### 5.3 Modül Sınırları (Taslak)

- **Girdi:** Kullanıcının **seçtiği repo** (login + repo fullName). İstenirse “tüm repolarım” da aynı mekanizmayla repo başına uygulanabilir.
- **Çıktı:** O repoya dair tüm bilgilerin DB’ye **eksiksiz** kaydı; **hızlı** (tetikle ve dön / asenkron); **basit ve temiz** (tek use case, port’lar üzerinden).
- **Kullanılacak mevcut parçalar:**  
  - Repo listesi: `InitializeUserRepositoriesUseCase` veya eşdeğeri.  
  - Job kuyruğu: Mevcut analyze + SyncJobServicePort (enqueue).  
  - Durum: RepoSyncStatusServicePort (readiness).  
  - Runner: Aynen kalır (job’ları işler).

Yeni modül bunları “port” üzerinden kullanır; doğrudan GitHub infrastructure’ına (controller, adapter) bağımlı olmaz. Port’ların implementasyonu mevcut adapter’ları çağırabilir.

### 5.4 Eksiksiz, Hızlı, Basit ve Temiz Olması İçin

- **Eksiksiz:** Seçilen repo için branch, commit, PR, language vb. tüm veri tipleri tek akışla (veya tek tetiklemeyle) kuyruğa alınsın / işlensin; eksik kalan bileşen kalmasın.
- **Hızlı:** “Tetikle ve dön”; ağır iş asenkron (runner veya tek pipeline). Response süresi kısa; frontend uzun beklememeli.
- **Basit:** Tek tetikleyici (örn. “seçilen repo için tüm veriyi DB’ye kaydet” use case); karmaşık job sırası mümkünse sadeleştirilsin veya modül dışında tutulsun.
- **Temiz:** Hexagonal; domain sadece “bu repo, tüm veri” kuralı; port’lar üzerinden infra (mevcut adapter’lar) kullanılsın.

---

## 6. Özet Tablo

| Konu | Mevcut | Hedef |
|------|--------|--------|
| **Hedef (tek cümle)** | — | **Kullanıcının seçtiği repoya dair tüm bilgilerin eksiksiz, hızlı, basit ve en temiz yolla DB’ye kaydedilmesi** |
| Repo verisi DB’de | Init/user + ayrı init’ler (branch, commit, PR, language); karmaşık job/runner | Seçilen repo için tüm veri tek akışla DB’de; hızlı, basit |
| Frontend | Init controller’ları veya analyze + polling | Tetikleme backend’de; dashboard hazır veriyi okusun |
| Mimari | Dağınık (birçok init, analyze, runner, status) | Yeni sync modülü: hexagonal, tek sorumluluk, entity’ler aynı |
| Auth benzeri | — | application (port + use case), domain (“bu repo, tüm veri”), infrastructure (mevcut adapter’lar) |

---

## 7. Sonraki Adım (Yeni Modül Geliştirme)

**Requirements, design ve task’ler hazır:**

| Doküman | Konum | İçerik |
|---------|--------|--------|
| **Requirements** | [.kiro/specs/repo-sync-module/requirements.md](../.kiro/specs/repo-sync-module/requirements.md) | User story’ler, acceptance criteria, SHALL maddeleri (Req1–Req6). |
| **Design** | [.kiro/specs/repo-sync-module/design.md](../.kiro/specs/repo-sync-module/design.md) | Paket yapısı, port’lar (TriggerSyncPort, ReadinessPort), use case’ler, API, akış. |
| **Tasks** | [.kiro/specs/repo-sync-module/tasks.md](../.kiro/specs/repo-sync-module/tasks.md) | Uygulama sırasına göre task listesi (Phase 1–4). |

Geliştirmeye Phase 1 (application DTOs, port’lar, use case’ler) ile başlanabilir; entity ve şema değişikliği yok, mevcut GitHub entity’leri kullanılacak.
