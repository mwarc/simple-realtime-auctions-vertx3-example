package com.github.mwarc.realtimeauctions;

import io.vertx.core.json.Json;
import io.vertx.ext.web.RoutingContext;

import java.math.BigDecimal;
import java.util.Optional;

public class AuctionHandler {

    private final AuctionRepository repository;

    public AuctionHandler(AuctionRepository repository) {
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
        Auction auctionRequestBody = new Auction(
            auctionId,
            new BigDecimal(context.getBodyAsJson().getString("price"))
        );
        Auction auctionDatabase = this.repository.getById(auctionId).orElse(new Auction(auctionId));

        if (AuctionValidator.isBidPossible(auctionDatabase, auctionRequestBody)) {
            this.repository.save(auctionRequestBody);
            context.vertx().eventBus().publish("auction." + auctionId, context.getBodyAsString());

            context.response()
                .setStatusCode(200)
                .end();
        } else {
            context.response()
                .setStatusCode(422)
                .end();
        }
    }
}
