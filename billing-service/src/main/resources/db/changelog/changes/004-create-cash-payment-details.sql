--liquibase formatted sql

--changeset Rslan:004-create-cash-payment-details

CREATE TABLE cash_payment_details
(
    payment_id      BIGINT         NOT NULL,
    received_amount NUMERIC(19, 2) NOT NULL,
    currency        VARCHAR(3)     NOT NULL,

    CONSTRAINT pk_cash_payment_details
        PRIMARY KEY (payment_id),

    CONSTRAINT fk_cash_payment_details_payment
        FOREIGN KEY (payment_id)
            REFERENCES payment (id),

    CONSTRAINT ck_cash_payment_details_received_amount_positive
        CHECK ( received_amount > 0 )
);