var chart = AmCharts.makeChart( "ohlc-chart", {
  "type": "serial",
  "theme": "light",
  "dataDateFormat":"YYYY-MM-DD",
  "valueAxes": [ {
    "position": "left"
  } ],
  "graphs": [ {
    "id": "g1",
    "balloonText": "Open:<b>[[open]]</b><br>Low:<b>[[low]]</b><br>High:<b>[[high]]</b><br>Close:<b>[[close]]</b><br>",
    "closeField": "close",
    "fillColors": "#7f8da9",
    "highField": "high",
    "lineColor": "#7f8da9",
    "lineAlpha": 1,
    "fillAlphas": 0,
    "lineThickness": 2,
    "lowField": "low",
    "negativeFillColors": "#db4c3c",
    "negativeLineColor": "#db4c3c",
    "openField": "open",
    "title": "Price:",
    "type": "ohlc",
    "valueField": "close"
  } ],
  "chartScrollbar": {
    "graph": "g1",
    "graphType": "line",
    "scrollbarHeight": 30
  },
  "chartCursor": {},
  "categoryField": "date",
  "categoryAxis": {
    "parseDates": true
  },
  "dataProvider": ohlcChartDataProvider,
  "export": {
    "enabled": true,
    "position": "bottom-right"
  }
} );

chart.addListener( "rendered", zoomChart );
zoomChart();

function zoomChart() {
  chart.zoomToIndexes( 20, 30 );
}
