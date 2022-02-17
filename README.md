# Implementasi Algoritma Greedy untuk Pembuatan Bot Permainan _Entelect Challenge 2020 - Overdrive_

> Program beserta `.jar` untuk bot penyelesaian permainan _Overdrive_ menggunakan bahasa pemrograman _Java_ dan algoritma _Greedy_
> sebagai Tugas Besar 1 IF2211 Strategi Algoritma

**[IMPORTANT!]** Isi folder `bin` adalah build Maven terbaru. Pembuatan build Maven baru tidak akan disimpan di folder `bin`, melainkan di folder `src\starter-bots\java\target`

## Daftar Isi
- [Deskripsi](#deskripsi)
- [Penggunaan](#penggunaan)
- [Identitas](#identitas)

## Deskripsi
Permainan _Overdrive_ adalah permainan dimana dua _bot_ mobil akan berusaha untuk memenangkan pertandingan. Bot yang dibuat menggunakan implementasi algoritma _greedy_ yang mengambil keputusan terbaik yang dapat diambil dalam suatu ronde, tanpa memperhitungkan keadaan ronde selanjutnya ataupun sebelumnya. Hal ini selaras dengan sifat algoritma _greedy_ yang mengambil optimasi secara lokal (berupa minimasi atau maksimasi kemungkinan). Optimasi lokal belum tentu mencapai keadaan optimum secara global, namun optimasi lokal dapat menghasilkan keadaan optimum lokal berupa keputusan terbaik yang diambil pada suatu saat tersebut.

Isi direktori adalah sebagai berikut:
```
├── bin   [isi folder hasil kompilasi beserta jar terbaru]
├── docs  [dokumen
├── src               [folder source, sumber: https://github.com/EntelectChallenge/2020-Overdrive/releases/tag/2020.3.4]
    ├── reference-bot [algoritma bot yang dijadikan referensi, sumber dari tautan di atas]
    ├── starter-bot   [implementasi algoritma greedy]
        ├── java
            ├── src / main / java / za / co / entelect / challenge
                ├── command
                ├── entities
                ├── enum
                ├── Bot.java      [implementasi utama algoritma
                ├── Helper.java   [deklarasi class sebagai fungsi helper untuk menjalankan algoritma]
                ├── Main.java     [script utama untuk menjalankan bot] 
             ├── target   [isi kompilasi build Maven, jika dilakukan pembaruan]
             ├── bot.json [konfigurasi bot]
             ├── java-starter-bot.iml
             ├── pom.xml  [file berisi informasi build Maven]
    ├── game-config.json                      [pengaturan permainan game]
    ├── game-engine.jar                       [.jar untuk game engine]
    ├── game-runner-config.json               [pengaturan konfigurasi game runner]
    ├── game-runner-jar-with-dependencies.jar [.jar untuk game runner]
    ├── run.bat                               [batch file untuk memulai permainan
```

## Penggunaan
**[RECOMMENDED]** Apabila ingin melakukan _build_ menggunakan Maven, disarankan untuk menggunakan IntelliJ IDEA (versi yang digunakan saat pengembangan adalah Ultimate 2021.2.1)

1. Buka direktori `src` menggunakan IntelliJ IDEA
2. Tunggu hingga _indexing_ selesai, akan muncul opsi untuk melakukan build (IDE akan membaca `pom.xml` untuk mendeteksi _Maven builds_). Pilih `Load...`
3. _Window Maven_ akan muncul. Pilih opsi `java-starter-bot` -> `Lifecycle` -> `Install`
4. Tunggu hingga proses _build_ selesai
5. Jalankan permainan dengan menjalankan `run.bat`. Hasil permainan akan disimpan di folder `match-logs`.

## Identitas
- <a href = "https://github.com/LordGedelicious">Gede Prasidha Bhawarnawa (13520004)</a>, bertanggung jawab atas perancangan algoritma dan pembuatan dokumen
- <a href = "https://github.com/Adityapnn811">Aditya Prawira Nugroho (13520049)</a>, bertanggung jawab atas perancangan program dan _debugging_
- <a href = "https://github.com/clumsyyyy">Owen Christian Wijaya (13520124)</a>, bertanggung jawab atas perancangan program dan _debugging_
