package com.hesaplab.transfer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.io.IOException;

public record TransferRequest(String sourceAccountId, String targetAccountId,
        @JsonDeserialize(using = AmountText.class) String amount) {
    public static class AmountText extends JsonDeserializer<String> {
        @Override public String deserialize(JsonParser parser, DeserializationContext context) throws IOException {
            if (!parser.hasToken(JsonToken.VALUE_STRING)) {
                return context.reportInputMismatch(String.class, "Tutar JSON metni olmalı.");
            }
            return parser.getText();
        }
    }
}
