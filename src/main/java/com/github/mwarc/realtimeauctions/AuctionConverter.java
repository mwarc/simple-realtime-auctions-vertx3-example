package com.github.mwarc.realtimeauctions;

import io.vertx.core.json.JsonObject;

public class AuctionConverter {

    private AuctionConverter() {
    }

    public static String toJson(Auction auction) {
        JsonObject json = new JsonObject()
                .put("id", auction.getId())
                .put("price", auction.getPrice().toString());
        return json.toString();
    }
}
