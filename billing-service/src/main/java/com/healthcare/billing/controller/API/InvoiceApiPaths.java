package com.healthcare.billing.controller.API;

public class InvoiceApiPaths {

    private InvoiceApiPaths() {
    }

    public static final String INVOICES_BASIC_URL = "/v1/invoices";

    public static final String PATH_VARIABLE_ID = "id";

    public static final String PATH_VARIABLE_INVOICE_NUMBER = "invoiceNumber";


    public static final String GET_BY_ID = "/{" + PATH_VARIABLE_ID + "}";

    public static final String GET_BY_INVOICE_NUMBER = "/number/{" + PATH_VARIABLE_INVOICE_NUMBER + "}";

    public static final String ISSUE_BY_ID = "/{" + PATH_VARIABLE_ID + "}/issue";

    public static final String CANCEL_BY_ID = "/{" + PATH_VARIABLE_ID + "}/cancel";

    public static final String PAY_BY_ID = "/{" + PATH_VARIABLE_ID + "}/pay";


}
