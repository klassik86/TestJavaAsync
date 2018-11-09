package com.kk.async;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;

import static com.kk.helpers.ThreadHelper.sleep;

// TODO: (padudin) rename 
public class CompletionStageTest1 {

    private static final Logger logger = LoggerFactory.getLogger(CompletionStageTest1.class);

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        CompletionStageTest1 test = new CompletionStageTest1();
//        test.start1();
//        test.start2SeqAsync();
//        test.start2SplitAsync();
//        test.start2SeqSync();
//        test.start2SplitSync();
        test.start2SplitDoubleInnerRunAsync();
        logger.debug("exit");
    }
    
    private void start2SeqAsync() {
        /*result:
        футур 1 запускается сразу, по окончанию его ждет 2й (т.е. ДО join),
        основной поток их отпустил (разделился от них и будет ждать их в future.toCompletableFuture().join() (для второго случая)

        CompletableFuture.completedFuture(null)
                .thenCompose(unused -> asyncStage("1"))  - запускается СРАЗУ (в момент объявления) и в НОВОМ потоке относительно MAIN, т.е. в join будем ждать окончание, если нужно
                .thenCompose(unused -> asyncStage("2")) - дожидается 1го asyncStage (т.е. выполняется с ним в одном потоке).

        Tue Feb 27 12:38:09 GST 2018: [ForkJoinPool.commonPool-worker-1] >> asyncStage > before sleep - 1
        Tue Feb 27 12:38:11 GST 2018: [ForkJoinPool.commonPool-worker-1] << asyncStage < after sleep - 1
        Tue Feb 27 12:38:11 GST 2018: [ForkJoinPool.commonPool-worker-1] >> asyncStage > before sleep - 2
        Tue Feb 27 12:38:13 GST 2018: [ForkJoinPool.commonPool-worker-1] << asyncStage < after sleep - 2
        Tue Feb 27 12:38:13 GST 2018: [ForkJoinPool.commonPool-worker-1] >> asyncStage > before sleep - 3
        Tue Feb 27 12:38:15 GST 2018: [ForkJoinPool.commonPool-worker-1] << asyncStage < after sleep - 3
        Tue Feb 27 12:38:15 GST 2018: [main] -----------
        Tue Feb 27 12:38:15 GST 2018: [ForkJoinPool.commonPool-worker-1] >> asyncStage > before sleep - 1
        Tue Feb 27 12:38:15 GST 2018: [main] >> join > before sleep - join
        Tue Feb 27 12:38:17 GST 2018: [ForkJoinPool.commonPool-worker-1] << asyncStage < after sleep - 1
        Tue Feb 27 12:38:17 GST 2018: [ForkJoinPool.commonPool-worker-1] >> asyncStage > before sleep - 2
        Tue Feb 27 12:38:19 GST 2018: [ForkJoinPool.commonPool-worker-1] << asyncStage < after sleep - 2
        Tue Feb 27 12:38:19 GST 2018: [ForkJoinPool.commonPool-worker-1] >> asyncStage > before sleep - 3
        Tue Feb 27 12:38:20 GST 2018: [main] << join < after sleep - join
        Tue Feb 27 12:38:20 GST 2018: [main] before join
        Tue Feb 27 12:38:21 GST 2018: [ForkJoinPool.commonPool-worker-1] << asyncStage < after sleep - 3
        Tue Feb 27 12:38:21 GST 2018: [main] exit

        * */
        logger.debug(">> start2SeqAsync");

        CompletableFuture.completedFuture(null)
                .thenCompose(unused -> asyncStage("1"))
                .thenCompose(unused -> asyncStage("2"))
                .thenCompose(unused -> asyncStage("3"))
        .toCompletableFuture().join();

        logger.debug("-----------");

        CompletableFuture<String> future = CompletableFuture.completedFuture(null)
                .thenCompose(unused -> asyncStage("1"))
                .thenCompose(unused -> asyncStage("2"))
                .thenCompose(unused -> asyncStage("3"));

        sleep("join", "join", 5000L);
        logger.debug("before join");
        future.toCompletableFuture().join();

        logger.debug("<< start2SeqAsync");
    }

    private void start2SplitAsync() {
        /*result:
        ВСЕ футуры запускается сразу параллеьно друг другу и ДО join,
        основной поток их отпустил (разделился от них и будет ждать их в future.toCompletableFuture().join() (в примере ниже футуры до вызова join выполнятся)

        CompletionStage<String> asyncStage1 = asyncStage("1"); - запускается СРАЗУ (в момент объявления) и в НОВОМ потоке [ForkJoinPool.commonPool-worker-1], т.е. в join будем ждать окончание, если нужно
        CompletionStage<String> asyncStage2 = asyncStage("2"); - запускается СРАЗУ (в момент объявления) и в НОВОМ потоке [ForkJoinPool.commonPool-worker-2], т.е. в join будем ждать окончание, если нужно
        ...

        Tue Feb 27 12:39:41 GST 2018: [ForkJoinPool.commonPool-worker-3] >> asyncStage > before sleep - 3
        Tue Feb 27 12:39:41 GST 2018: [ForkJoinPool.commonPool-worker-2] >> asyncStage > before sleep - 2
        Tue Feb 27 12:39:41 GST 2018: [ForkJoinPool.commonPool-worker-1] >> asyncStage > before sleep - 1
        Tue Feb 27 12:39:41 GST 2018: [main] >> join > before sleep - join
        Tue Feb 27 12:39:43 GST 2018: [ForkJoinPool.commonPool-worker-3] << asyncStage < after sleep - 3
        Tue Feb 27 12:39:43 GST 2018: [ForkJoinPool.commonPool-worker-1] << asyncStage < after sleep - 1
        Tue Feb 27 12:39:43 GST 2018: [ForkJoinPool.commonPool-worker-2] << asyncStage < after sleep - 2
        Tue Feb 27 12:39:46 GST 2018: [main] << join < after sleep - join
        Tue Feb 27 12:39:46 GST 2018: [main] before join
        Tue Feb 27 12:39:46 GST 2018: [main] exit

        * */

        logger.debug(">> start2SplitAsync");

        CompletionStage<String> asyncStage1 = asyncStage("1");
        CompletionStage<String> asyncStage2 = asyncStage("2");
        CompletionStage<String> asyncStage3 = asyncStage("3");

        sleep("join", "join", 5000L);
        logger.debug("before join");
        asyncStage1
                .thenCompose(unused -> asyncStage2)
                .thenCompose(unused -> asyncStage3)
        .toCompletableFuture().join();

        logger.debug("<< start2SplitAsync");
        // TODO: (padudin) проверить что на вход футуры будет подаваться результат другой футуры?
    }

    private void start2SeqSync() {
        /*result:
        футур 1 запускается сразу, по окончанию его ждет 2й (т.е. ДО join),
        футуры выполняются в MAIN потоке, т.е. не происходит выделения нового потока.

        Т.е. если запуск без Async, то
       CompletionStage<String> future = syncStage("1") - запускается СРАЗУ (в момент объявления) и в ТЕКУЩЕМ потоке, т.е. пока не дойдем до конца, не продолжим след строчку кода!
                .thenCompose(unused -> syncStage("2")) - запускается после выполнения syncStage("1") и в ТЕКУЩЕМ потоке, т.е. пока не дойдем до конца, не продолжим след строчку кода!

        *
        * Tue Feb 27 12:32:14 GST 2018: [main] >> syncStage > before sleep - 1
        Tue Feb 27 12:32:16 GST 2018: [main] << syncStage < after sleep - 1
        Tue Feb 27 12:32:16 GST 2018: [main] >> syncStage > before sleep - 2
        Tue Feb 27 12:32:18 GST 2018: [main] << syncStage < after sleep - 2
        Tue Feb 27 12:32:18 GST 2018: [main] >> syncStage > before sleep - 3
        Tue Feb 27 12:32:20 GST 2018: [main] << syncStage < after sleep - 3
        Tue Feb 27 12:32:20 GST 2018: [main] -----------
        Tue Feb 27 12:32:20 GST 2018: [main] >> syncStage > before sleep - 1
        Tue Feb 27 12:32:22 GST 2018: [main] << syncStage < after sleep - 1
        Tue Feb 27 12:32:22 GST 2018: [main] >> syncStage > before sleep - 2
        Tue Feb 27 12:32:24 GST 2018: [main] << syncStage < after sleep - 2
        Tue Feb 27 12:32:24 GST 2018: [main] >> syncStage > before sleep - 3
        Tue Feb 27 12:32:26 GST 2018: [main] << syncStage < after sleep - 3
        Tue Feb 27 12:32:26 GST 2018: [main] >> join > before sleep - join
        Tue Feb 27 12:32:31 GST 2018: [main] << join < after sleep - join
        Tue Feb 27 12:32:31 GST 2018: [main] before join
        Tue Feb 27 12:32:31 GST 2018: [main] exit
        * */

        logger.debug(">> start2SeqSync");

        syncStage("1")
                .thenCompose(unused -> syncStage("2"))
                .thenCompose(unused -> syncStage("3"))
                .toCompletableFuture().join();

        logger.debug("-----------");
        CompletionStage<String> future = syncStage("1")
                .thenCompose(unused -> syncStage("2"))
                .thenCompose(unused -> syncStage("3"));

        sleep("join", "join", 5000L);
        logger.debug("before join");
        future.toCompletableFuture().join();

        logger.debug("<< start2SeqSync");
    }


    private void start2SplitSync() {
        /*result:
        * Аналогично start2SeqSync.
        * футур 1 запускается сразу, по окончанию его ждет 2й (т.е. ДО join),
        футуры выполняются в MAIN потоке, т.е. не происходит выделения нового потока.

        CompletionStage<String> syncStage1 = syncStage("1");  - запускается СРАЗУ (в момент объявления) и в ТЕКУЩЕМ потоке (main), т.е. пока не дойдем до конца, не продолжим след строчку кода!
        CompletionStage<String> syncStage2 = syncStage("2");  - запускается после выполнения syncStage("1") и в ТЕКУЩЕМ потоке (main), т.е. пока не дойдем до конца, не продолжим след строчку кода!

        *
        *
        Tue Feb 27 12:48:03 GST 2018: [main] >> start2SplitSync
        Tue Feb 27 12:48:03 GST 2018: [main] >> syncStage > before sleep - 1
        Tue Feb 27 12:48:05 GST 2018: [main] << syncStage < after sleep - 1
        Tue Feb 27 12:48:05 GST 2018: [main] >> syncStage > before sleep - 2
        Tue Feb 27 12:48:07 GST 2018: [main] << syncStage < after sleep - 2
        Tue Feb 27 12:48:07 GST 2018: [main] >> syncStage > before sleep - 3
        Tue Feb 27 12:48:09 GST 2018: [main] << syncStage < after sleep - 3
        Tue Feb 27 12:48:09 GST 2018: [main] >> join > before sleep - join
        Tue Feb 27 12:48:14 GST 2018: [main] << join < after sleep - join
        Tue Feb 27 12:48:14 GST 2018: [main] before join
        Tue Feb 27 12:48:14 GST 2018: [main] << start2SplitSync
        Tue Feb 27 12:48:14 GST 2018: [main] exit
        * */

        logger.debug(">> start2SplitSync");
        CompletionStage<String> syncStage1 = syncStage("1");
        CompletionStage<String> syncStage2 = syncStage("2");
        CompletionStage<String> syncStage3 = syncStage("3");

        sleep("join", "join", 5000L);
        logger.debug("before join");
        syncStage1
                .thenCompose(unused -> syncStage2)
                .thenCompose(unused -> syncStage3)
                .toCompletableFuture().join();

        logger.debug("<< start2SplitSync");

        // TODO: (padudin) если убрать join - продемонстрировать , если буду оформлять.
    }

    private void start2SplitDoubleInnerRunAsync() {
        /*result:

        Видно как при каждом runAsync/thenRunAsync создается отдельный поток (бывало что переиспользовался предыдущий)


        Tue Feb 27 13:24:24 GST 2018: [main] >> start2SplitDoubleInnerRunAsync
        Tue Feb 27 13:24:24 GST 2018: [main] >> join > before sleep - join
        Tue Feb 27 13:24:24 GST 2018: [ForkJoinPool.commonPool-worker-2] >> asyncDoubleInnerSeqRunStage > before sleep - 2
        Tue Feb 27 13:24:24 GST 2018: [ForkJoinPool.commonPool-worker-1] >> asyncDoubleInnerSeqRunStage > before sleep - 1
        Tue Feb 27 13:24:26 GST 2018: [ForkJoinPool.commonPool-worker-1] << asyncDoubleInnerSeqRunStage < after sleep - 1
        Tue Feb 27 13:24:26 GST 2018: [ForkJoinPool.commonPool-worker-2] << asyncDoubleInnerSeqRunStage < after sleep - 2
        Tue Feb 27 13:24:26 GST 2018: [ForkJoinPool.commonPool-worker-3] >> asyncDoubleInnerSeqRunStage2_1 > before sleep - 2
        Tue Feb 27 13:24:26 GST 2018: [ForkJoinPool.commonPool-worker-4] >> asyncDoubleInnerSeqRunStage2_1 > before sleep - 1
        Tue Feb 27 13:24:28 GST 2018: [ForkJoinPool.commonPool-worker-4] << asyncDoubleInnerSeqRunStage2_1 < after sleep - 1
        Tue Feb 27 13:24:28 GST 2018: [ForkJoinPool.commonPool-worker-3] << asyncDoubleInnerSeqRunStage2_1 < after sleep - 2
        Tue Feb 27 13:24:28 GST 2018: [ForkJoinPool.commonPool-worker-4] >> asyncDoubleInnerSeqRunStage2_2 > before sleep - 1
        Tue Feb 27 13:24:28 GST 2018: [ForkJoinPool.commonPool-worker-1] >> asyncDoubleInnerSeqRunStage2_2 > before sleep - 2
        Tue Feb 27 13:24:29 GST 2018: [main] << join < after sleep - join
        Tue Feb 27 13:24:29 GST 2018: [main] before join
        Tue Feb 27 13:24:29 GST 2018: [main] << start2SplitDoubleInnerRunAsync
        Tue Feb 27 13:24:29 GST 2018: [main] exit
        * */

        logger.debug(">> start2SplitDoubleInnerRunAsync");

        CompletionStage<Void> asyncStage1 = asyncDoubleInnerSeqRunStage("1");
        CompletionStage<Void> asyncStage2 = asyncDoubleInnerSeqRunStage("2");

        sleep("join", "join", 5000L);
        logger.debug("before join");
        asyncStage1
                .thenCompose(unused -> asyncStage2)
                .toCompletableFuture().join();

        logger.debug("<< start2SplitDoubleInnerRunAsync");
    }

    /*
    * ВЫВОД:
    * все футуры по сути запускаются СРАЗУ как только они объявляются (поток доходит до их объявления).
    * Если они зачейнены при объявлении к предыдущему футуру, то до их объявления очередь дойдет после выполенния предыдущего,
    * т.е. и до запуска очередь дойдет только после выполнения предыдущей футуры в чейнинге.
    * В момент объявления, если это Async , то может быть запущено выполнение в новом треде, но последовательность определяется пред-м пунктом.
    * Если это не Async , то выполнение гарантировано в текущем треде.
    *
    * */
    private CompletionStage<String> syncStage(String name) {
        return CompletableFuture.completedFuture(null)
                .thenApply(unused -> {
                    sleep(name, "syncStage", 2000L);
                    return name + "_";
                });
    }

    private CompletionStage<String> asyncStage(String name) {
        return CompletableFuture.supplyAsync(() -> {
            sleep(name, "asyncStage", 2000L);
            return name + "_";
        });
    }

    private CompletionStage<String> asyncDoubleInnerSeqStage(String name) {
        return CompletableFuture.supplyAsync(() -> {
            sleep(name, "asyncDoubleInnerSeqStage", 2000L);
            return CompletableFuture.supplyAsync(() -> {
                sleep(name, "asyncDoubleInnerSeqStage2_1", 2000L);
                return name + "_";
            }).join();
        });
    }

    private CompletionStage<Void> asyncDoubleInnerSeqRunStage(String name) {
        return CompletableFuture.runAsync(() -> {
            sleep(name, "asyncDoubleInnerSeqRunStage", 2000L);
            CompletableFuture.runAsync(() -> {
                sleep(name, "asyncDoubleInnerSeqRunStage2_1", 2000L);
            }).thenRunAsync(() -> {
                sleep(name, "asyncDoubleInnerSeqRunStage2_2", 2000L);
            });
        });
    }

    private CompletionStage<String> asyncDoubleSeqStage(String name) {
        return CompletableFuture.supplyAsync(() -> {
            sleep(name, "asyncDoubleSeqStage", 2000L);
            return name + "_";
        }).thenCompose(name2 -> {
            return CompletableFuture.supplyAsync(() -> {

                sleep(name, "asyncDoubleSeqStage2", 3000L);
                return name2 + "_";
            });
        });
    }
    
}
