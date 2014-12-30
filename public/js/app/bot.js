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

  /*
   * ViewModels
   */
  /*_.state = ko.observable();
  _.stateGetter = ko.computed(function() {
    $('.loader-global').show()
    $.ajax({
      url: "/bot/state",
      type: "GET",
      dataType: "json",
      success: function(data) {
        $('.loader-global').hide();
        if(data!=null) {
          var d = $.map(data, function(item){ return new _.personModel(item) });
          _.recs(d);
        }
      },
      error: function() {
        $('.loader-global').hide();
      }
    });
  });*/

  _.logs = ko.observableArray([]);
  _.logsGetter = ko.computed(function() {
    $('.loader-global').show()
    $.ajax({
      url: "/bot/log/"+getUserId(),
      type: "GET",
      dataType: "json",
      success: function(data) {
        $('.loader-global').hide();
        if(data!=null && data.length!=0) {
          var d = $.map(data, function(item){ return new _.logModel(item) });
          _.logs(d);
          _.logs.reverse();
        }
      },
      error: function() {
        $('.loader-global').hide();
      }
    });
  });
  _.logsAppender = function() {
    $.ajax({
      url: "/bot/log/"+getUserId()+"/updates",
      type: "GET",
      dataType: "json",
      success: function(data) {
        if(data!=null && data.length!=0) {
          $.map(data, function(item){
            var d = new _.logModel(item);
            _.logs.unshift(d);
          });
        }
      },
      error: function() {
        _.updateIntervalErrors += 1;
      }
    });
  };

  _.like = function(data, event) {
    $.getJSON("/t/"+getAuthToken()+"/like/"+data.associateId(), function(result) {
      if(result!=null) alert("It's a match! Head to the inbox to chat.");
      _.createLog("undo_swipe_dislike", "User reversed a dislike for "+data.associateId()+".", data.associateId(), data.associateImg());
    });
  }
  _.dislike = function(data, event) {
    $.getJSON("/t/"+getAuthToken()+"/dislike/"+data.associateId(), function(result) {
      _.createLog("undo_swipe_like", "User reversed a like for "+data.associateId()+".", data.associateId(), data.associateImg());
    });
  }

  _.createLog = function(task, log, associateId, associateImg) {
    $.ajax({
      url: "/bot/log/"+getUserId(),
      type: "POST",
      data: JSON.stringify({created: new Date().getTime(), task: task, log: log, associateId: associateId, associateImg: associateImg}),
      contentType: "text/plain",
      success: function() {
        console.log("Successfully created bot log.");
      },
      error: function() {
        console.warn("Bot log creation failed.")
      }
    })
  }

  // set a timer for automatically retrieving updates
  _.updateIntervalErrors = 0;
  _.updateInterval = setInterval(function() {
    _.logsAppender();
    if(_.updateIntervalErrors > 5) {
      clearInterval(_.updateInterval)
      console.warn("Polling has been canceled, error threshold reached.");
    }
  }, 30000);

  return _;
}