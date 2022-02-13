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
        List <Object> pNextBlocks;
        List <Object> leftLane = new ArrayList<>();
        List <Object> rightLane = new ArrayList <Object>();
        List <Object> currentLane = c.getBlocksInFront(myCar.position.lane, myCar.position.block, myCar.speed);

        // inisialisasi next blocks (sesuai car speed atau selisih)
        if (currentLane.size() >= min(myCar.speed, trackLength - myCar.position.block + 1)) {
            pNextBlocks = currentLane.subList(0, min(myCar.speed, trackLength - myCar.position.block + 1) + 1);
        } else {
            pNextBlocks = currentLane;
        }

        // left lane dan right lane diinisialisasi apabila bisa diinisialisasi
        if (myCar.position.lane > 1){
            leftLane = c.getBlocksInFront(myCar.position.lane - 1, myCar.position.block, myCar.speed);
        }
        if (myCar.position.lane < 4){
            rightLane = c.getBlocksInFront(myCar.position.lane + 1, myCar.position.block, myCar.speed);
        }

        List <Object> boostLane = c.getBlocksInFront(myCar.position.lane, myCar.position.block, 15);
        List <Object> accelLane = c.getBlocksInFront(myCar.position.lane, myCar.position.block,  h.nextSpeedState(myCar));

        // implementasi algoritma kalau di depan lane kosong / tidak ada obstacle
        String choice = h.compareLanes(myCar, leftLane, currentLane, rightLane);

        if (myCar.damage >= 2){
            return FIX;
        }

        if (myCar.speed <= 3){
            return ACCELERATE;
        }

        // algoritma jika lane kosong
        if (h.Obstacles(currentLane) == 0){
            if (myCar.position.lane == opponent.position.lane){
                //jika mobil musuh ada di lane yang sama dengan kita, coba main agresif
                return sameLaneCommand(choice, myCar, opponent, currentLane, pNextBlocks);
            } else {
                //jika mobil musuh beda, algonya beda
                return diffLaneCommand(choice, myCar, opponent, currentLane, pNextBlocks);
            }
        } else {
            // algoritma apabila ada obstacles
            if (myCar.damage >= 2){
                return FIX;
            }

            if (myCar.speed <= 3){
                return ACCELERATE;
            }

            if (h.hasPowerUp(PowerUps.BOOST, myCar.powerups)){
                return USE_BOOST;
            }


            // algoritma sederhana pengecekan apakah ada mud di depan / ada wall di depan
            // .contains(ELMT) dipake untuk tau apakah di dalem list ada ELMT tersebut
            if (currentLane.contains(Terrain.MUD) || currentLane.contains(Terrain.WALL)
                    || currentLane.contains(Terrain.OIL_SPILL) || h.hasCyberTruck(0) != -1) {
                if (h.hasPowerUp(PowerUps.LIZARD, myCar.powerups) && h.obstacleLandingBlock("CENTER") == 0){
                    return USE_LIZARD;
                } else if ((!currentLane.contains(Terrain.WALL) || h.obstacleLandingBlock("CENTER") != 3)
                        && myCar.damage == 0 && passThroughPowUp(currentLane, PowerUps.BOOST)) {
                    return ACCELERATE;
                } else {
                    return switching(choice);
                }
            }

            if (h.hasPowerUp(PowerUps.EMP, myCar.powerups)){
                if (opponent.position.lane <= myCar.position.lane + 1 && opponent.position.lane >= myCar.position.lane - 1
                        && opponent.position.block > myCar.position.block){
                    return USE_EMP;
                }
            }

            // algo tweet, kalau misalnya powerup on dan lane musuhnya gada apa", kita ganggu
            if (h.hasPowerUp(PowerUps.TWEET, myCar.powerups)){
                return new TweetCommand(opponent.position.lane, opponent.position.block + opponent.speed + 1);
            }

            return ACCELERATE;
        }
    }

    // ========== INISIALISASI FUNGSI HELPER DI SINI ==========

    // return apakah blocks yang akan dilewati ronde itu mengandung objek yg kita cari
    private boolean passThroughPowUp(List <Object> Lane, PowerUps powerUp) {
        int i = 0;
        boolean found = false;
        while (i < myCar.speed && !found) {
            if (Lane.get(i).equals(powerUp)) {
                found = true;
            } else {
                i += 1;
            }
        }
        return found;
    }

    private Command sameLaneCommand(String choice, Car myCar, Car opponent, List <Object> currentLane, List <Object> pNextBlocks){
        if (myCar.damage >= 2){
            return FIX;
        }

        if (myCar.speed <= 3){
            return ACCELERATE;
        }

        if (h.hasPowerUp(PowerUps.BOOST, myCar.powerups)){
            return USE_BOOST;
        }

        if (currentLane.contains(Terrain.MUD) || currentLane.contains(Terrain.WALL)
                || currentLane.contains(Terrain.OIL_SPILL) || h.hasCyberTruck(0) != -1){
            if (h.hasPowerUp(PowerUps.LIZARD, myCar.powerups) && h.obstacleLandingBlock("CENTER") == 0){
                return USE_LIZARD;
            } else if ((!currentLane.contains(Terrain.WALL) || h.obstacleLandingBlock("CENTER") != 3)
                    && myCar.damage == 0 && passThroughPowUp(currentLane, PowerUps.BOOST)) {
                return ACCELERATE;
            } else {
                return switching(choice);
            }
        }


        if (h.hasPowerUp(PowerUps.BOOST, myCar.powerups) && h.Obstacles(currentLane) < 10 && !myCar.boosting) {
            return USE_BOOST;
        }

        // algo tweet, kalau misalnya powerup on dan lane musuhnya gada apa", kita ganggu
        if (h.hasPowerUp(PowerUps.TWEET, myCar.powerups)){
            return new TweetCommand(opponent.position.lane, opponent.position.block + opponent.speed + 1);
        }

        if (h.hasPowerUp(PowerUps.EMP, myCar.powerups)){
            if (opponent.position.lane <= myCar.position.lane + 1 && opponent.position.lane >= myCar.position.lane - 1
                    && opponent.position.block > myCar.position.block){
                return USE_EMP;
            }
        }

        // kalau lane mobil kita sama dengan len musuh dan kita punya oil, pake
        if (h.hasPowerUp(PowerUps.OIL, myCar.powerups)
                && myCar.position.block > opponent.position.block){
            return USE_OIL;
        }

        return ACCELERATE;
    }


    private Command diffLaneCommand(String choice, Car myCar, Car opponent, List <Object> currentLane, List <Object> pNextBlocks){
        if (myCar.damage >= 2){
            return FIX;
        }
        if (myCar.speed <= 3){
            return ACCELERATE;
        }

        if (h.hasPowerUp(PowerUps.BOOST, myCar.powerups)){
            return USE_BOOST;
        }

        if (currentLane.contains(Terrain.MUD) || currentLane.contains(Terrain.WALL)
                || currentLane.contains(Terrain.OIL_SPILL) || h.hasCyberTruck(0) != -1){
            if (h.hasPowerUp(PowerUps.LIZARD, myCar.powerups) && h.obstacleLandingBlock("CENTER") == 0){
                return USE_LIZARD;
            } else if ((!currentLane.contains(Terrain.WALL) || h.obstacleLandingBlock("CENTER") != 3)
                    && myCar.damage == 0 && passThroughPowUp(currentLane, PowerUps.BOOST)) {
                return ACCELERATE;
            } else {
                return switching(choice);
            }
        }

        if (h.hasPowerUp(PowerUps.BOOST, myCar.powerups) && h.Obstacles(currentLane) < 10 && !myCar.boosting
                && Math.abs(myCar.position.block - opponent.position.block) <= 20){
            return USE_BOOST;
        }

        if (h.hasPowerUp(PowerUps.TWEET, myCar.powerups)){
            return new TweetCommand(opponent.position.lane, opponent.position.block + opponent.speed + 1);
        }

        if (h.hasPowerUp(PowerUps.EMP, myCar.powerups)){
            if (opponent.position.lane <= myCar.position.lane + 1 && opponent.position.lane >= myCar.position.lane - 1
                    && opponent.position.block > myCar.position.block){
                return USE_EMP;
            }
        }

        // kalau lane mobil kita sama dengan lane musuh dan kita punya oil, pake
        if (myCar.position.lane == opponent.position.lane && h.hasPowerUp(PowerUps.OIL, myCar.powerups)
                && myCar.position.block > opponent.position.block){
            return USE_OIL;
        }

        return ACCELERATE;
    }

    private Command switching(String choice){
        int no_accelerate = h.Obstacles(c.getBlocksInFront(myCar.position.lane, myCar.position.block, myCar.speed));
        int with_accelerate = h.Obstacles(c.getBlocksInFront(myCar.position.lane, myCar.position.block,  h.nextSpeedState(myCar)));
        int leftLandingBlock = 100;
        int leftObstacleBlock = 100;
        int leftObstacleCount = 100;
        int leftPowerUpCount = 0;

        int rightLandingBlock = 100;
        int rightObstacleBlock = 100;
        int rightObstacleCount = 100;
        int rightPowerUpCount = 0;

        int currLandingBlock = h.LaneBlock("CENTER");
        int currObstacleBlock = h.obstacleLandingBlock("CENTER");
        int currObstacleCount = h.Obstacles(c.getBlocksInFront(myCar.position.lane, myCar.position.block, myCar.speed)
                .subList(0, min(myCar.speed, trackLength - myCar.position.block + 1)));
        int currPowerUpCount = h.countPowerUps(c.getBlocksInFront(myCar.position.lane, myCar.position.block, myCar.speed)
                .subList(0, min(myCar.speed, trackLength - myCar.position.block + 1)));

        if (myCar.position.lane > 1){
            leftLandingBlock = h.LaneBlock("LEFT");
            leftObstacleBlock = h.obstacleLandingBlock("LEFT");
            leftPowerUpCount = h.countPowerUps(c.getBlocksInFront(myCar.position.lane - 1, myCar.position.block, myCar.speed)
                    .subList(0, min(myCar.speed, trackLength - myCar.position.block + 1)));
            leftObstacleCount = h.Obstacles(c.getBlocksInFront(myCar.position.lane - 1, myCar.position.block, myCar.speed)
                    .subList(0, min(myCar.speed, trackLength - myCar.position.block + 1)));
        }

        if (myCar.position.lane < 4) {
            rightLandingBlock = h.LaneBlock("RIGHT");
            rightObstacleBlock = h.obstacleLandingBlock("RIGHT");
            rightPowerUpCount = h.countPowerUps(c.getBlocksInFront(myCar.position.lane + 1, myCar.position.block, myCar.speed)
                    .subList(0, min(myCar.speed, trackLength - myCar.position.block + 1)));
            leftObstacleCount = h.Obstacles(c.getBlocksInFront(myCar.position.lane + 1, myCar.position.block, myCar.speed)
                    .subList(0, min(myCar.speed, trackLength - myCar.position.block + 1)));
        }

        int max = 0;
        int min = 0;
        switch (choice) {
            case "TURN_LEFT":
                return TURN_LEFT;
            case "STAY":
                if (with_accelerate <= no_accelerate) {
                    if (!myCar.boosting){
                        return ACCELERATE;
                    } else {
                        if (h.hasPowerUp(PowerUps.BOOST, myCar.powerups)){
                            return USE_BOOST;
                        }
                    }
                } else {
                    return NOTHING;
                }
            case "TURN_RIGHT":
                return TURN_RIGHT;
            case "CURR_LEFT":
                // bandingin powerup yang ada di kiri dan tengah
                // kalau sama jenisnya, cek dulu mendingan ngebut atau engga
                max = max(leftPowerUpCount, currPowerUpCount);
                min = min(leftObstacleCount, currObstacleCount);
                if ((max == currPowerUpCount && max != 100) || (min == currObstacleCount && min != 0)){
                    if (with_accelerate <= no_accelerate) {
                        if (!myCar.boosting){
                            return ACCELERATE;
                        } else {
                            if (h.hasPowerUp(PowerUps.BOOST, myCar.powerups)){
                                return USE_BOOST;
                            }
                        }
                    } else {
                        return NOTHING;
                    }
                } else {
                    return TURN_LEFT;
                }
            case "CURR_RIGHT":
                // bandingin powerup yang ada di kanan dan tengah
                // kalau sama jenisnya, cek dulu mendingan ngebut atau engga
                max = max(rightPowerUpCount, currPowerUpCount);
                min = min(rightObstacleCount, currObstacleCount);
                if ((max == currPowerUpCount && max != 100) || (min == currObstacleCount && min != 0)){
                    if (with_accelerate <= no_accelerate) {
                        if (!myCar.boosting){
                            return ACCELERATE;
                        } else {
                            if (h.hasPowerUp(PowerUps.BOOST, myCar.powerups)){
                                return USE_BOOST;
                            }
                        }
                    } else {
                        return NOTHING;
                    }
                } else {
                    return TURN_RIGHT;
                }
            case "ALL":
                // bandingin powerup yang ada di kiri dan tengah
                // kalau sama jenisnya, cek dulu mendingan ngebut atau engga
                max = h.max3(currPowerUpCount, leftPowerUpCount, rightPowerUpCount);
                min = h.min3(currObstacleCount, leftObstacleCount, rightObstacleCount);
                if ((max == currPowerUpCount && max != 100) || (min == currObstacleCount && min != 0)){
                    if (with_accelerate <= no_accelerate) {
                        if (!myCar.boosting){
                            return ACCELERATE;
                        } else {
                            if (h.hasPowerUp(PowerUps.BOOST, myCar.powerups)){
                                return USE_BOOST;
                            }
                        }
                    } else {
                        return NOTHING;
                    }
                } else if ((max == leftPowerUpCount && max != 100) || (min == leftObstacleCount && min != 0)){
                    return TURN_LEFT;
                } else if ((max == rightPowerUpCount && max != 100) || (min == rightObstacleCount && min != 0)){
                    return TURN_RIGHT;
                }
            default:
                if (!myCar.boosting){
                    return ACCELERATE;
                } else {
                    if (h.hasPowerUp(PowerUps.BOOST, myCar.powerups)){
                        return USE_BOOST;
                    }
                }
                return NOTHING;
        }
    }
}
