# Repo Sync Modülü — Gereksinimler

## Giriş

Repo Sync Modülü, kullanıcının seçtiği repoya ait tüm veriyi (branch, commit, pull request listesi/detay/inceleme, dil) veritabanına senkronize etmek için **tek ve temiz** bir yol sunar. Modül hexagonal mimariyi (auth modülü gibi) izler, mevcut GitHub entity'lerini kullanır ve frontend'in birden fazla init controller çağırmasına gerek kalmayacak şekilde minimal bir API sunar. Hedef: seçilen repo için **eksiksiz, hızlı, basit ve temiz** sync.

**Referans:** [GITHUB_SYNC_MODULE_ANALYSIS.md](../../../docs/GITHUB_SYNC_MODULE_ANALYSIS.md)

## Sözlük

- **Seçilen_Repo**: Kullanıcının seçtiği tek repo (`repo fullName` ile tanımlanır, örn. `owner/repo`).
- **Repo_Sync_Modülü**: Seçilen repo için sync tetikleme ve takibinden sorumlu yeni hexagonal modül.
- **Sync**: O repoya ait tüm repo verisinin (branch, commit, PR, dil) veritabanında saklanmasını sağlama süreci.
- **Mevcut_GitHub_Entity'leri**: RepositoryEntity, BranchEntity, CommitEntity, PullRequestEntity, LanguageEntity, SyncJobEntity vb. — yeni entity eklenmez; modül mevcut olanları kullanır.
- **Port**: Use case'ler tarafından kullanılan, uygulama katmanına ait (dışa dönük) arayüz; altyapı adapter'ları tarafından implemente edilir.
- **Tetikleyici**: Seçilen repo için sync'i başlatan tek giriş noktası (örn. bir use case / bir API); yanıt kısa olur (“tetiklendi” veya “hazır”).
- **Hazırlık (Readiness)**: Repo verisinin metrikler için tam olup olmadığını gösteren durum (READY / PARTIAL / NOT_READY) ve bileşen bayrakları (branchesReady, commitsReady vb.).

## Gereksinimler

### Gereksinim 1 — Seçilen repo için tek tetikleyici

**Kullanıcı Hikayesi:** Frontend geliştiricisi olarak, kullanıcının seçtiği repo için sync'i tek bir yolla tetiklemek istiyorum; böylece dashboard birden fazla init endpoint çağırmak zorunda kalmasın.

#### Kabul Kriterleri

1. Frontend (veya başka bir backend akışı) seçilen repoyu senkronize etmek istediğinde, Repo_Sync_Modülü `login` ve `repo fullName` kabul eden **tek bir tetikleyici** (örn. tek bir API endpoint'i ile çağrılan tek use case) sunacaktır.
2. Tetikleyici çağrıldığında, Repo_Sync_Modülü çağıranın ayrı init endpoint'leri çağırmasına gerek kalmadan, o repo için gerekli tüm sync işinin (branch, commit, PR listesi, PR detayı, inceleme, dil) kuyruğa alınmasını veya çalıştırılmasını sağlayacaktır.
3. Repo_Sync_Modülü API yanıt süresinin düşük kalması için **kısa bir yanıt** dönecektir (örn. “sync tetiklendi” veya “zaten hazır”); ağır iş **asenkron** olacaktır (örn. mevcut job kuyruğu ve runner üzerinden).
4. Repo_Sync_Modülü, seçilen repo için frontend'in legacy init controller'larını (`/branches/init`, `/commits/init`, `/languages/init`, `/pull-requests/sync/initiate` vb.) çağırmasını **gerektirmeyecektir**.

### Gereksinim 2 — Seçilen repo için eksiksiz veri

**Kullanıcı Hikayesi:** Ürün sahibi olarak, seçilen repoya ait verinin veritabanında eksiksiz olmasını istiyorum; böylece dashboard boşluk olmadan tam metrikleri gösterebilsin.

#### Kabul Kriterleri

1. Seçilen repo için sync başarıyla tamamlandığında, sistem veritabanında (mevcut entity'leri kullanarak) en az şunları saklamış olacaktır: branch'ler, commit'ler (varsayılan branch, sayfalanmış), pull request listesi, pull request detayları (uygulanabilir olduğunda), incelemeler ve diller.
2. Sync sonrası o repo için hazırlık sorgulandığında, Repo_Sync_Modülü mevcut hazırlık anlamıyla tutarlı olacak şekilde durum (READY / PARTIAL / NOT_READY) ve bileşen bayraklarını (örn. branchesReady, commitsReady, pullRequestsReady, prDetailsReady, reviewsReady, languagesReady) döndürecek bir yol sunacaktır.
3. Repo_Sync_Modülü mevcut GitHub entity'lerini ve kalıcılığı (RepositoryEntity, BranchEntity, CommitEntity, PullRequestEntity, LanguageEntity, SyncJobEntity vb.) kullanacaktır; repo verisi depolama için **yeni entity türü tanımlamayacaktır**.

### Gereksinim 3 — Hızlı ve basit

**Kullanıcı Hikayesi:** Kullanıcı olarak, sync'in hızlı ve karmaşık adımlar olmadan tetiklenmesini istiyorum; deneyim öngörülebilir ve verimli olsun.

#### Kabul Kriterleri

1. Tetikleyici endpoint (veya use case) **makul sürede** (örn. birkaç saniyenin altında) dönecektir; uzun süren iş mevcut job runner vb. ile **asenkron** yapılacaktır.
2. Repo_Sync_Modülü en fazla **küçük bir işlem seti** sunacaktır (örn. “repo için sync tetikle”, “repo hazırlığını al”); frontend tek bir “sync durumu” veya “hazırlık” kontrolü dışında birden fazla init çağrısı veya karmaşık polling mantığı **yapmak zorunda kalmayacaktır**.
3. Repo_Sync_Modülü, sync mantığının (neyin hangi sırayla kuyruğa alınacağı) **tek bir yerde** toplanması (örn. tek use case + domain/port'lar) için tasarlanacaktır; birçok controller'a dağılmış olmayacaktır.

### Gereksinim 4 — Temiz hexagonal mimari

**Kullanıcı Hikayesi:** Geliştirici olarak, yeni sync mantığının ayrı bir hexagonal modülde olmasını istiyorum; böylece bağımsız bakım ve test edebilelim, ileride eski init/analyze akışını değiştirebilelim.

#### Kabul Kriterleri

1. Repo_Sync_Modülü net katmanlarla **hexagonal mimariyi** izleyecektir: **api** (controller, DTO'lar), **application** (port'lar, use case'ler, DTO'lar), **domain** (sync kuralları / domain servisi), **infrastructure** (port'ları implemente eden adapter'lar).
2. Application ve domain katmanları **yalnızca port arayüzlerine** (dışa dönük) bağımlı olacaktır; altyapıya (örn. mevcut GitHub controller'ları veya somut adapter'lar) **doğrudan bağımlı olmayacaktır**.
3. Infrastructure katmanı, modülün port'larını mevcut GitHub servisleri veya adapter'larına (örn. job enqueue, readiness okuma) uygun yerde delegasyon yaparak implemente edecektir; entity ve tablo **yeniden kullanımı korunacaktır**.
4. Modül, mevcut GitHub modülünün giriş noktalarını kirletmeden gelişebilmesi ve başarılı olunca eski akışın yerini alabilmesi için **ayrı bir pakette** (örn. `com.optimetric.reposync` veya net bir alt paket) konumlandırılacaktır.

### Gereksinim 5 — Hazırlık ve durum

**Kullanıcı Hikayesi:** Frontend geliştiricisi olarak, seçilen repoya ait verinin gösterime hazır olup olmadığını bilmek istiyorum; böylece metrikleri veya “hâlâ senkronize ediliyor” durumunu gösterebileyim.

#### Kabul Kriterleri

1. Repo_Sync_Modülü, belirli bir repo (fullName ile) için hazırlığı alacak ve mevcut hazırlık anlamıyla tutarlı şekilde durum (READY / PARTIAL / NOT_READY) ve bileşen bayraklarını döndürecek bir yol sunacaktır (örn. branchesReady, commitsReady vb.).
2. Hazırlık bilgisi mevcut veriden (örn. mevcut repository, branch, commit, PR, language tabloları) veya modülün bir port üzerinden kullandığı ayrı bir servisten türetilebilecektir; mevcut entity'lerden hesaplanabiliyorsa “hazırlık” için ayrı kalıcılık gerekmez.
3. İsteğe bağlı olarak Repo_Sync_Modülü seçilen repo için “sync durumu” (örn. job durumları) sunabilir; sunacaksa tanımlı bir port/API üzerinden sunacaktır ki frontend legacy endpoint'leri çağırmadan polling yapabilsin.

### Gereksinim 6 — Mevcut entity'lere dokunulmaması

**Kullanıcı Hikayesi:** Sistem bakımcısı olarak, bu iş sırasında mevcut GitHub modülünde şema ve entity değişikliği istemiyorum; geçiş riski düşük kalsın.

#### Kabul Kriterleri

1. Repo_Sync_Modülü repository, branch, commit, pull request veya language verisi için **yeni JPA entity veya veritabanı tablosu eklemeyecek veya değiştirmeyecektir**; yalnızca mevcut GitHub entity'lerini kullanacaktır.
2. Repo_Sync_Modülü iş kuyruğa almak için mevcut SyncJobEntity ve ilgili job tablolarına bağımlı olabilir; ayrı bir değişiklik olarak açıkça kararlaştırılmadıkça **yeni job tablosu gerektirmeyecektir**.
3. Yeni kod (use case'ler, port'lar, adapter'lar) **yeni modül paketinde** yer alacaktır; mevcut GitHub paket yapısı, yeni modülün port'larını karşılamak için orada açıkça adapter implemente edilmediği sürece **değişmeyecektir**.

---

## Özet

| ID   | Odak                     | Ana kabul |
|------|--------------------------|-----------|
| Req1 | Tek tetikleyici          | Tek tetikleyici (login + fullName); legacy init gerekmez; kısa yanıt; asenkron iş. |
| Req2 | Eksiksiz veri            | Branch, commit, PR, inceleme, dil saklanır; hazırlık sunulur; mevcut entity'ler kullanılır. |
| Req3 | Hızlı ve basit           | Tetikleyici hızlı döner; az sayıda işlem; merkezi sync mantığı. |
| Req4 | Hexagonal, ayrı modül    | api / application / domain / infrastructure; yalnızca port'lar; ayrı paket. |
| Req5 | Hazırlık ve durum        | Repo için hazırlık durumu ve bayraklar; isteğe bağlı sync job durumu. |
| Req6 | Entity/şema değişikliği yok | Yalnızca mevcut entity ve tablolar kullanılır. |
