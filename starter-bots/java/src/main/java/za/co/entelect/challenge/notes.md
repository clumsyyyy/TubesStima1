ALGORITMA OMEGA AGRESIF!
Memprioritaskan penggunaan powerup ofensif, baru kecepatan mobil
Harapannya memberikan damage ke mobil lawan dan memaksa mereka terkena speed limit

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

2nd Edit : Saran dari adit, karena kecil kemungkinan ada jalur kosong, maka gpp boost tapi dgn syarat ga terlalu sakit damagenya
HANYA LAKUKAN FIX SAAT TIDAK BOOSTING

Kalau "Range Depan Mobil" kosong:
    Kalau mobil 1 lane dengan lawan :
        Kalau mobil di depan lawan :
            Kalau boosting = true :
                (pilih lane yang paling sedikit damagenya)
            Kalau 1 <= myCar.damage <= 3 dan "Jarak Antar Mobil" >=  enemyCar.speed :
                <FIX>
            Kalau myCar.damage > 3 :
                <FIX>
            Kalau ada OIL :
                <USE_OIL>
            Kalau ada TWEET :
                <USE_TWEET myCar.position.lane enemyCar.position.block + 1>
            Kalau "Current Lane Block" ada PowerUp:
                <NOTHING> // jalan dengan kecepatan yg sama
            Kalau "Left Lane Block" ada PowerUp dan "Left Lane Range" kosong :
                <TURN_LEFT>
            Kalau "Right Lane Block" ada PowerUp dan "Right Lane Range" kosong :
                <TURN_RIGHT>
            Kalau ada BOOST dan "Range Boost Depan Mobil" kosong :
            Kalau "Boost Block" ada PowerUp :
                <USE_BOOST>
            Kalau "Range Accelerate Depan Mobil" kosong :
            Kalau "Accelerate Block" ada PowerUp :
                <ACCELERATE>
            Kalau "Decelerate Block" ada PowerUp dan "Jarak Antar Mobil" > enemyCar.speed dan enemyCar.speed < myCar.speed:
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
            Kalau "Jarak Antar Mobil" > myCar.position.block + myCar.NextSpeed dan "Range Accelerate Depan Mobil" kosong :
                <ACCELERATE>
            Kalau "Current Lane Block" ada PowerUp :
                <NOTHING>
            Kalau "Left Lane Block" ada PowerUp : 
            Kalau "Left Lane Range" kosong : // biar bisa accelerate habis itu tanpa collision
                <TURN_LEFT>
            Kalau "Right Lane Block" ada PowerUp : 
            Kalau "Right Lane Range" kosong: // sama kyk yg left
                <TURN_RIGHT>
    Kalau mobil beda lane dengan lawan :
        Kalau mobil di depan lawan :
            Kalau 1 <= myCar.damage <= 3 dan "Jarak Antar Mobil" >=  enemyCar.speed :
                <FIX>
            Kalau myCar.damage > 3 :
                <FIX>
            Kalau ada TWEET :
                <USE_TWEET enemyCar.position.lane enemyCar.position.block + 1>
            Kalau punya OIL, lawan ada di kiri mobil, dan "Left Lane Range" kosong :
                <TURN_LEFT>
            Kalau punya OIL, lawan ada di kanan mobil, dan "Right Lane Range" kosong :
                <TURN_RIGHT>
            Kalau ada BOOST dan "Range Boost Depan Mobil" kosong :
                <USE_BOOST>
            Kalau "Range Accelerate Depan Mobil" kosong :
            Kalau "Accelerate Block" ada PowerUp :
                <ACCELERATE>
            Kalau "Decelerate Block" ada PowerUp dan "Jarak Antar Mobil" > enemyCar.speed dan enemyCar.speed < myCar.speed:
                <DECELERATE>
            Kalau lawan ada di kiri mobil, dan "Left Lane Range" kosong : // biar ngehalangin mobil lawan
                <TURN_LEFT>
            Kalau lawan ada di kanan mobil, dan "Right Lane Range" kosong :
                <TURN_RIGHT>
        Kalau mobil di belakang lawan :
            Kalau 1 <= myCar.damage <= 2 dan "Jarak Antar Mobil" <= myCar.speed : // asumsi bisa nyusul setelah diperbaikin
                <FIX>
            Kalau myCar.damage > 2 :
                <FIX>
            
            
            
            
            