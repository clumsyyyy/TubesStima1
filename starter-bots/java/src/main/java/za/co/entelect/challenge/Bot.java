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
    private static final int maxSpeed = 9;
    private static final int trackLength = 500;
    private Random random;
    private GameState gameState;
    private Car opponent;
    private Car myCar;
    private List<Command> directionList = new ArrayList<>();
    private final Helper h;
    private final Comparison c;
    // inisialisasi commands sesuai urutan di game-rules.md

    // nothing / accel / decel
    private final static Command NOTHING = new DoNothingCommand();
    private final static Command ACCELERATE = new AccelerateCommand();
    private final static Command DECELERATE = new DecelerateCommand();

    // belok kiri / kanan
    private final static Command TURN_LEFT = new ChangeLaneCommand(-1);
    private final static Command TURN_RIGHT = new ChangeLaneCommand(1);

    // power ups
    // [IMPORTANT] instance tweet gapunya default constructor,
    // jadi ngecallnya pas mau pake aja

    private final static Command USE_BOOST = new BoostCommand();
    private final static Command USE_OIL = new OilCommand();
    private final static Command USE_LIZARD = new LizardCommand();
    private final static Command USE_EMP = new EmpCommand();
    private final static Command FIX = new FixCommand();

    // ini bagian public
    // user-defined constructor
    public Bot(Random random, GameState gameState) {
        this.random = random;
        this.gameState = gameState;
        this.myCar = gameState.player;
        this.opponent = gameState.opponent;
        this.h = new Helper(this.myCar, this.gameState);
        this.c = new Comparison(gameState);
        directionList.add(TURN_LEFT);
        directionList.add(TURN_RIGHT);
    }


    public Command run() {
        Car myCar = gameState.player;
        Car opponent = gameState.opponent;
        //inisialisasi list blocks yang bisa dilihat di depan mobil kita
        //ket. p = player, o = opponent

        List <Object> leftLane = new ArrayList<>();
        List <Object> rightLane = new ArrayList <Object>();
        List <Object> currentLane = c.getBlocksInFront(myCar.position.lane, myCar.position.block, myCar.speed);
        // inisialisasi next blocks (sesuai car speed atau selisih)
        List <Object> pNextBlocks = currentLane;
        if (currentLane.size() >= min(myCar.speed, (trackLength - myCar.position.block + 1))) {
//            pNextBlocks = currentLane.subList(0, myCar.speed + 1);
            pNextBlocks = currentLane;
        }

        // left lane dan right lane diinisialisasi apabila bisa diinisialisasi
        List <Object> pNextBlocksLeft = pNextBlocks, pNextBlocksRight = pNextBlocks;
        if (myCar.position.lane > 1){
            leftLane = c.getBlocksInFront(myCar.position.lane - 1, myCar.position.block - 1, myCar.speed);
            if (leftLane.size() >= min(myCar.speed, (trackLength - myCar.position.block + 1))) {
                pNextBlocksLeft = leftLane;
            }
        }
        if (myCar.position.lane < 4){
            rightLane = c.getBlocksInFront(myCar.position.lane + 1, myCar.position.block - 1, myCar.speed);
            if (rightLane.size() >= min(myCar.speed, (trackLength - myCar.position.block + 1))) {
                pNextBlocksRight = rightLane;
            }
        }

        List <Object> boostLane = c.getBlocksInFront(myCar.position.lane, myCar.position.block, 15);
        List <Object> accelLane = c.getBlocksInFront(myCar.position.lane, myCar.position.block,  h.nextSpeedState(myCar));

        // implementasi algoritma kalau di depan lane kosong / tidak ada obstacle
        String choice = h.compareLanes(myCar, pNextBlocksLeft, pNextBlocks, pNextBlocksRight);

        // TODO
        // Deskripsi perubahan : rework semua algo sesui notes_commented.md
        // Buat baseline aja, soalnya kemarin yang dipindahin sama owen ga semua
        // Plus fix teabagging

        if (myCar.damage >= 2 && !nearFinish(currentLane, myCar)){
            return FIX;
        }

        if (myCar.speed <= 3 && !nearFinish(currentLane, myCar)){
            if (h.Obstacles(pNextBlocks) <= 1 && myCar.damage == 0) {
                return ACCELERATE;
            } else {
                return switching(choice, pNextBlocks, pNextBlocksLeft, pNextBlocksRight);
            }
        }

        // algoritma jika lane kosong
        if (h.Obstacles(currentLane) == 0){
            if (myCar.position.lane == opponent.position.lane){
                //jika mobil musuh ada di lane yang sama dengan kita, coba main agresif
                return sameLaneCommand(choice, myCar, opponent, currentLane, pNextBlocks, pNextBlocksLeft, pNextBlocksRight);
            } else {
                //jika mobil musuh beda, algonya beda
                return diffLaneCommand(choice, myCar, opponent, currentLane, pNextBlocks, pNextBlocksLeft, pNextBlocksRight);
            }
        } else {
            // algoritma apabila ada obstacles
            // ini keknya gaperlu karena udah dicek di atas
//            if (h.hasPowerUp(PowerUps.BOOST, myCar.powerups) && h.Obstacles(currentLane) < 10 && !myCar.boosting
//                    && Math.abs(myCar.position.block - opponent.position.block) <= 20){
//                if (h.Obstacles(pNextBlocksLeft) < h.Obstacles(currentLane) || h.Obstacles(pNextBlocksRight) < h.Obstacles(currentLane)) {
//                    return switching(choice, pNextBlocks, pNextBlocksLeft, pNextBlocksRight);
//                }
//                return USE_BOOST;
//            }
            // tambahin case baru buat dia ngesummon EMP
            if (myCar.damage < 3 && h.Obstacles(currentLane) < 3 && h.hasPowerUp(PowerUps.EMP, myCar.powerups)
                    && Math.abs(myCar.position.lane - opponent.position.lane) <= 1 && opponent.position.block > myCar.position.block) {
                return USE_EMP;
            }

            if (h.hasPowerUp(PowerUps.BOOST, myCar.powerups) && h.Obstacles(currentLane) < 10 && !myCar.boosting) {
                if (h.Obstacles(pNextBlocksLeft) <= h.Obstacles(currentLane) || h.Obstacles(pNextBlocksRight) <= h.Obstacles(currentLane)) {
                    return switching(choice, pNextBlocks, pNextBlocksLeft, pNextBlocksRight);
                }
                return USE_BOOST;
            }

            // algoritma sederhana pengecekan apakah ada mud di depan / ada wall di depan
            // .contains(ELMT) dipake untuk tau apakah di dalem list ada ELMT tersebut

            // TODO Note : bagian kode yg ini gw hilangin untuk kode di bawah
            //         else if ((!currentLane.contains(Terrain.WALL) || h.obstacleLandingBlock(pNextBlocks) != 3) && myCar.damage == 0) {
            //              return ACCELERATE;
            //         }
            if (pNextBlocks.contains(Terrain.MUD) || pNextBlocks.contains(Terrain.WALL)
                    || pNextBlocks.contains(Terrain.OIL_SPILL) || h.hasCyberTruck(0)) {
                if (h.hasPowerUp(PowerUps.LIZARD, myCar.powerups) && h.obstacleLandingBlock(pNextBlocks) == 0) {
                    return USE_LIZARD;
                } else {
                    return switching(choice, pNextBlocks, pNextBlocksLeft, pNextBlocksRight);
                }
            }

        }
        return ACCELERATE;
    }

    // ========== INISIALISASI FUNGSI HELPER DI SINI ==========

    // fungsi ngecek dia udah deket finish atau belum
    private boolean nearFinish (List<Object> CurrLane, Car myCar) {
        int i = 0;
        boolean finish = false;
        while (i < myCar.speed && !finish) {
            if (CurrLane.get(i).equals(Terrain.FINISH)) {
                finish = true;
            } else {
                i++;
            }
        }
        return finish;
    }

    // return apakah blocks yang akan dilewati ronde itu mengandung objek yg kita cari
    private boolean passThroughPowUp(List <Object> Lane, PowerUps powerUp) {
        int i = 0;
        boolean found = false;
        while (i < Lane.size() && !found) {
            if (Lane.get(i).equals(powerUp)) {
                found = true;
            } else {
                i += 1;
            }
        }
        return found;
    }

    private Command sameLaneCommand(String choice, Car myCar, Car opponent, List<Object> currentLane, List<Object> pNextBlocks
            , List <Object> pNextBlockLeft, List <Object> pNextBlockRight){
        int no_accelerate = h.Obstacles(c.getBlocksInFront(myCar.position.lane, myCar.position.block, myCar.speed));
        int with_accelerate = h.Obstacles(c.getBlocksInFront(myCar.position.lane, myCar.position.block,  h.nextSpeedState(myCar)));
        int with_boost = h.Obstacles(c.getBlocksInFront(myCar.position.lane, myCar.position.block, h.currentMaxSpeed(myCar)));
        // sengaja dibuat dua kasus terpisah soalnya urutan prioritasnya beda
        if (myCar.position.block >= opponent.position.block) {
            // kalau lane mobil kita sama dengan lane musuh dan kita punya oil, pake
            if (h.hasPowerUp(PowerUps.OIL, myCar.powerups)) {
                return USE_OIL;
            }
            // algo tweet, kalau misalnya powerup on dan lane musuhnya gada apa", kita ganggu
            if (h.hasPowerUp(PowerUps.TWEET, myCar.powerups)){
                return new TweetCommand(opponent.position.lane, opponent.position.block + opponent.speed + 1);
            }

            if (h.hasPowerUp(PowerUps.BOOST, myCar.powerups) && h.Obstacles(currentLane) < 10 && !myCar.boosting) {
                return USE_BOOST;
            }

/*            if (with_accelerate < 10) {
                return ACCELERATE;
            }*/

        } else { // kalau mobilnya di belakang
            if (h.hasPowerUp(PowerUps.EMP, myCar.powerups) && (opponent.position.block > myCar.position.block)) {
                return USE_EMP;
            }

            // algo tweet, kalau misalnya powerup on dan lane musuhnya gada apa", kita ganggu
            if (h.hasPowerUp(PowerUps.TWEET, myCar.powerups)){
                return new TweetCommand(opponent.position.lane, opponent.position.block + opponent.speed + 1);
            }

            if (h.hasPowerUp(PowerUps.BOOST, myCar.powerups) && h.Obstacles(currentLane) < 10 && (opponent.position.block - myCar.position.block) > 15 && !myCar.boosting) {
                return USE_BOOST;
            }

            if (with_accelerate < 10) {
                return ACCELERATE;
            }
        }

        // TODO nah yang bawah ini jujur gw bingung comment atau engga, soalnya sameLaneCommand kan cuma dipake kalau h.obstacles(currentLane) == 0

        // if (pNextBlocks.contains(Terrain.MUD) || pNextBlocks.contains(Terrain.WALL)
        //         || pNextBlocks.contains(Terrain.OIL_SPILL) || h.hasCyberTruck(0)){
        //     if (h.hasPowerUp(PowerUps.LIZARD, myCar.powerups) && h.obstacleLandingBlock(pNextBlocks) == 0){
        //         return USE_LIZARD;
        //     } else if ((!currentLane.contains(Terrain.WALL) || h.obstacleLandingBlock(pNextBlocks) != 3)
        //             && myCar.damage == 0 && passThroughPowUp(currentLane, PowerUps.BOOST)) { // TODO
        //         return ACCELERATE;
        //     } else {
        //         return switching(choice, pNextBlocks, pNextBlockLeft, pNextBlockRight);
        //     }
        // }

        return ACCELERATE;
    }


    private Command diffLaneCommand(String choice, Car myCar, Car opponent, List<Object> currentLane, List<Object> pNextBlocks
            , List <Object> pNextBlockLeft, List <Object> pNextBlockRight){
        int no_accelerate = h.Obstacles(c.getBlocksInFront(myCar.position.lane, myCar.position.block, myCar.speed));
        int with_accelerate = h.Obstacles(c.getBlocksInFront(myCar.position.lane, myCar.position.block,  h.nextSpeedState(myCar)));
        int with_boost = h.Obstacles(c.getBlocksInFront(myCar.position.lane, myCar.position.block, h.currentMaxSpeed(myCar)));
        if (myCar.position.block >= opponent.position.block) {
            if (h.hasPowerUp(PowerUps.TWEET, myCar.powerups)){
                return new TweetCommand(opponent.position.lane, opponent.position.block + opponent.speed + 1);
            }
            // buat antisipasi EMP
            if (Math.abs(myCar.position.lane - opponent.position.lane) == 2 && (myCar.position.lane != 1 || myCar.position.lane != 4)) {
                switch (myCar.position.lane) {
                    case 2:
                        return c.compareTwoLanes(-1, trackLength);
                    case 3:
                        return c.compareTwoLanes(1, trackLength);
                    default:
                        break;
                }
            }
            if (h.hasPowerUp(PowerUps.BOOST, myCar.powerups) && with_boost < 10 && !myCar.boosting) {
                return USE_BOOST;
            }
/*            if (with_accelerate < 10) {
                return ACCELERATE;
            }*/

        } else {
            if (h.hasPowerUp(PowerUps.EMP, myCar.powerups)){
                if (opponent.position.lane <= myCar.position.lane + 1 && opponent.position.lane >= myCar.position.lane - 1
                        && opponent.position.block > myCar.position.block){
                    return USE_EMP;
                }
            }
            if (h.hasPowerUp(PowerUps.BOOST, myCar.powerups) && with_boost < 10 && !myCar.boosting && Math.abs(myCar.position.block - opponent.position.block) < h.currentMaxSpeed(myCar)) {
                return USE_BOOST;
            }
            if (with_accelerate < 10) {
                return ACCELERATE;
            }
        }

        // TODO dicomment dengan alasan yang sama dgn sameLaneCommand

        // if (pNextBlocks.contains(Terrain.MUD) || pNextBlocks.contains(Terrain.WALL)
        //         || pNextBlocks.contains(Terrain.OIL_SPILL) || h.hasCyberTruck(0)){
        //     if (h.hasPowerUp(PowerUps.LIZARD, myCar.powerups) && h.obstacleLandingBlock(pNextBlocks) == 0){
        //         return USE_LIZARD;
        //     } else if ((!currentLane.contains(Terrain.WALL) || h.obstacleLandingBlock(pNextBlocks) != 3)
        //             && myCar.damage == 0 && passThroughPowUp(currentLane, PowerUps.BOOST)) { //
        //         return ACCELERATE;
        //     } else {
        //         return switching(choice, pNextBlocks, pNextBlockLeft, pNextBlockRight);
        //     }
        // }

        return ACCELERATE;
    }

    // ngubah switching biar dia ga cuma liat landingnya aja di fungsi %ObstacleBlock, tapi full 1 path yang bakal dilewatin mobilnya
    // ngubah switching biar dia ga cuma liat landingnya aja di fungsi %ObstacleBlock, tapi full 1 path yang bakal dilewatin mobilnya
    private Command switching(String choice, List <Object> pNextBlock, List <Object> pNextBlockLeft, List <Object> pNextBlockRight){
        int no_accelerate = h.Obstacles(c.getBlocksInFront(myCar.position.lane, myCar.position.block, myCar.speed));
        int with_accelerate = h.Obstacles(c.getBlocksInFront(myCar.position.lane, myCar.position.block,  h.nextSpeedState(myCar)));
        int with_boost = h.Obstacles(c.getBlocksInFront(myCar.position.lane, myCar.position.block, h.currentMaxSpeed(myCar)));


        int leftObstacleBlock = 100;
        int leftPowerUpCount = 0;

        int rightObstacleBlock = 100;
        int rightPowerUpCount = 0;

        int currObstacleBlock = h.Obstacles(pNextBlock);
        int currPowerUpCount = h.countPowerUps(c.getBlocksInFront(myCar.position.lane, myCar.position.block, myCar.speed));

        if (myCar.position.lane > 1) {
            leftObstacleBlock = h.Obstacles(pNextBlockLeft);
            leftPowerUpCount = h.countPowerUps(c.getBlocksInFront(myCar.position.lane - 1, myCar.position.block, myCar.speed));
        }
        if (myCar.position.lane < 4) {
            rightObstacleBlock = h.Obstacles(pNextBlockRight);
            // tambahin ini soalnya sebelumnya gaada, makanya dia kadang2 suka ga belok kanan
            rightPowerUpCount = h.countPowerUps(c.getBlocksInFront(myCar.position.lane + 1, myCar.position.block, myCar.speed));
        }

        switch (choice) {
            case "TURN_LEFT":
                return TURN_LEFT;
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
            case "TURN_RIGHT":
                return TURN_RIGHT;
            case "CURR_LEFT":
                // bandingin powerup yang ada di kiri dan tengah
                // kalau sama jenisnya, cek dulu mendingan ngebut atau engga
                if ((currObstacleBlock <= leftObstacleBlock) || (currPowerUpCount >= leftPowerUpCount)){
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
                // bandingin powerup yang ada di kanan dan tengah
                // kalau sama jenisnya, cek dulu mendingan ngebut atau engga
                if ((currObstacleBlock <= rightObstacleBlock) || (currPowerUpCount >= rightPowerUpCount)){
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
            case "ALL":
                // bandingin powerup yang ada di kiri dan tengah
                // kalau sama jenisnya, cek dulu mendingan ngebut atau engga
                int min = h.min3(leftObstacleBlock, currObstacleBlock, rightObstacleBlock);
                int max = h.max3(currPowerUpCount, leftPowerUpCount, rightPowerUpCount);
                if (currObstacleBlock == min
                        || currPowerUpCount == max) {
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
                } else if (leftObstacleBlock == min
                        || leftPowerUpCount == max) {
                    if (myCar.speed == 0) {
                        return ACCELERATE;
                    } else {
                        return TURN_LEFT;
                    }
                } else if (rightObstacleBlock == min
                        || rightPowerUpCount == max) {
                    if (myCar.speed == 0) {
                        return ACCELERATE;
                    } else {
                        return TURN_RIGHT;
                    }
                }
            default:
                if (!myCar.boosting && h.hasPowerUp(PowerUps.BOOST, myCar.powerups) && with_boost <= with_accelerate) {
                    return USE_BOOST;
                } else {
                    if (myCar.speed == 0) {
                        return ACCELERATE;
                    } else {
                        return NOTHING;
                    }
                }
        }
    }
}
