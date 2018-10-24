var tickerSocket = new WebSocket("ws://localhost:8080/ticker");
tickerSocket.onmessage = function (event) {
  // var msg = JSON.parse(event.data);
  // if (msg.type === "ticker") {
    var id = "ticker";
    var e = document.getElementById(id);
    e.textContent = event.data;
  // }
  console.log(event.data);
}
