package ru.mail.kdog.nettyproject.server.handlers;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import ru.mail.kdog.dto.DtoProtos;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import static ru.mail.kdog.nettyproject.server.handlers.ServiceHandler.QueryUtil.CONTENT;
import static ru.mail.kdog.nettyproject.server.handlers.ServiceHandler.QueryUtil.maxContentPart;
import static ru.mail.kdog.nettyproject.server.handlers.ServiceHandler.QueryUtil.minContentPart;

public class ServiceHandler extends ChannelInboundHandlerAdapter {


    private ThreadLocalRandom random = ThreadLocalRandom.current();

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object request) throws Exception {

        PseudoQuery pseudoQuery = generatePseudoQuery(); // генерация "запроса", в котором генерируется ветка BLOCK or NON-BLOCK, а также случайное содержимое
        Optional<DtoProtos.Page> resultingPage; // будущий результат

        if (pseudoQuery.isBlockingOperation()) {
            resultingPage = executeDefaultBlockingOperation(pseudoQuery);
        } else {
            resultingPage = executeNonBlockingOperation(pseudoQuery);
        }

        ctx.write(resultingPage.get());
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.fireChannelReadComplete();
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }

    /**
     * @param pseudoQuery объект запроса
     * @return Optional<DtoProtos.Page>
     * @throws InterruptedException
     */
    private Optional<DtoProtos.Page> executeDefaultBlockingOperation(PseudoQuery pseudoQuery) throws InterruptedException {
        return executeDefaultBlockingOperation(pseudoQuery, () -> callWait(QueryUtil.TIMEOUT_500));
    }

    /**
     * выполняет операцию c блокировкой
     * @param pseudoQuery типизированный запрос
     * @return Optional<DtoProtos.Page>
     */
    private Optional<DtoProtos.Page> executeDefaultBlockingOperation(PseudoQuery pseudoQuery, BlockingOperation blockingOperation) throws InterruptedException {
        blockingOperation.call();
        return executeNonBlockingOperation(pseudoQuery);
    }

    /**
     * выполняет операцию без блокировки
     *
     * @param pseudoQuery типизированный запрос
     * @return Optional<DtoProtos.Page>
     */
    private Optional<DtoProtos.Page> executeNonBlockingOperation(PseudoQuery pseudoQuery) {

        DtoProtos.Page.Builder pageBuilder = DtoProtos.Page.newBuilder();
        String pageUuid = UUID.randomUUID().toString();
        DtoProtos.UUID.Builder pageUUIDBuilder = DtoProtos.UUID.newBuilder();
        pageUUIDBuilder.setValue(pageUuid);
        pageBuilder.setUuid(pageUUIDBuilder);

        for (int i = 0; i < pseudoQuery.getCountPartContent(); i++) { // заполняем соответсвенно
            DtoProtos.ContentPart contentPart = DtoProtos.ContentPart.newBuilder()
                    .setPageUuid(pageUUIDBuilder)
                    .setPartNum(i)
                    .setContent(CONTENT + i)
                    .build();

            pageBuilder.addContentPart(contentPart);
        }
        return Optional.ofNullable(pageBuilder.build());
    }

    /**
     * генерация "запроса", в котором генерируется ветка BLOCK or NON-BLOCK, а также случайное содержимое
     * @return объект запроса PseudoQuery
     */
    private PseudoQuery generatePseudoQuery() {
        return new PseudoQuery(getRandomIntegerBetweenRange(minContentPart, maxContentPart), getRandomBoolean());
    }

    /**
     * Объект имитирующий объект запроса, содержит контекст-информацию запроса
     */
    private static class PseudoQuery {
        private final int countPartContent;
        private final boolean blockingOperation;

        int getCountPartContent() {
            return countPartContent;
        }

        public boolean isBlockingOperation() {
            return blockingOperation;
        }

        PseudoQuery(int partContent, boolean blockingOperation) {
            this.countPartContent = partContent;
            this.blockingOperation = blockingOperation;
        }
    }

    /**
     * Util класс инкапсулирующий методы для работы с запросами
     */
    static class QueryUtil {
        static long TIMEOUT_500 = 500L;
        static final String CONTENT = "part of content - ";
        static final int minContentPart = 1;
        static final int maxContentPart = 3;

    }

    private synchronized void callWait(long timeout) throws InterruptedException {
        wait(timeout);
    }

    //region вспомогательные методы-генераторы
    private int getRandomIntegerBetweenRange(int min, int max) {
        return random.nextInt(min, max);
    }

    private boolean getRandomBoolean() {
        return random.nextBoolean();
    }
    //endregion

    /**
     * Описывает сигнатуру блокирующей операции
     */
    @FunctionalInterface
    interface BlockingOperation {
        void call() throws InterruptedException;
    }
}
