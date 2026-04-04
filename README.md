# YazLab II - Proje I

## 1. Proje Bilgileri

- Ders: Yazılım Geliştirme Laboratuvarı II
- Proje: Dispatcher (API Gateway) üzerinden yönetilen mikroservis tabanlı mesajlaşma uygulaması.
- Ekip Üyeleri:
  - `İbrahim Alperen Keskin`
  - `Talha Fırat Meşe`


## 2. Giriş

Bu projede, modern yazılım geliştirme süreçlerine uygun şekilde mikroservis mimarisi üzerine kurulu bir mesajlaşma sistemi geliştirilmiştir. Sistemin tüm dış trafik yönetimi bir `Dispatcher` servisi üzerinden yapılmakt, kimlik doğrulama, rol bazlı erişim kontrolü, servisler arası güvenli iletişim ve gözlemlenebilirlik tek bir bütün olarak ele alınmaktadır.

Projenin temel amacı:

- Tüm dış istekleri tek bir giriş noktasında toplamak
- Mikroservisleri birbirinden veri ve ağ seviyesinde izole etmek
- Yetkilendirme mantığını merkezi hale getirmek
- Docker ile tek komutta ayağa kalkabilen bir yapı kurmak
- Dispatcher geliştirmesinde TDD yaklaşımını uygulamak

## 3. Problemin Tanımı ve Hedefler

Bu projede çözülmek istenen problem, yoğun trafik altında çalışabilecek bir sistemde:

- Kullanıcıların kayıt ve giriş işlemlerinin yönetilmesi
- Kullanıcı profil ve listeleme işlemlerinin ayrık servislerde tutulması
- Mesajlaşma akışlarının bağımsız servisler üzerinden yürütülmesi
- Tüm erişim ve yönlendirme mantığının Dispatcher üzerinde toplanması
- Servislerin doğrudan dış dünyaya açılmadan yalnızca iç ağdan haberleşmesi

hedeflerini aynı anda sağlamaktır.

## 4. Sistem Mimarisi

### 4.1. Mimari Genel Bakış

Sistem aşağıdaki bileşenlerden oluşmaktadır:

- `dispatcher`
- `auth-service`
- `user-service`
- `message-service`
- `mongo-auth`
- `mongo-user`
- `mongo-message`
- `mongo-dispatcher`
- `prometheus`
- `grafana`
- `loki`
- `promtail`
- `frontend`

### 4.2. Mikroservis Yapısı

- `auth-service`: kayıt, giriş ve JWT token üretimi
- `user-service`: kullanıcı profili ve kullanıcı listeleme işlemleri
- `message-service`: konuşma oluşturma, mesaj gönderme ve mesaj listeleme işlemleri
- `dispatcher`: tüm dış isteklerin karşılanması, doğrulama, yetki kontrolü ve uygun servise yönlendirme

### 4.3. Mermaid Mimari Diyagramı

```mermaid
flowchart LR
    Client["Kullanıcı / Postman / Frontend"] --> Dispatcher["Dispatcher API Gateway"]
    Frontend["Frontend (Nginx)"] --> Dispatcher
    Dispatcher --> Auth["Auth Service"]
    Dispatcher --> User["User Service"]
    Dispatcher --> Message["Message Service"]
    Dispatcher --> DDB["Mongo Dispatcher"]
    Auth --> ADB["Mongo Auth"]
    User --> UDB["Mongo User"]
    Message --> MDB["Mongo Message"]
    Dispatcher --> Prom["Prometheus"]
    Dispatcher --> Logs["Promtail / Loki / Grafana"]
```

## 5. Dispatcher ve TDD Süreci

Dispatcher servisi proje isterine uygun olarak TDD mantığı ile geliştirilmiştir.

### 5.1. TDD Yaklaşımı

- Red: önce başarısız test yazıldı
- Green: testi geçirecek minimum kod geliştirildi
- Refactor: kod okunabilirliği ve yapısı iyileştirildi

### 5.2. TDD Kanıtları

**Bu bölüme Git commit geçmişi ve ilgili test sınıfları ekleyeceğiz.**

- `DispatcherReadinessEndpointTest`
- `DispatcherAccessRulesEndpointTddRedTest`
- `DispatcherAuthorizationMongoIntegrationTest`
- `MongoAccessAuthorizationServiceTest`

### 5.3. TDD Akış Diyagramı

```mermaid
flowchart TD
    A["Başarısız test yaz"] --> B["Testi çalıştır"]
    B --> C["Fail (Red)"]
    C --> D["Minimum kodu yaz"]
    D --> E["Testi tekrar çalıştır"]
    E --> F["Pass (Green)"]
    F --> G["Refactor et"]
    G --> H["Tüm testleri tekrar doğrula"]
```

## 6. Richardson Maturity Model ve REST Tasarımı

### 6.1. Uygulanan REST İlkeleri

- Kaynak temelli URI yapısı kullanılmıştır
- HTTP metotları amacına uygun seçilmiştir
- Hata durumlarında uygun 4xx ve 5xx kodları dönülmektedir
- Veri transferi JSON formatında yapılmaktadır

### 6.2. Örnek Endpointler

| Endpoint | Metot | Açıklama |
| --- | --- | --- |
| `/auth/register` | `POST` | kullanıcı kaydı |
| `/auth/login` | `POST` | kullanıcı girişi |
| `/profile` | `GET` | aktif kullanıcı profili |
| `/users` | `GET` | admin kullanıcı listesi |
| `/conversations` | `GET` | kullanıcının konuşmaları |
| `/conversations` | `POST` | yeni konuşma oluşturma |
| `/conversations/{id}/messages` | `GET` | mesajları listeleme |
| `/conversations/{id}/messages` | `POST` | mesaj gönderme |
| `/conversations/{id}` | `DELETE` | konuşma silme |

### 6.3. RMM Değerlendirmesi

Bu bölüme projenin Richardson Maturity Model seviye 2 uyumluluğunu anlatacağız.

## 7. Sınıflar, Veri Yapıları ve İşleyiş

### 7.1. Temel Sınıflar

Bu bölüme her servisteki controller, service, repository ve model katmanları açıklanacaktır.

### 7.2. Sequence Diyagramları

#### Kayıt ve Giriş Akışı

```mermaid
sequenceDiagram
    participant C as Client
    participant D as Dispatcher
    participant A as Auth Service
    participant U as User Service

    C->>D: POST /auth/register
    D->>A: POST /auth/register
    A->>U: internal user create sync
    A-->>D: 201 Created
    D-->>C: 201 Created

    C->>D: POST /auth/login
    D->>A: POST /auth/login
    A-->>D: JWT token
    D-->>C: JWT token
```

#### Mesajlaşma Akışı

```mermaid
sequenceDiagram
    participant C as Client
    participant D as Dispatcher
    participant M as Message Service

    C->>D: POST /conversations
    D->>D: JWT + rol kontrolü
    D->>M: internal token + X-User
    M-->>D: Conversation
    D-->>C: 201 Created

    C->>D: POST /conversations/{id}/messages
    D->>M: internal token + X-User
    M-->>D: ChatMessage
    D-->>C: 201 Created
```

## 8. Veri Tabanları ve İzolasyon

Her servis kendi bağımsız NoSQL veri tabanına sahiptir:

- `auth-service -> mongo-auth`
- `user-service -> mongo-user`
- `message-service -> mongo-message`
- `dispatcher -> mongo-dispatcher`

### 8.1. Ağ İzolasyonu

- mikroservisler `expose` ile iç ağda tutulmuştur
- yalnızca `dispatcher` ve kullanıcı arayüzü dış dünyaya port açmaktadır
- mikroservisler, `X-Yazlab-Internal-Token` olmadan gelen istekleri reddetmektedir

Bu bölüme Docker ekran görüntüleri eklenecektir.

## 9. Docker ve Çalıştırma

### 9.1. Sistemi Ayağa Kaldırma

```bash
docker compose up --build
```

### 9.2. Servisler

- Dispatcher: `http://localhost:8080`
- Frontend: `http://localhost:8085`
- Grafana: `http://localhost:3000`
- Prometheus: `http://localhost:9090`
- Loki: `http://localhost:3100`

## 10. Gözlemlenebilirlik

Projede Dispatcher trafiği ve loglar aşağıdaki araçlarla izlenmektedir:

- `Prometheus`
- `Grafana`
- `Loki`
- `Promtail`

Bu bölüme:

- trafik paneli ekran görüntüleri
- durum kodları tablosu
- p95 yanıt süresi grafiği
- log kayıtları tablosu

ekleyeceğiz.

## 11. Yük Testleri

### 11.1. Kullanılan Araç

- `k6`

### 11.2. Test Senaryoları

Yük testi senaryosu `load-tests/k6-dispatcher.js` dosyasında yer almaktadır. Senaryo şu akışların Dispatcher üzerinden çalıştırılmasını hedefler:

- kullanıcı kaydı
- kullanıcı girişi
- profil sorgulama
- admin kullanıcı listeleme
- konuşma oluşturma
- mesaj gönderme
- konuşma listeleme
- mesaj listeleme

### 11.3. Yük Testini Çalıştırma

```bash
k6 run load-tests/k6-dispatcher.js
```

Belirli bir tepe yük ile:

```bash
k6 run -e BASE_URL=http://localhost:8080 -e MAX_TARGET=200 load-tests/k6-dispatcher.js
```

Sonuçları JSON olarak dışarı almak için:

```bash
k6 run --summary-export=load-tests/results/k6-summary-200.json load-tests/k6-dispatcher.js
```

### 11.4. Test Sonuç Tablosu

| Eşzamanlı Yük | Ortalama Süre | p95 | Hata Oranı | Not |
| --- | --- | --- | --- | --- |
| 50 | `DOLDURULACAK` | `DOLDURULACAK` | `DOLDURULACAK` | `DOLDURULACAK` |
| 100 | `DOLDURULACAK` | `DOLDURULACAK` | `DOLDURULACAK` | `DOLDURULACAK` |
| 200 | `DOLDURULACAK` | `DOLDURULACAK` | `DOLDURULACAK` | `DOLDURULACAK` |
| 500 | `DOLDURULACAK` | `DOLDURULACAK` | `DOLDURULACAK` | `DOLDURULACAK` |

### 12. Ekran Görüntüleri ve Sistem Doğrulaması
Bu bölümde, uygulamanın kullanıcı arayüzü, mikroservislerin API testleri ve sistemin güvenlik katmanlarına dair kanıtlar sunulmaktadır.

Frontend ve Kullanıcı Arayüzü

Ana Sayfa: Uygulamanın giriş sonrası kullanıcıyı karşıladığı genel arayüz.

<img width="1894" height="942" alt="Frontend Ana Ekran" src="https://github.com/user-attachments/assets/5e59a5e9-f2dd-40f7-a3c9-52749ea3d809" />
<p align="center">--------------------------------------------------</p>

Kayıt ve Giriş İşlemleri: Kullanıcı yetkilendirme akışını gösteren kayıt ve giriş formları.

<p align="center">
<img width="48%" src="https://github.com/user-attachments/assets/7006ac1d-296c-4d6f-87d6-0b015f76b4ed" />
&nbsp;&nbsp;
<img width="48%" src="https://github.com/user-attachments/assets/f2f0dc99-c272-47a8-98b7-2d2682845be2" />
</p>
<p align="center">--------------------------------------------------</p>

Postman: API Endpoint Testleri


Profil ve Kullanıcı Sorguları: Kullanıcı bilgilerinin getirilmesi ve sistemdeki kullanıcıların listelenmesi işlemleri.

<p align="center">
<img width="48%" src="https://github.com/user-attachments/assets/5b4971d0-6189-4165-b9d5-87a87e4419d3" />
&nbsp;&nbsp;
<img width="48%" src="https://github.com/user-attachments/assets/c704a640-b468-4327-be26-d64e999903f1" />
</p>
<p align="center">--------------------------------------------------</p>

Mesajlaşma ve Konuşma Yönetimi: Yeni bir konuşma başlatma ve mesaj gönderimi isteklerinin doğrulanması.

<img width="538" alt="Mesajlaşma İstekleri" src="https://github.com/user-attachments/assets/690fd6ce-7a23-4f3b-bc97-1dc9a1a2e4e6" />
<p align="center">--------------------------------------------------</p>

Sistem Güvenliği ve İzolasyon


Doğrudan Mikroservis Erişiminin Reddedilmesi: Mimari gereği mikroservisler dış ağa kapalıdır. Aşağıdaki görselde, API Gateway (Dispatcher) üzerinden geçmeden doğrudan bir mikroservise (Port: 8082) erişilmeye çalışıldığında alınan bağlantı reddi (ECONNREFUSED) hatası görülmektedir. Bu durum, servis izolasyonunun başarılı olduğunu kanıtlar.

<img width="536" alt="Güvenlik Testi" src="https://github.com/user-attachments/assets/40269309-3e6a-4bd9-befe-e493a2b0f362" />

<p align="center">--------------------------------------------------</p>

Docker ve Sistem İzleme

Docker Konteyner Listesi: Sistemde aktif olarak çalışan mikroservisler, veritabanları (MongoDB) ve izleme araçlarının (Loki, Promtail, Grafana) çalışma durumu.

<img width="1099" alt="Docker Konteyner Listesi" src="https://github.com/user-attachments/assets/24b8f878-823d-444c-ad84-ca24595f3cef" />

<p align="center">--------------------------------------------------</p>

Grafana Dashboard: Sistem loglarının merkezi olarak takip edildiği görselleştirme ekranı.

(İlgili görsel eklenecektir)



## 13. Test Senaryoları ve Sonuçları

Bu bölüme:

- birim testleri
- entegrasyon testleri
- Dispatcher TDD testleri
- yük testi sonuçları
- hata yönetimi senaryoları

ayrıntılı olarak yazılacaktır.

## 14. Karmaşıklık Analizi ve Literatür

Bu bölüme:

- kullanılan algoritmaların genel analizi
- mimari tercihlerin gerekçesi
- mikroservis, API Gateway, REST ve TDD literatür özeti

eklenecektir.

## 15. Sonuç ve Tartışma

Bu bölümde:

- projede elde edilen başarılar
- sistemin sınırlılıkları
- geliştirilebilecek yönler
- olası gelecek çalışmalar

değerlendirilecektir.

## 16. Ekler

- Markdown ve Mermaid kaynakları
- kullanılan bağlantılar
- gerekiyorsa ek diyagramlar
