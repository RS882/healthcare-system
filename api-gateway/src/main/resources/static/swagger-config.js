window.onload = () => {
    window.ui = SwaggerUIBundle({
        dom_id: '#swagger-ui',
        urls: [
            { name: "Auth Service", url: "/v3/api-docs/auth" },
            { name: "User Service", url: "/v3/api-docs/user" }
        ],
        requestInterceptor: (req) => {
            req.credentials = "include"; // ⬅️ ключ
            return req;
        },
        presets: [
            SwaggerUIBundle.presets.apis,
            SwaggerUIStandalonePreset
        ],
        layout: "StandaloneLayout"
    });
}
