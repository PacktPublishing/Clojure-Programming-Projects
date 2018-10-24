var tickerSocket = new WebSocket("ws://localhost:8080/ticker");
tickerSocket.onmessage = function (event) {
  var e = document.getElementById("ticker");
  e.textContent = event.data;
  console.log(event.data);
}
