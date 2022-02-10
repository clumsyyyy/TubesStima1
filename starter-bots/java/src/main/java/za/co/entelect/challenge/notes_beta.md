ALGORITMA OMEGA AGRESIF!
Memprioritaskan penggunaan powerup ofensif, baru kecepatan mobil
Harapannya memberikan damage ke mobil lawan dan memaksa mereka terkena speed limit
List obstacles : MUD/OIL, Mobil Lawan, WALL

Alur Cara Kerja Algoritmanya :

Note : conditional yg numpuk atas bawah artinya OR

Arah pemrosesannya algoritmanya selalu dari atas ke bawah (sekuensial) :

"Range Depan Mobil" = myCar.position.block + 1 sampai myCar.position.block + myCar.speed
"Range Accelerate Depan Mobil" = myCar.position.block + 1 sampai myCar.position.block + myCar.NextSpeedState
"Range Boost Depan Mobil" = myCar.position.block + 1 sampai myCar.position.block + BOOST_SPEED
"Jarak Antar Mobil" = | myCar.position.block - enemyCar.position.block |
"Left Lane Range" = myCar.position.lane - 1, myCar.position.block SAMPAI myCar.position.block + myCar.speed - 1
"Right Lane Range" = myCar.position.lane + 1, myCar.position.block SAMPAI myCar.position.block + myCar.speed - 1
"Left Lane Block" = myCar.position.lane - 1, myCar.position.block + myCar.speed - 1
"Right Lane Block" = myCar.position.lane + 1, myCar.position.block + myCar.speed - 1
"Current Lane Block" = myCar.position.lane, myCar.position.block + myCar.speed
"Accelerate Block" = myCar.position.lane, myCar.position.block + myCar.NextSpeed
"Decelerate Block" = myCar.position.lane, myCar.position.block + myCar.PreviousSpeed
"Boost Block" = myCar.position.lane, myCar.position.block + 15

"CanGoLeft" = myCar.position.lane - 1 IN (1,5)
"CanGoRight" = myCar.position.lane + 1 IN (1,5)
"CanGoLeft" dan "CanGoRight" adalah boolean

Kalau "Range Depan Mobil" kosong:
    Kalau mobil 1 lane dengan lawan :
        Kalau mobil di depan lawan :
            Kalau 1 <= myCar.damage <= 3 dan "Jarak Antar Mobil" >=  enemyCar.speed :
                <FIX> ##kurang tau apakah nanti lebih efektif ato engga
            Kalau myCar.damage > 3 :
                <FIX>
            Kalau ada OIL :
                <USE_OIL>
            Kalau ada TWEET :
                <USE_TWEET myCar.position.lane enemyCar.position.block + 1>
            Kalau "Current Lane Block" ada PowerUp:
                <NOTHING> // jalan dengan kecepatan yg sama
            Kalau "Left Lane Block" ada PowerUp dan "Left Lane Range" kosong dan "CanGoLeft": ##pasti bisa go left kalo ada power up
                <TURN_LEFT>
            Kalau "Right Lane Block" ada PowerUp dan "Right Lane Range" kosong dan "CanGoRight":
                <TURN_RIGHT>
            Kalau ada BOOST dan "Range Boost Depan Mobil" kosong :
            Kalau "Boost Block" ada PowerUp :
                <USE_BOOST>
            Kalau "Range Accelerate Depan Mobil" kosong :
            Kalau "Accelerate Block" ada PowerUp :
                <ACCELERATE>
            Kalau "Decelerate Block" ada PowerUp dan "Jarak Antar Mobil" > enemyCar.speed dan enemyCar.speed < myCar.speed: ##kayanya gaperlu decelerate deh buat ambil power up
                <DECELERATE>
        Kalau mobil di belakang lawan :
            Kalau 1 <= myCar.damage <= 2 dan "Jarak Antar Mobil" <= myCar.speed : // asumsi bisa nyusul setelah diperbaikin
                <FIX>
            Kalau myCar.damage > 2 :
                <FIX>
            Kalau ada TWEET : 
                <USE_TWEET myCar.position.lane enemyCar.position.block + 1>
            Kalau ada EMP :
                <USE_EMP>
            Kalau ada BOOST dan "Jarak Antar Mobil" > BOOST_SPEED dan "Range Boost Depan Mobil" kosong :
                <USE_BOOST>
            Kalau "Jarak Antar Mobil" > myCar.NextSpeed dan "Range Accelerate Depan Mobil" kosong :
                <ACCELERATE>
            Kalau "Current Lane Block" ada PowerUp :
                <NOTHING>
            Kalau "Left Lane Block" ada PowerUp dan "CanGoLeft": 
            Kalau "Left Lane Range" kosong dan "CanGoLeft": // biar bisa accelerate habis itu tanpa collision
                <TURN_LEFT>
            Kalau "Right Lane Block" ada PowerUp dan "CanGoRight": 
            Kalau "Right Lane Range" kosong dan "CanGoRight": // sama kyk yg left
                <TURN_RIGHT>
    Kalau mobil beda lane dengan lawan :
        Kalau mobil di depan lawan :
            Kalau 1 <= myCar.damage <= 3 dan "Jarak Antar Mobil" >=  enemyCar.speed :
                <FIX>
            Kalau myCar.damage > 3 :
                <FIX>
            Kalau ada TWEET :
                <USE_TWEET enemyCar.position.lane enemyCar.position.block + 1>
            Kalau punya OIL, lawan ada di kiri mobil, dan "Left Lane Range" kosong dan "CanGoLeft":
                <TURN_LEFT>
            Kalau punya OIL, lawan ada di kanan mobil, dan "Right Lane Range" kosong dan "CanGoRight":
                <TURN_RIGHT>
            Kalau ada BOOST dan "Range Boost Depan Mobil" kosong : ## antara kosong atau obstacle nya dikit, soalnya lane kosong rare kalo dari match sbelom sbelomnya
                <USE_BOOST>
            Kalau "Range Accelerate Depan Mobil" kosong :
            Kalau "Accelerate Block" ada PowerUp :
                <ACCELERATE>
            Kalau "Decelerate Block" ada PowerUp dan "Jarak Antar Mobil" > enemyCar.speed dan enemyCar.speed < myCar.speed:
                <DECELERATE>
            Kalau lawan ada di kiri mobil, dan "Left Lane Range" kosong dan "CanGoLeft": // biar ngehalangin mobil lawan
                <TURN_LEFT>
            Kalau lawan ada di kanan mobil, dan "Right Lane Range" kosong dan "CanGoRight":
                <TURN_RIGHT>
        Kalau mobil di belakang lawan : 
        // prioritasin ngeperlambat mobil make powerup, kalo ga baru nyusul lawan, baru ambil powerup
            Kalau 1 <= myCar.damage <= 2 dan "Jarak Antar Mobil" <= myCar.speed : // asumsi bisa nyusul setelah diperbaikin
                <FIX>
            Kalau myCar.damage > 2 :
                <FIX>
            Kalau ada TWEET : 
                <USE_TWEET myCar.position.lane enemyCar.position.block + 1>
            Kalau ada EMP dan lane lawan ada di range (-1, 0, 1) dari myCar.position.lane : #sama kek gw alognya
                <USE_EMP>
            Kalau ada BOOST dan "Jarak Antar Mobil" > BOOST_SPEED dan "Range Boost Depan Mobil" kosong :
                <USE_BOOST>
            Kalau "Jarak Antar Mobil" > myCar.position.block + myCar.NextSpeed dan "Range Accelerate Depan Mobil" kosong :
                <ACCELERATE>
            Kalau "Current Lane Block" ada PowerUp :
                <NOTHING>
            Kalau "Left Lane Block" ada PowerUp dan lawan tidak di kiri mobil dan "CanGoLeft": # kalo ada power up di seblah udah pasti bisa ke kiri/kanan ga si? ato belom tentu?
                <TURN_LEFT>
            Kalau "Right Lane Block" ada PowerUp dan lawan tidak di kanan mobil dan "CanGoRight": 
                <TURN_RIGHT>     
Kalau "Range Depan Mobil" ada obstacle :
// prioritaskan pengunaan lizard, baru mengelak. 
// semisalnya gbs mengelak, pilih obstacle yang paling tidak merugikan dengan urutan : mud/oil -> wall
// berlaku untuk kasus mobil 1 lane atau beda lane dengan lawan dan untuk kasus mobil di depan atau belakang lawan:
        Kalau ada LIZARD :
            <USE_LIZARD>
        Kalau "Left Lane Block" ada PowerUp dan "Left Lane Range" kosong :
            <TURN_LEFT>
        Kalau "Right Lane Block" ada PowerUp dan "Right Lane Range" kosong :
            <TURN_RIGHT>
        Kalau "Right Lane Range" dan "Left Lane Range" kosong dan "CanGoRight" atau "CanGoLeft":
            RANDOM <TURN_LEFT> OR <TURN_RIGHT>
            (pilih jalur yang lebih banyak powerup atau lebih menguntungkan powerupnya dengan urutan tweet -> lizard -> boost -> oil, tapi jujur belum terlalu kebayang algonya, ini opsional aja deh) ## antara banyak powerup ato obstacle terdikit
        Kalau "Left Lane Range" kosong dan "Right Lane Range" tidak kosong dan "CanGoLeft":
            <TURN_LEFT>
        Kalau "Left Lane Range" tidak kosong dan "Right Lane Range" kosong dan "CanGoRight":
            <TURN_RIGHT>
        Kalau "Left Lane Range" dan "Right Lane Range" tidak kosong :
            (Simpan jumlah MUD/OIL dan WALL untuk Left Lane, Current Lane, dan Right Lane)
            (Catat juga "CanGoRight" dan "CanGoLeft")
            (Pilih Lane dengan urutan persyaratan pemilihannya :)
            (1. Tidak ada WALL)
            (2. Kalau semua ada WALL, pilih yang WALLnya paling sedikit)
            (3. Kalau beberapa ada yang imbang WALLnya, pilih yang paling sedikit MUD/OILnya)
            (4. Kalau ada beberapa kandidat, prioritaskan Current Lane)
            (5. Khusus untuk kasus dimana lawan di depan dan beda lane dengan mobil kita, prioritaskan Lane yang tidak terdapat lawan. Antisipasi lawan menjatuhkan OIL)


## Buat fixing aku agak ragu kalo range 1 sampe 3, takutnya habis fix, jalan nabrak lagi, fix lagi lgsg, jadi banyak berhenti