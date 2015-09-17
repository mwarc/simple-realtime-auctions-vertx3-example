package com.github.mwarc.realtimeauctions;

import io.vertx.core.json.Json;
import io.vertx.ext.web.RoutingContext;

import java.util.Optional;

import static com.github.mwarc.realtimeauctions.Auction.defaultAuction;

public class AuctionApi {

    private final AuctionRepository repository;

    public AuctionApi(AuctionRepository repository) {
        this.repository = repository;
    }

    public void handleGetAuction(RoutingContext context) {
        String auctionId = context.request().getParam("id");
        Optional<Auction> auction = this.repository.getById(auctionId);

        if (auction.isPresent()) {
            context.response()
                .putHeader("content-type", "application/json")
                .setStatusCode(200)
                .end(Json.encodePrettily(auction.get()));
        } else {
            context.response()
                .putHeader("content-type", "application/json")
                .setStatusCode(404)
                .end();
        }
    }

    public void handleChangeAuctionPrice(RoutingContext context) {
        String auctionId = context.request().getParam("id");
        Auction auctionRequestBody = new Auction(auctionId, context.getBodyAsJson());
        Auction auctionDatabase = this.repository.getById(auctionId).orElse(defaultAuction(auctionId));

        if (AuctionValidator.isBidPossible(auctionDatabase, auctionRequestBody)) {
            this.repository.save(auctionRequestBody);
            context.vertx().eventBus().publish("auction." + auctionId, context.getBodyAsString());

            context.response()
                .putHeader("content-type", "application/json")
                .setStatusCode(200)
                .end();
        } else {
            context.response()
                .putHeader("content-type", "application/json")
                .setStatusCode(422)
                .end();
        }
    }
}
