package com.github.dr.rwserver.command;

public class GameTimeLapse {
//    private GameTimeLapse(){
//        Events.on(EventType.GameStartEvent.class,()->{
//            Log.clog("开始游戏计时...");
//            this.start=System.currentTimeMillis();
//            this.stage=0;
//        });
//        Events.on(EventType.GameOverEvent.class,()->{
//            Log.clog("开始战役室计时...");
//            this.start=System.currentTimeMillis();
//        });
//        Log.clog("开始战役室计时...");
//    }
//    public static GameTimeLapse curr=new GameTimeLapse();
//    private long start=System.currentTimeMillis();
//    private byte stage;
//
//    public static long getStartTime(){
//        return curr.start;
//    }
//
//    public void update(){
//        gameOverCheck();
//        int lap= (int) (System.currentTimeMillis()-start);
//        if(Data.game.isStartGame){
//            if(lap>(stage+1)*5000*60) {
//                stage= (byte) (lap/300000);
//                Data.playerGroup.each(e -> e.sendSystemMessage("游戏已进行"+lap/60000+"分钟"));
//                Log.clog("游戏已进行"+lap/60000+"分钟");
//            }
//            KongZhi.broadCast(KongZhi.gameStateInfo());
//        }
//    }
//
//    public void refresh(){
//        Threads.removeScheduledFutureData("play-time");
//        Log.clog("游戏计时任务开始");
//        Threads.newThreadService2(this::update,1,10, TimeUnit.SECONDS,"play-time");
//    }
//
//    public void gameOverCheck(){
//        if(Data.game.isStartGame&&Data.playerGroup.size()==0) {
//            Log.clog("GameTimeLapse:结束游戏");
//            Events.fire(new EventType.GameOverEvent());
//        }
//    }
}
