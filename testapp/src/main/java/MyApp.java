import java.util.concurrent.CompletableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by vazvadsk on 2017-04-22.
 */
public class MyApp {
    final static Logger logger = LoggerFactory.getLogger(MyApp.class);
    public static void main(String[] args) throws InterruptedException {
        CompletableFuture.runAsync(() -> endlessLoop());
        CompletableFuture.runAsync(() -> endlessLoop());
        CompletableFuture.runAsync(() -> endlessLoop());
        CompletableFuture.runAsync(() -> endlessLoop());
        endlessLoop();
    }

    public static void endlessLoop(){
        while(true){
            logger.trace("My TRACE message via LogBack appender for OMS ... " + Thread.currentThread());
            logger.debug("My DEBUG message via LogBack appender for OMS ... "+ Thread.currentThread());
            logger.info("My INFO message via LogBack appender for OMS ... "+ Thread.currentThread());
            logger.warn("My WARN message via LogBack appender for OMS ... "+ Thread.currentThread());
            logger.error("My ERROR message via LogBack appender for OMS ... "+ Thread.currentThread());

            try{
                int i = 0;
                i = i / i;
            }catch (ArithmeticException ex){
                logger.error("My arithmetic error via LogBack appender for OMS ...", ex);
            }
        }
    }
}
