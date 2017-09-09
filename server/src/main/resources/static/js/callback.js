(function () {

function createSocketAndSendCode() {
    var ws = new WebSocket("ws://localhost:" + PORT);
    ws.onopen = function (event) {
        ws.send(AUTH_CODE);
        ws.close();
    };
    ws.onerror = function (event) {
        alert("Could not communicate with Greedy Greedy.");
    };
}

window.onload = function () {
    var launchAnchor = document.getElementById("launchAnchor");
    if (TYPE === "desktop") {
        createSocketAndSendCode();
        if (launchAnchor) {
            launchAnchor.href = "#";
            launchAnchor.onclick = function (event) {
                createSocketAndSendCode()
            };
        }
    }
    else if (TYPE === "android") {
        var mobileDetect = new MobileDetect(window.navigator.userAgent);
        var letsReinventTheWheelIntentUrl = "intent://auth/callback?code=" + AUTH_CODE +
            "#Intent;scheme=com.rolandoislas.greedygreedy;end";
        var intentUrl = "com.rolandoislas.greedygreedy://auth/callback?code=" + AUTH_CODE;
        if (mobileDetect.is("Chrome") && mobileDetect.version("Chrome") >= 25)
            intentUrl = letsReinventTheWheelIntentUrl;
        window.location = intentUrl;
        if (launchAnchor)
            launchAnchor.href = intentUrl;
    }
};
}());