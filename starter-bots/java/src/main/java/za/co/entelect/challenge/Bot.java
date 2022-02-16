package za.co.entelect.challenge;

import za.co.entelect.challenge.command.*;
import za.co.entelect.challenge.entities.*;
import za.co.entelect.challenge.enums.PowerUps;
import za.co.entelect.challenge.enums.Terrain;

import java.util.*;

import static java.lang.Math.max;
import static java.lang.Math.min;

public class Bot {

    // ========== INISIALISASI VARIABEL PRIVATE ==========
    private static final int trackLength = 500;
    private final GameState gameState;
    private final Car opponent;
    private final Car myCar;
    private final Helper h;

    private final static Command NOTHING = new DoNothingCommand();
    private final static Command ACCELERATE = new AccelerateCommand();
    private final static Command TURN_LEFT = new ChangeLaneCommand(-1);
    private final static Command TURN_RIGHT = new ChangeLaneCommand(1);
    private final static Command USE_BOOST = new BoostCommand();
    private final static Command USE_OIL = new OilCommand();
    private final static Command USE_LIZARD = new LizardCommand();
    private final static Command USE_EMP = new EmpCommand();
    private final static Command FIX = new FixCommand();

    // ========== CONSTRUCTOR ==========
    public Bot(GameState gameState) {
        this.gameState = gameState;
        this.myCar = gameState.player;
        this.opponent = gameState.opponent;
        this.h = new Helper(this.myCar, this.gameState);
    }

    /*
    Fungsi run dipanggil di Main.java sebagai basis pengambilan keputusan. Keputusan yang diambil adalah
    keputusan terbaik yang dapat diambil di dalam suatu ronde, tanpa mempertimbangkan keadaan ronde sebelumnya
    atau ronde setelahnya. Hal ini dilakukan sebagai implementasi algoritma greedy, yang mengambil keputusan
    yang bersifat optimum lokal (keputusan terbaik pada suatu saat)
     */

    public Command run() {
        // inisialisasi variabel dari gameState
        Car myCar = gameState.player;
        Car opponent = gameState.opponent;

        /*
        Pemanggilan fungsi getBlocksInFront untuk mendapatkan list of lanes pada lane kiri, kanan, dan tengah.
        Inisialisasi lane kiri dan/atau kanan dilakukan apabila memungkinkan, yaitu pada kondisi apabila
        lokasi lane mobil di antara 1 dan 4 (tidak di kiri ataupun di kanan).
         */
        
        List <Object> currentLane = h.getBlocksInFront(myCar.position.lane, myCar.position.block, myCar.speed);
        List <Object> leftLane = currentLane;
        List <Object> rightLane = currentLane;
        if (myCar.position.lane > 1){
            leftLane = h.getBlocksInFront(myCar.position.lane - 1, myCar.position.block, myCar.speed);
            leftLane.remove(leftLane.size() - 1);
        }
        if (myCar.position.lane < 4){
            rightLane = h.getBlocksInFront(myCar.position.lane + 1, myCar.position.block , myCar.speed);
            rightLane.remove(rightLane.size() - 1);
        }
        if (currentLane.size() != 0) {
            currentLane.remove(0);
        }


        /*
        Variabel choice digunakan sebagai penanda kemungkinan lane yang dapat diakses mobil.
         */
        
        String choice = h.compareLanes();
        System.out.println("lane: " + myCar.position.lane);
        System.out.println("block: " + myCar.position.block);
        System.out.println("choice: " + choice);


        /*
        Algoritma mengutamakan perbaikan mobil apabila damage mobil sama dengan atau lebih besar dari 2
        dan ada power-up boost, sehingga mobil dapat mencapai kecepatan tertinggi. Fix juga akan dilakukan
        apabila mobil mempunyai damage di atas 2.
        Selain itu, mobil juga akan mengutamakan switching lane
         */
        
        if (myCar.damage >= 2 && (!nearFinish(currentLane, myCar) || h.hasPowerUp(PowerUps.BOOST, myCar.powerups))){
            return FIX;
        } else if (myCar.damage > 2) {
            return FIX;
        }

        if (myCar.speed <= 3 && !nearFinish(currentLane, myCar)){
            if (h.Obstacles(currentLane, 0) <= 1 && myCar.damage == 0) {
                System.out.println("enter switching line 99");
                return avoiding(choice, currentLane, leftLane, rightLane);
            }
        }

        /*
        Algoritma dibagi menjadi dua segmen, yaitu apabila lane sedang kosong dan tidak kosong.
        Algoritma pada lane kosong dibagi menjadi algoritma saat musuh berada dalam lane yang sama
        atau berbeda.
         */
        
        if (h.Obstacles(currentLane, 0) == 0){
            if (myCar.position.lane == opponent.position.lane){
                // pemanggilan fungsi sameLaneCommand(...) untuk lane musuh == lane mobil
                return sameLaneCommand(choice, myCar, opponent, currentLane, leftLane, rightLane);
            } else {
                // pemanggilan fungsi diffLaneCommand(...) untuk lane musuh != lane mobil;=
                return diffLaneCommand(choice, myCar, opponent, currentLane, leftLane, rightLane);
            }
        } else {
            /*
            Mobil akan menggunakan EMP apabila memungkinkan. Untuk menghindari collison, mobil dapat melakukan
            manuver berpindah ke kiri / kanan.
             */
            if (myCar.damage < 3 && h.Obstacles(currentLane, 0) < 3
                    && h.hasPowerUp(PowerUps.EMP, myCar.powerups)
                    && Math.abs(myCar.position.lane - opponent.position.lane) <= 1
                    && opponent.position.block > myCar.position.block) {
                if (Math.abs(myCar.position.block - opponent.position.block) > myCar.speed) {
                    return USE_EMP;
                } else {
                    System.out.println("enter switching line 130");
                    return avoiding("LEFT_RIGHT", currentLane, leftLane, rightLane);
                }
            }

            /*
            Apabila ada powerup boost namun memungkinkan untuk pindah, mobil akan pindah terlebih dahulu. Jika tidak
            baru boost digunakan.
             */
            
            if (h.hasPowerUp(PowerUps.BOOST, myCar.powerups)
                    && h.Obstacles(currentLane, 0) < 10
                    && !myCar.boosting) {
                if (h.Obstacles(leftLane, -1) <= h.Obstacles(currentLane, 0)
                        || h.Obstacles(rightLane, 1) <= h.Obstacles(currentLane, 0)) {
                    System.out.println("enter switching line 139");
                    return avoiding(choice, currentLane, leftLane, rightLane);
                }
                return USE_BOOST;
            }

            /*
            Algoritma juga akan menangani kasus apabila ada rintangan. Apabila memungkinkan untuk menggunakan
            power-up LIZARD dan blok tujuan bukanlah blok rintangan, maka mobil akan menggunakan perintah
            USE_LIZARD. Sebaliknya, mobil akan berusaha menghindar.
             */
            
            if (currentLane.contains(Terrain.MUD) || currentLane.contains(Terrain.WALL)
                    || currentLane.contains(Terrain.OIL_SPILL) || h.hasCyberTruck(0) != -1) {
                if (h.hasPowerUp(PowerUps.LIZARD, myCar.powerups) && h.obstacleLandingBlock(currentLane) == 0) {
                    return USE_LIZARD;
                } else {
                    System.out.println("enter switching line 141");
                    return avoiding(choice, currentLane, leftLane, rightLane);
                }
            }

        }
        return ACCELERATE;
    }



    // Fungsi validasi apakah mobil berada di dekat garis finis
    private boolean nearFinish (List<Object> CurrLane, Car myCar) {
        int i = 0;
        boolean finish = false;
        while (i < CurrLane.size() && !finish) {
            if (CurrLane.get(i).equals(Terrain.FINISH)) {
                finish = true;
            } else {
                i++;
            }
        }
        return finish;
    }

    // fungsi untuk melakukan algoritma jika mobil musuh berada di lane yang sama
    private Command sameLaneCommand(String choice, Car myCar, Car opponent, List<Object> currentLane,
                                    List <Object> pNextBlockLeft, List <Object> pNextBlockRight){
        System.out.println("enter sameLaneCommand");
        int with_accelerate = h.Obstacles(h.getBlocksInFront(myCar.position.lane, myCar.position.block,  h.nextSpeedState(myCar)), 0);

        /*
        Apabila mobil berada di depan mobil musuh, mobil akan berusaha untuk bermain agresif
        dengan menggunakan power-ups yang dimiliki. Jika tidak, mobil akan menggunakan power-up yang dimiliki
        untuk memperlambat mobil musuh
         */
        if (myCar.position.block >= opponent.position.block) {
            if (h.hasPowerUp(PowerUps.OIL, myCar.powerups)) {
                return USE_OIL;
            }
            if (h.hasPowerUp(PowerUps.TWEET, myCar.powerups)){
                return new TweetCommand(opponent.position.lane, opponent.position.block + opponent.speed + 1);
            }

            if (h.hasPowerUp(PowerUps.BOOST, myCar.powerups) && h.Obstacles(currentLane, 0) < 10 && !myCar.boosting) {
                System.out.println("enter use boost line 207");
                return USE_BOOST;
            }
        } else {
            if (h.hasPowerUp(PowerUps.EMP, myCar.powerups) && (opponent.position.block > myCar.position.block)) {
                if (Math.abs(myCar.position.block - opponent.position.block) > myCar.speed) {
                    System.out.println("enter use emp line 218");
                    return USE_EMP;
                } else {
                    System.out.println("enter switching line 221");
                    return avoiding("LEFT_RIGHT", currentLane, pNextBlockLeft, pNextBlockRight);
                }
            }

            if (h.hasPowerUp(PowerUps.TWEET, myCar.powerups)){
                return new TweetCommand(opponent.position.lane, opponent.position.block + opponent.speed + 1);
            }

            if (h.hasPowerUp(PowerUps.BOOST, myCar.powerups) && h.Obstacles(currentLane, 0) < 10
                    && (opponent.position.block - myCar.position.block) > 15 && !myCar.boosting) {
                System.out.println("enter use boost 232");
                return USE_BOOST;
            }

            if (with_accelerate < 10) {
                System.out.println("enter accel 237");
                return ACCELERATE;
            }
        }
        System.out.println("default return for samelane");
        return avoiding(choice, currentLane, pNextBlockLeft, pNextBlockRight);
    }


    private Command diffLaneCommand(String choice, Car myCar, Car opponent, List<Object> currentLane,
                                    List <Object> pNextBlockLeft, List <Object> pNextBlockRight){
        System.out.println("enter diffLaneCommand");
        int with_accelerate = h.Obstacles(h.getBlocksInFront(myCar.position.lane, myCar.position.block,  h.nextSpeedState(myCar)), 0);
        int with_boost = h.Obstacles(h.getBlocksInFront(myCar.position.lane, myCar.position.block, h.currentMaxSpeed(myCar)), 0);
        
        /*
        Apabila mobil berada di depan musuh, mobil akan mengantisipasi EMP yang akan ditembakkan musuh dan mencoba
        menghindar dari obstacle. Sebaliknya, mobil juga akan berusaha untuk memperlambat musuh dengan
        power-up yang dimiliki.
         */
        if (myCar.position.block >= opponent.position.block) {
            if (h.hasPowerUp(PowerUps.TWEET, myCar.powerups)){
                return new TweetCommand(opponent.position.lane, opponent.position.block + opponent.speed + 1);
            }
            // buat antisipasi EMP
            if (Math.abs(myCar.position.lane - opponent.position.lane) == 2 && (myCar.position.lane != 1 || myCar.position.lane != 4)) {
                switch (myCar.position.lane) {
                    case 2:
                        return h.compareTwoLanes(currentLane, pNextBlockLeft, -1, trackLength);
                    case 3:
                        return h.compareTwoLanes(currentLane, pNextBlockRight, 1,  trackLength);
                    default:
                        break;
                }
            }

            if (currentLane.contains(Terrain.MUD) || currentLane.contains(Terrain.WALL)
                    || currentLane.contains(Terrain.OIL_SPILL) || h.hasCyberTruck(0) != -1) {
                if (h.hasPowerUp(PowerUps.LIZARD, myCar.powerups) && h.obstacleLandingBlock(currentLane) == 0) {
                    return USE_LIZARD;
                } else {
                    System.out.println("enter switching 273");
                    return avoiding(choice, currentLane, pNextBlockLeft, pNextBlockRight);
                }
            }
        } else {
            if (h.hasPowerUp(PowerUps.EMP, myCar.powerups)){
                if (opponent.position.lane <= myCar.position.lane + 1 && opponent.position.lane >= myCar.position.lane - 1
                        && opponent.position.block > myCar.position.block){
                    if (Math.abs(myCar.position.block - opponent.position.block) > myCar.speed) {
                        return USE_EMP;
                    } else {
                        System.out.println("enter switching line 293");
                        return avoiding("LEFT_RIGHT", currentLane, pNextBlockLeft, pNextBlockRight);
                    }
                }
            }
            if (h.hasPowerUp(PowerUps.BOOST, myCar.powerups) && with_boost < 10 && !myCar.boosting && Math.abs(myCar.position.block - opponent.position.block) < h.currentMaxSpeed(myCar)) {
                System.out.println("enter switching line 299");
                return USE_BOOST;
            }
            if (with_accelerate < 10) {
                System.out.println("enter switching line 303");
                return ACCELERATE;
            }
        }
        System.out.println("default return for difflane");
        return avoiding(choice, currentLane, pNextBlockLeft, pNextBlockRight);
    }

    /* 
        Algoritma pembanding lane untuk mengambil keputusan terkait lane terbaik yang dapat diambil.
        Perbandingan dilakukan dengan prioritas rintangan dan jumlah power-up. Apabila suatu lane mempunyai
        jumlah rintangan yang lebih sedikit dibandingkan lane lainnya, maka lane tersebut akan diambil. Apabila 
        lane tersebut mempunyai jumlah rintangan yang sama dengan lane lainnya, maka lane dengan jumlah power-up
        terbanyak yang akan diambil. Perbandingan dilakukan berdasarkan string choice dengan aturan:
        - CURR_LEFT = membandingkan lane yang ditempati dengan lane kiri
        - CURR_RIGHT = membandingkan lane yang ditempati dengan lane kanan
        - ALL = membandingkan lane kiri, kanan, dan lane yang ditempati
        - LEFT_RIGHT = membandingkan lane kiri dan kanan (dipanggil untuk menghindari collision saat EMP
        - STAY = tetap di lane
     */
    private Command avoiding(String choice, List <Object> pNextBlock, List <Object> pNextBlockLeft, List <Object> pNextBlockRight){
        int no_accelerate = h.Obstacles(h.getBlocksInFront(myCar.position.lane, myCar.position.block, myCar.speed), 0);
        int with_accelerate = h.Obstacles(h.getBlocksInFront(myCar.position.lane, myCar.position.block,  h.nextSpeedState(myCar)), 0);
        int with_boost = h.Obstacles(h.getBlocksInFront(myCar.position.lane, myCar.position.block, h.currentMaxSpeed(myCar)), 0);
        int leftObstacleCount = 100;
        int leftPowerUpCount = 0;

        int rightObstacleCount = 100;
        int rightPowerUpCount = 0;

        int currObstacleCount = h.Obstacles(pNextBlock, 0);
        int currPowerUpCount = h.countPowerUps(pNextBlock);

        if (myCar.position.lane > 1) {
            leftObstacleCount = h.Obstacles(pNextBlockLeft, -1);
            leftPowerUpCount = h.countPowerUps(pNextBlockLeft);
        }
        if (myCar.position.lane < 4) {
            rightObstacleCount = h.Obstacles(pNextBlockRight, 1);
            rightPowerUpCount = h.countPowerUps(pNextBlockRight);
        }
        System.out.println("leftObstacleCount: " + leftObstacleCount);
        System.out.println("rightObstacleCount: " + rightObstacleCount);
        System.out.println("currObstacleCount: " + currObstacleCount);
        System.out.println("currPowerUpCount: " + currPowerUpCount);
        System.out.println("leftPowerUpCount: " + leftPowerUpCount);
        System.out.println("rightPowerUpCount: " + rightPowerUpCount);

        switch (choice) {
            case "CURR_LEFT":
                if ((currObstacleCount < leftObstacleCount)
                        || ((currObstacleCount == leftObstacleCount) && (currPowerUpCount >= leftPowerUpCount))){
                    if (with_accelerate <= no_accelerate) {
                        if (!myCar.boosting && h.hasPowerUp(PowerUps.BOOST, myCar.powerups) && with_boost <= with_accelerate) {
                            return USE_BOOST;
                        } else {
                            return ACCELERATE;
                        }
                    } else {
                        if (myCar.speed == 0) {
                            return ACCELERATE;
                        } else {
                            return NOTHING;
                        }
                    }
                } else {
                    if (myCar.speed == 0) {
                        return ACCELERATE;
                    } else {
                        return TURN_LEFT;
                    }
                }
            case "CURR_RIGHT":
                if ((currObstacleCount < rightObstacleCount)
                        || ((currObstacleCount == rightObstacleCount)
                        && (currPowerUpCount >= rightPowerUpCount))){
                    if (with_accelerate <= no_accelerate) {
                        if (!myCar.boosting && h.hasPowerUp(PowerUps.BOOST, myCar.powerups) && with_boost <= with_accelerate) {
                            return USE_BOOST;
                        } else {
                            return ACCELERATE;
                        }
                    } else {
                        if (myCar.speed == 0) {
                            return ACCELERATE;
                        } else {
                            return NOTHING;
                        }
                    }
                } else {
                    if (myCar.speed == 0) {
                        return ACCELERATE;
                    } else {
                        return TURN_RIGHT;
                    }
                }
            case "LEFT_RIGHT":
                if (leftObstacleCount < rightObstacleCount
                || ((leftObstacleCount == rightObstacleCount) && (leftPowerUpCount >= rightPowerUpCount))){
                    if (myCar.speed == 0) {
                        return ACCELERATE;
                    } else {
                        return TURN_LEFT;
                    }
                } else {
                    if (myCar.speed == 0) {
                        return ACCELERATE;
                    } else {
                        return TURN_RIGHT;
                    }
                }
            case "ALL":
                if ((currObstacleCount < Math.min(leftObstacleCount, rightObstacleCount)
                        || ((currObstacleCount == Math.min(leftObstacleCount, rightObstacleCount))
                        && currPowerUpCount >= Math.max(leftPowerUpCount, rightPowerUpCount)))) {
                    if (with_accelerate <= no_accelerate) {
                        if (!myCar.boosting && h.hasPowerUp(PowerUps.BOOST, myCar.powerups) && with_boost <= with_accelerate) {
                            return USE_BOOST;
                        } else {
                            return ACCELERATE;
                        }
                    } else {
                        if (myCar.speed == 0) {
                            return ACCELERATE;
                        } else {
                            return NOTHING;
                        }
                    }
                }

                if ((leftObstacleCount < Math.min(currObstacleCount, rightObstacleCount)
                        || ((leftObstacleCount == Math.min(currObstacleCount, rightObstacleCount))
                        && (leftPowerUpCount >= Math.max(currPowerUpCount, rightPowerUpCount))))) {
                    if (myCar.speed == 0) {
                        return ACCELERATE;
                    } else {
                        return TURN_LEFT;
                    }
                }

                if ((rightObstacleCount < Math.min(currObstacleCount, leftObstacleCount))
                        || ((rightObstacleCount == Math.min(currObstacleCount, leftObstacleCount))
                        && (rightPowerUpCount >= Math.max(currPowerUpCount, leftPowerUpCount)))) {
                    if (myCar.speed == 0) {
                        return ACCELERATE;
                    } else {
                        return TURN_RIGHT;
                    }
                }
            case "STAY":
                if (with_accelerate <= no_accelerate) {
                    if (!myCar.boosting && h.hasPowerUp(PowerUps.BOOST, myCar.powerups) && with_boost <= with_accelerate) {
                        return USE_BOOST;
                    } else {
                        return ACCELERATE;
                    }
                } else {
                    if (myCar.speed == 0) {
                        return ACCELERATE;
                    } else {
                        return NOTHING;
                    }
                }
            default:
                if (!myCar.boosting && h.hasPowerUp(PowerUps.BOOST, myCar.powerups) && with_boost <= with_accelerate) {
                    return USE_BOOST;
                } else {
                    return ACCELERATE;
                }
        }
    }
}
