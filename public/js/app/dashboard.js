$(document).ready(function(){
  var app = new App();
  ko.applyBindings(app, $(".wrapper")[0]);
});

window.App = function() {
  var _ = {}


  /*
   * Object models
   */
  _.logModel = function(data) {
    var o = this;
    o.created = ko.observable(data.created);
    o.createdDate = ko.observable(new Date(data.created).toLocaleTimeString())
    o.task = ko.observable(data.task);
    o.log = ko.observable(data.log);
    o.associateId = ko.observable(data.associateId);
    o.associateImg = ko.observable(data.associateImg);
    o.lastSeen = ko.observable(moment(data.created).fromNow());
    setInterval(function() {
      o.lastSeen(moment(o.created()).fromNow());
    }, 60000);
  }
  _.stateModel = function(data) {
    var o = this;
    o.keepActive = ko.observable(data.keepActive);
    o.state = ko.observable(data.state);
  }

  _.totalMatches = ko.observable(0);
  _.totalMessages = ko.observable(0);
  _.longTalks = ko.observable(0);
  _.summariesGetter = ko.computed(function() {
    $('.loader-global').show()
    $.ajax({
    url: "/t/"+getAuthToken()+"/messages/summaries",
      type: "GET",
      dataType: "json",
      success: function(data) {
        $('.loader-global').hide();
        _.totalMatches(data.total_matches);
        _.totalMessages(data.total_messages);
        _.longTalks(data.long_talks);
      },
      error: function() {
        $('.loader-global').hide();
      }
    });
  });

  _.logs = ko.observableArray([]);
  _.botTasks = ko.observable(0);
  _.logsGetter = ko.computed(function() {
    $('.loader-global').show()
    $.ajax({
      url: "/bot/log/"+getUserId(),
      type: "GET",
      dataType: "json",
      success: function(data) {
        $('.loader-global').hide();
        if(data!=null && data.length!=0) {
          var latest = data.reverse().slice(0, 4)
          var d = $.map(latest, function(item){ return new _.logModel(item) });
          _.logs(d);
          _.botTasks(data.length)
        }
      },
      error: function() {
        $('.loader-global').hide();
      }
    });
  });

  _.clearModels = function(){
    $('.loader-global').show();
    var r = confirm("Are you sure you want to clear all of your facial analysis data? You will need to like/dislike more people again in order for the bot to function properly.");
    if(r==true) {
      $.ajax({
          url: "/t/"+getAuthToken()+"/facial/reset",
          type: "DELETE",
          success: function(result) {
            $('.loader-global').hide();
            alert("Facial analysis data successfully erased. Please like/dislike more people to rebuild your models.")
          }
      });
    }
  }



  /*
   * Chart data
   */
  $('.loader-global').show();
  $.ajax({
    url: "/t/"+getAuthToken()+"/analytics/sentiments",
    type: "GET",
    dataType: "json",
    success: function(data) {
      var positive = 0,
        negative = 0,
        neutral = 0;
      for(m in data) {
        switch(data[m]) {
          case "POSITIVE":
            positive += 1;
            break;
          case "NEGATIVE":
            negative += 1;
            break;
          case "NEUTRAL":
            neutral += 1;
            break;
        }
      }
      var sentimentData = [
        {
        value: positive,
        color: "green",
        highlight: "#1ab394",
        label: "Positive"
        },
        {
        value: neutral,
        color: "gray",
        highlight: "#1ab394",
        label: "Neutral"
        },
        {
        value: negative,
        color: "red",
        highlight: "#1ab394",
        label: "Negative"
        }
      ];
      var chartOptions = {
        segmentShowStroke: true,
        segmentStrokeColor: "#fff",
        segmentStrokeWidth: 5,
        percentageInnerCutout: 20, // This is 0 for Pie charts
        animationSteps: 100,
        animationEasing: "easeOutBounce",
        animateRotate: true,
        animateScale: false,
        responsive: true
      };

      var ctx = document.getElementById("sentimentChart").getContext("2d");
      _.sentimentChart = new Chart(ctx).Doughnut(sentimentData, chartOptions);
      $('.loader-global').hide()
    },
    error: function() {
      $('.loader-global').hide()
    }
  });

  return _;
}