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
    private Random random;
    private GameState gameState;
    private Car opponent;
    private Car myCar;
    private List<Command> directionList = new ArrayList<>();

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

        directionList.add(TURN_LEFT);
        directionList.add(TURN_RIGHT);
    }


    // TODO: algo ganti lane kalau mau dapetin powerup (KERJAAN GEDE) + fungsi warning
    public Command run() {
        Car myCar = gameState.player;
        Car opponent = gameState.opponent;
        //inisialisasi list blocks yang bisa dilihat di depan mobil kita
        //ket. p = player, o = opponent

        List<Object> pBlocks = getBlocksInFront(myCar.position.lane, myCar.position.block);
        List<Object> pNextBlocks = pBlocks.subList(0, min(myCar.speed, 1500 - myCar.position.block));

        // kalau damage mobil >= 5, langsung baikin
        // karena kalo damage >= 5, mobil langsung gabisa gerak
        if (myCar.damage >= 4){
            return FIX;
        }
        if (myCar.speed <= 3){
            return ACCELERATE;
        }
        // kalo obstacle di 20 lane setelahnya cuma ada 1 ato kurang, boost aja
        if (Obstacles(pBlocks) <= 1) {
            if (hasPowerUp(PowerUps.BOOST, myCar.powerups)){
                return USE_BOOST;
            }
        }

        // algoritma sederhana pengecekan apakah ada mud di depan / ada wall di depan
        // .contains(ELMT) dipake untuk tau apakah di dalem list ada ELMT tersebut
        if (pNextBlocks.contains(Terrain.MUD) || pNextBlocks.contains(Terrain.WALL) || pNextBlocks.contains(Terrain.OIL_SPILL)){
            if (hasPowerUp(PowerUps.LIZARD, myCar.powerups)){
                return USE_LIZARD;
            } else if (!pNextBlocks.contains(Terrain.WALL) && myCar.damage <= 3 && passThroughPowUp(pNextBlocks, PowerUps.BOOST)) {
                return ACCELERATE;
            } else {
                if (myCar.position.lane == 1){          //kalau misalnya di lane 1, turn right biar ga minus
                    return TURN_RIGHT;
                } else if (myCar.position.lane == 4){   //kalau misalnya di lane 4, turn left biar ga minus
                    return TURN_LEFT;
                } else {                                //kalau misalnya ngga di situ, bebas
                    return compareObstacles();
                    //return directionList.get(random.nextInt(directionList.size()));
                }
            }
        }

        //  TODO: =============== IMPLEMENTASI ALGO GEDE ===============
        //  TODO: IMPLEMENTASI OIL DIBENERIN fixed, FUNGSI BASIC AVOIDCANCE
        // boost kalo nganggur dan di depan free

        // algo tweet, kalau misalnya powerup on dan lane musuhnya gada apa", kita ganggu
        if (hasPowerUp(PowerUps.TWEET, myCar.powerups)){
            return new TweetCommand(opponent.position.lane, opponent.position.block);
        }

        // kalau lane mobil kita sama dengan len musuh dan kita punya oil, pake dan kita di depan
        if (myCar.position.lane == opponent.position.lane && hasPowerUp(PowerUps.OIL, myCar.powerups) && myCar.position.block > opponent.position.block){
            return USE_OIL;
        }

        // algo emp
        if (hasPowerUp(PowerUps.EMP, myCar.powerups)){
            if (opponent.position.lane <= myCar.position.lane + 1 && opponent.position.lane >= myCar.position.lane - 1 && opponent.position.block > myCar.position.block){
                return USE_EMP;
            } else {
                if (myCar.position.lane == 1){          //kalau misalnya di lane 1, turn right biar ga minus
                    return TURN_RIGHT;
                } else if (myCar.position.lane == 4){   //kalau misalnya di lane 4, turn left biar ga minus
                    return TURN_LEFT;
                } else {                                //kalau misalnya ngga di situ, bebas
                    //return directionList.get(random.nextInt(directionList.size()));
                    return compareObstacles();
                    //bisa dicoba ganti pake compareObstacles()
                }
            }
        }
        // kalo di depan ga ada masalah apa-apa
        // g a s
        return ACCELERATE;
    }



    // ========== INISIALISASI FUNGSI HELPER DI SINI ==========
    /**
     * Returns map of blocks and the objects in the for the current lanes, returns the amount of blocks that can be
     * traversed at max speed.
     **/
    private List<Object> getBlocksInFront(int lane, int block) {
        List<Lane[]> map = gameState.lanes;
        List<Object> blocks = new ArrayList<>();
        int startBlock = map.get(0)[0].position.block;

        Lane[] laneList = map.get(lane - 1);
        for (int i = max(block - startBlock, 0); i <= block - startBlock + 15; i++) {
            if (laneList[i] == null || laneList[i].terrain == Terrain.FINISH) {
                break;
            }

            blocks.add(laneList[i].terrain);

        }
        return blocks;
    }
    // fungsi untuk mengecek apakah powerup yang mau dipakai ada di list

    private Boolean hasPowerUp(PowerUps powerUpToCheck, PowerUps[] available) {
        for (PowerUps powerUp: available) {
            if (powerUp.equals(powerUpToCheck)) {
                return true;
            }
        }
        return false;
    }

    private int Obstacles(List<Object> Lane) {
        int count = 0;
        for (int i = 0; i < Lane.size(); i++) {
            if (Lane.get(i).equals(Terrain.MUD) || Lane.get(i).equals(Terrain.WALL) ||
                    Lane.get(i).equals(Terrain.OIL_SPILL)){
                count++;
            }
        }
        return count;
    }
    // car lane yang jumlah obstaclesnya paling dikit
    // CALL KALAU DIA GA DI LANE 1 ATAU 4
    // TODO: pake ini kok malah kalah ya? terlalu defensif kah?

    private Command compareObstacles(){
        int Lcount = Obstacles(getBlocksInFront(myCar.position.lane - 1, myCar.position.block).subList(0, min(myCar.speed, 1500 - myCar.position.block)));
        int Ccount = Obstacles(getBlocksInFront(myCar.position.lane, myCar.position.block).subList(0, min(myCar.speed, 1500 - myCar.position.block)));
        int Rcount = Obstacles(getBlocksInFront(myCar.position.lane + 1, myCar.position.block).subList(0, min(myCar.speed, 1500 - myCar.position.block)));
        if (Lcount <= Ccount && Lcount <= Rcount) {
            return TURN_LEFT;
        } else if (Rcount <= Ccount && Rcount <= Lcount) {
            return TURN_RIGHT;
        } else {
            if (hasPowerUp(PowerUps.BOOST, myCar.powerups)) {
                return USE_BOOST;
            } else {
                return ACCELERATE;
            }
        }
    }

    // return apakah blocks yang akan dilewati ronde itu mengandung objek yg kita cari
    private boolean passThroughPowUp(List<Object> Lane, PowerUps powerUp) {
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

}


