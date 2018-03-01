package com.kk.async;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import static com.kk.helpers.LoggerHelper.print;
import static com.kk.helpers.ThreadHelper.sleep;

public class CompletionStageTest2 {

    public static void main(String[] args) {

        CompletionStageTest2 completionStageTest2 = new CompletionStageTest2();

        completionStageTest2.expSingleComletionStageAndAsync();
//        completionStageTest2.expDoubleComletionStageAndAsync();
    }

    public void expSingleComletionStageAndAsync() {
        /*
        Thu Mar 01 19:54:29 GST 2018: [ForkJoinPool.commonPool-worker-1] >> getAsyncCompletionStage: chunk 1
        Thu Mar 01 19:54:29 GST 2018: [ForkJoinPool.commonPool-worker-1] >> chunk 1 > before sleep -
        Thu Mar 01 19:54:31 GST 2018: [ForkJoinPool.commonPool-worker-1] << chunk 1 < after sleep -
        Thu Mar 01 19:54:31 GST 2018: [ForkJoinPool.commonPool-worker-1] >> getAsyncCompletionStage: chunk 2
        Thu Mar 01 19:54:31 GST 2018: [ForkJoinPool.commonPool-worker-1] >> chunk 2 > before sleep -
        Thu Mar 01 19:54:31 GST 2018: [ForkJoinPool.commonPool-worker-1] << chunk 2 < after sleep -
        * */

        CompletableFuture.completedFuture(null)
                .thenCompose(unused -> {
                    return getAsyncCompletionStage("chunk 1", 2000L); // вот тут случайно вызываем метод который возвращает CompletionStage и все как бы будет хорошо и без ошибок, но с последствиями
                })
                .thenCompose(unused -> getAsyncCompletionStage("chunk 2", 500L))
                .toCompletableFuture().join();
    }

    public void expDoubleComletionStageAndAsync() {
        /*
        Thu Mar 01 19:52:21 GST 2018: [ForkJoinPool.commonPool-worker-2] >> getAsyncCompletionStage: chunk 2
        Thu Mar 01 19:52:21 GST 2018: [ForkJoinPool.commonPool-worker-1] >> getAsyncCompletionStage: chunk 1
        Thu Mar 01 19:52:21 GST 2018: [ForkJoinPool.commonPool-worker-1] >> chunk 1 > before sleep -
        Thu Mar 01 19:52:21 GST 2018: [ForkJoinPool.commonPool-worker-2] >> chunk 2 > before sleep -
        Thu Mar 01 19:52:22 GST 2018: [ForkJoinPool.commonPool-worker-2] << chunk 2 < after sleep -

        << chunk 1 < after sleep - отсутствует, т.к. асинхронный стейдж был завернут в другой стейдж
        * */

        CompletableFuture.completedFuture(null)
                .thenApply(unused -> {
                    return getAsyncCompletionStage("chunk 1", 2000L); // вот тут случайно вызываем метод который возвращает CompletionStage и все как бы будет хорошо и без ошибок, но с последствиями
                })
                .thenCompose(unused -> getAsyncCompletionStage("chunk 2", 500L))
                .toCompletableFuture().join();
    }

    private CompletionStage<String> getAsyncCompletionStage(String value, long sleepTime) {
        return CompletableFuture.supplyAsync(() -> {
            print(">> getAsyncCompletionStage: " + value);
            sleep("", value, sleepTime);
            return value;
        });
    }
}
