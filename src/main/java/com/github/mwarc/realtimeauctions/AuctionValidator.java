package com.github.mwarc.realtimeauctions;

import java.math.BigDecimal;

public class AuctionValidator {

    public static boolean isBidPossible(
        Auction auctionDatabase,
        Auction auctionRequestBody
    ) {
        BigDecimal currentPrice = auctionDatabase.getPrice();
        BigDecimal newPrice = auctionRequestBody.getPrice();
        return currentPrice.compareTo(newPrice) == -1;
    }
}
