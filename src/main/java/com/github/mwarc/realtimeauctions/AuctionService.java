package com.github.mwarc.realtimeauctions;


import io.vertx.core.AbstractVerticle;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.ErrorHandler;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.handler.sockjs.BridgeEvent;
import io.vertx.ext.web.handler.sockjs.BridgeOptions;
import io.vertx.ext.web.handler.sockjs.PermittedOptions;
import io.vertx.ext.web.handler.sockjs.SockJSHandler;


public class AuctionService extends AbstractVerticle {

    private static final Logger logger = LoggerFactory.getLogger(AuctionService.class);

    @Override
    public void start() {
        Router router = Router.router(vertx);

        router.route().failureHandler(errorHandler());
        router.route("/eventbus/*").handler(eventBusHandler());
        router.mountSubRouter("/api", auctionApiRouter());
        router.route().handler(staticHandler());

        vertx.createHttpServer().requestHandler(router::accept).listen(8080);
    }

    private ErrorHandler errorHandler() {
        return ErrorHandler.create(true);
    }

    private SockJSHandler eventBusHandler() {
        BridgeOptions options = new BridgeOptions()
                .addOutboundPermitted(new PermittedOptions().setAddressRegex("auction\\.[0-9]+"));
        return SockJSHandler.create(vertx).bridge(options, event -> {
            if (event.type() == BridgeEvent.Type.SOCKET_CREATED) {
                logger.info("A socket was created");
            }
            event.complete(true);
        });
    }

    private Router auctionApiRouter() {
        AuctionRepository auctionRepository = new AuctionRepository(vertx.sharedData());
        AuctionApi auctionApi = new AuctionApi(auctionRepository);

        Router router = Router.router(vertx);
        router.route().consumes("application/json");
        router.route().produces("application/json");
        router.route().handler(BodyHandler.create());

        router.get("/auctions/:id").handler(auctionApi::handleGetAuction);
        router.patch("/auctions/:id").handler(auctionApi::handleChangeAuctionPrice);

        return router;
    }

    private StaticHandler staticHandler() {
        StaticHandler staticHandler = StaticHandler.create();
        staticHandler.setCachingEnabled(false);
        return staticHandler;
    }
}
