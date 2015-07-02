package com.github.mwarc.realtimeauctions;

import io.vertx.ext.web.RoutingContext;

import java.util.Optional;

import static com.github.mwarc.realtimeauctions.Auction.defaultAuction;

public class AuctionApi {

    private final AuctionRepository auctionRepository;

    public AuctionApi(AuctionRepository auctionRepository) {
        this.auctionRepository = auctionRepository;
    }

    public void handleGetAuction(RoutingContext routingContext) {
        String auctionId = routingContext.request().getParam("id");
        Optional<Auction> auction = this.auctionRepository.getById(auctionId);

        if (auction.isPresent()) {
            routingContext.response()
                .putHeader("content-type", "application/json")
                .setStatusCode(200)
                .end(AuctionConverter.toJson(auction.get()));
        } else {
            routingContext.response()
                .putHeader("content-type", "application/json")
                .setStatusCode(404)
                .end();
        }
    }

    public void handleChangeAuctionPrice(RoutingContext routingContext) {
        String auctionId = routingContext.request().getParam("id");
        Auction auctionRequestBody = new Auction(auctionId, routingContext.getBodyAsJson());
        Auction auctionDatabase = this.auctionRepository.getById(auctionId).orElse(defaultAuction(auctionId));

        if (AuctionValidator.isBidPossible(auctionDatabase, auctionRequestBody)) {
            this.auctionRepository.save(auctionRequestBody);
            routingContext.vertx().eventBus().publish("auction." + auctionId, routingContext.getBodyAsString());

            routingContext.response()
                .putHeader("content-type", "application/json")
                .setStatusCode(200)
                .end();
        } else {
            routingContext.response()
                .putHeader("content-type", "application/json")
                .setStatusCode(400)
                .end();
        }
    }
}
