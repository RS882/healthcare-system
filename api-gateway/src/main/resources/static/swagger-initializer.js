window.onload = () => {
    window.ui = SwaggerUIBundle({
        urls: [
            { url: "/v3/api-docs/auth", name: "Auth Service" },
            { url: "/v3/api-docs/user", name: "User Service" }
        ],
        dom_id: '#swagger-ui',
        deepLinking: true,
        requestInterceptor: function (req) {
            req.credentials = 'include'; // 🔥 критично важно
            return req;
        },
        presets: [
            SwaggerUIBundle.presets.apis,
            SwaggerUIStandalonePreset
        ],
        layout: "StandaloneLayout"
    });
};
