--liquibase formatted sql

--changeset Rslan:003-create-card-payment-details

CREATE TABLE card_payment_details
(
    payment_id                  BIGINT          NOT NULL,
    provider                    VARCHAR(50)     NOT NULL,
    provider_transaction_id     VARCHAR(255)    NOT NULL,

    CONSTRAINT pk_card_payment_details
          PRIMARY KEY (payment_id),

    CONSTRAINT fk_card_payment_details_payment
        FOREIGN KEY (payment_id)
            REFERENCES payment (id),

    CONSTRAINT uk_card_provider_transaction
        UNIQUE (provider, provider_transaction_id)
);