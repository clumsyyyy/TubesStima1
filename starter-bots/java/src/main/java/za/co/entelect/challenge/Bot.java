package za.co.entelect.challenge;               

import za.co.entelect.challenge.command.*;
import za.co.entelect.challenge.entities.*;
import za.co.entelect.challenge.enums.PowerUps;
import za.co.entelect.challenge.enums.Terrain;

import java.util.*;

import static java.lang.Math.max;

public class Bot {

    // ========== INISIALISASI VARIABEL PRIVATE ==========
    private static final int maxSpeed = 9;
    private List<Command> directionList = new ArrayList<>();

    private Random random;
    private GameState gameState;
    private Car opponent;
    private Car myCar;

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
    // TODO: algoritma buat nentuin tempat yang pas buat pake tweetnya

    private final static Command USE_BOOST = new BoostCommand();
    private final static Command USE_OIL = new OilCommand();
    private final static Command USE_LIZARD = new LizardCommand();
    private final static Command USE_EMP = new EmpCommand();
    private final static Command FIX = new FixCommand();

    // ini bagian public
    // user-defined constructor
    // TODO: opponent gimana maksudnya?
    public Bot(Random random, GameState gameState) {
        this.random = random;
        this.gameState = gameState;
        this.myCar = gameState.player;
        this.opponent = gameState.opponent;

        directionList.add(TURN_LEFT);
        directionList.add(TURN_RIGHT);
    }


    // INI YANG DIKOTAK KATIK
    public Command run() {

        //inisialisasi list blocks yang bisa dilihat di depan mobil kita
        //ket. p = player, o = opponent
        List<Object> pBlocks = getBlocksInFront(myCar.position.lane, myCar.position.block);
        //List<Object> oBlocks = getBlocksInFront(opponent.position.lane, opponent.position.block);
        List<Object> pNextBlocks = pBlocks.subList(0,1);
        //List<Object> oNextBlocks = oBlocks.subList(0,1);
        if(myCar.damage == 5) {
            return FIX;
        }
        // kalau damage mobil >= 5, langsung baikin
        // karena kalo damage >= 5, mobil langsung gabisa gerak

        if (myCar.speed <= 3){
            return ACCELERATE;
        }

        if (myCar.damage >= 5){
            return FIX;
        }

        // algoritma sederhana pengecekan apakah ada mud di depan / ada wall di depan
        // .contains(ELMT) dipake untuk tau apakah di dalem list ada ELMT tersebut
        if (pBlocks.contains(Terrain.MUD) || pNextBlocks.contains(Terrain.WALL)){
            if (hasPowerUp(PowerUps.LIZARD, myCar.powerups)){
                return USE_LIZARD;
            }
            if (pNextBlocks.contains(Terrain.MUD) || pNextBlocks.contains(Terrain.WALL)){
                if (myCar.position.lane == 1){          //kalau misalnya di lane 1, turn right biar ga minus
                    return TURN_RIGHT;
                } else if (myCar.position.lane == 4){   //kalau misalnya di lane 4, turn left biar ga minus
                    return TURN_LEFT;
                } else {                                //kalau misalnya ngga di situ, bebas
                    return directionList.get(random.nextInt(directionList.size()));
                }
            }
        }

        // boost kalo nganggur
        if (hasPowerUp(PowerUps.BOOST, myCar.powerups)){
            return USE_BOOST;
        }

        // aggression, sementara ngambil dari ref bot, nanti tambahin tweet di sini
        if (myCar.speed == maxSpeed) {
            if (hasPowerUp(PowerUps.OIL, myCar.powerups)) {
                return USE_OIL;
            }
            if (hasPowerUp(PowerUps.EMP, myCar.powerups)) {
                return USE_EMP;
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
        for (int i = max(block - startBlock, 0); i <= block - startBlock + Bot.maxSpeed; i++) {
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
}
