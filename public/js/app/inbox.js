$(document).ready(function(){
  var app = new App()
  app.Controller.run();
  ko.applyBindings(app, $(".wrapper")[0]);
});

window.App = function() {
  var _ = {}

  /*
   * Object models
   */
  _.matchModel = function(match) {
    var o = this;
    o._id = ko.observable(match._id);
    o.messages = ko.observableArray($.map(match.messages, function(item){ return new _.messageModel(item) }));
    o.messages.sort(function(left, right) { return new Date(left.created_date()) == new Date(right.created_date()) ? 0 : (new Date(right.created_date()) < new Date(left.created_date()) ? -1 : 1) });
    o.messages.subscribe(function(newMessages) {
      if(newMessages.length!=0) {
        var preview = newMessages[0].message().slice(0, 15)+"...";
        o.messagePreview(preview);
      }
    });
    o.person = ko.observable(new _.personModel(match.person))
    o.last_activity_date = ko.observable(match.last_activity_date);
    o.participants = ko.observableArray(match.participants);
    o.unread_count = ko.observable(match.message_count);
    o.messagePreview = ko.observable((match.messages.length!=0 ? match.messages[match.messages.length-1].message.slice(0, 15)+"..." : ""));
    o.getEnhancedProfile = function() {
      $.getJSON("/t/"+getAuthToken()+"/profile/"+o.participants()[0], function(data) {
        o.person(new _.personModel(data));
        $(".message-header-photos span img").ibox();
      });
    }
  }
  _.personModel = function(data) {
    var o = this;
    o._id = ko.observable(data._id);
    o.name = ko.observable(data.name);
    o.photos = ko.observable($.map(data.photos, function(item){ return new _.photoModel(item) }));
    o.bio = ko.observable(data.bio);
    o.mainPhoto = ko.observable((data.photos.length!=0 ? data.photos[0].processedFiles[3].url : "/assets/img/user-generic.png"));
    try { var old_ping = new Date(o.ping_time()) } catch(e) { var old_ping = 0 }
    o.ping_time = ko.observable((new Date(data.ping_time).getTime() > old_ping ? data.ping_time : old_ping));
    o.distance_mi = ko.observable((typeof data.distance_mi=="undefined" ? null : data.distance_mi));
    o.lastSeen = ko.computed(function() { return moment(o.ping_time()).fromNow() });
    setInterval(function() {
      o.ping_time.valueHasMutated();
    }, 60000);
  }
  _.photoModel = function(data) {
    var o = this;
    o.id = ko.observable(data.id);
    o.url = ko.observable(data.url);
    o.fileName = ko.observable(data.fileName)
    o.extension = ko.observable(data.extension);
  }
  _.messageModel = function(data) {
    var o = this;
    o.from = ko.observable(data.from);
    o.to = ko.observable(data.to);
    o.message = ko.observable(data.message);
    o.created_date = ko.observable(data.created_date);
    o.sent = ko.observable(moment(data.created_date).fromNow());
    setInterval(function() {
      o.sent(moment(o.created_date()).fromNow());
    }, 60000);
  }

  /*
   * ViewModels
   */
  _.refresh = ko.observable(0);
  _.matches = ko.observableArray([]);
  _.matchesGetter = ko.computed(function() {
    $('.loader-global').show()
    _.refresh();
    $.ajax({
      url: "/t/"+getAuthToken()+"/messages",
      type: "GET",
      dataType: "json",
      success: function(data) {
        $('.loader-global').hide();
        var d = $.map(data, function(item){ return new _.matchModel(item) });
        _.matches(d);
        //_.matches.sort(function(left, right) { return new Date(left.last_activity_date()) == new Date(right.last_activity_date()) ? 0 : (new Date(right.last_activity_date()) < new Date(left.last_activity_date()) ? -1 : 1) });
        _.matches.reverse()
      },
      error: function() {
        $('.loader-global').hide();
      }
    });
  });

  _.selectedMatch = ko.observable();
  _.selectedMatch.subscribe(function(newValue) {
    $(".tooltip-ui").tooltip({
      selector: "[data-toggle=tooltip]",
      container: "body"
    });
    if(newValue!=null) {
      newValue.getEnhancedProfile();
      $.ajax({
        url: "/t/"+getAuthToken()+"/messages/unread_count/"+newValue._id(),
        type: "DELETE",
        success: function() {
          newValue.unread_count(0);
        }
      });
    }
  });
  _.selectedMatchId = ko.observable();
  _.matches.subscribe(function() { _.selectedMatchId.valueHasMutated(); });
  _.selectedMatchSetter = ko.computed(function() {
    try {
      var matches = _.matches()
      for (i = 0; i < matches.length; i++) {
        if(matches[i]._id()==_.selectedMatchId()) _.selectedMatch(matches[i])
      }
    } catch(e) {
      console.error(e)
    }
  });

  // Match chart analytics
  _.matchDailyChart = ko.observableArray([]);
  _.selectedMatchAnalytics = ko.computed(function() {
    try {
      var matches = _.matches()
      var matchId = _.selectedMatchId()
      var tzOffset = new Date().getTimezoneOffset() / 60;

      for (i = 0; i < matches.length; i++) {
        if(matches[i]._id()==_.selectedMatchId()) {
          var messages = matches[i].messages();
          var timestamps = $.map(messages, function(item) { if(item.from()!=getUserId()) return item.created_date().split("T")[0]; });

          if(timestamps.length==0) {
            _.matchDailyChart([0]);
            setTimeout(function(){ $(".line").peity("line"); }, 300);

          } else {
            var counts = {};
            counts[moment().format("YYYY-MM-DD")] = 0;
            for(var x = 0; x< timestamps.length; x++) {
              var num = timestamps[x];
              counts[num] = counts[num] ? counts[num]+1 : 1;
            }
            var intervals = [];
            for(count in counts) { intervals.push(counts[count]); }

            _.matchDailyChart(intervals);
            _.matchDailyChart.reverse();
            setTimeout(function(){ $(".line").peity("line"); }, 300);
          }
        }
      }
    } catch(e) {
      console.error(e)
    }
  });

  _.messageDraft = ko.observable("");
  _.sendMessage = function(data, event) {
    $.post("/t/"+getAuthToken()+"/messages/send/"+_.selectedMatchId(), {message: _.messageDraft()}, function(newMessage) {
      _.messageDraft("");
      $.map(_.matches(), function(match) {
        if(match.participants()[0]==newMessage.to) match.messages.unshift(new _.messageModel(newMessage));
      });
    });
    return false;
  }

  _.getUpdates = function() {
    $.ajax({
      url: "/t/"+getAuthToken()+"/updates",
      type: "GET",
      dataType: "json",
      success: function(data) {
        // check if there are any updates
        if(typeof data.unread_counts!="undefined" && data.unread_counts!=null) {
          var count = 0;
          for(i in data.unread_counts) {
            count += data.unread_counts[i];
          }
          if(count>0) {
            _.refresh.valueHasMutated();
            console.log("Received "+count+" new updates.")
          }
        }
      },
      error : function() {
        _.updateIntervalErrors += 1;
        console.warn("Polling failed for latest updates.");
      }
    });
  }
  // set a timer for automatically retrieving updates
  _.updateIntervalErrors = 0;
  _.updateInterval = setInterval(function() {
    _.getUpdates();
    if(_.updateIntervalErrors > 5) {
      clearInterval(_.updateInterval)
      console.warn("Polling has been canceled, error threshold reached.");
    }
  }, 30000);

  _.reportUser = function(data, event) {
    var r = confirm("Are you sure you want to report this user as annoying?");
    if(r==true) {
      $.post("/t/"+getAuthToken()+"/report/"+data._id()+"/2", function(data) {
        // reporting a user doesn't automatically unmatch them
        _.unmatch(data, event)
      });
    }
  }
  _.unmatch = function(data, event) {
    var r = confirm("Are you sure you want to unmatch with this user?");
    if(r==true) {
      $.ajax({
        url: "/t/"+getAuthToken()+"/unmatch/"+_.selectedMatchId(),
        type: "DELETE",
        success: function(result) {
          _.matches.valueHasMutated();
          window.location.hash = "all"
        }
      });
    }
  }
  _.addYesNoData = function(data, event) {
  $('.loader-global').show();
    var r = confirm("Are you sure you want to add this user to facial analysis models?");
      if(r==true) {
        $.ajax({
          url: "/t/"+getAuthToken()+"/yesno/"+_.selectedMatchId()+"/true",
          type: "GET",
          success: function(result) {
            $('.loader-global').hide();
          }
        });
      }
  }

  /*
   * Controller for hash routing
   */
  _.Controller = Sammy(function() {

    // this is used in Sammy.js to set a flag so that the error handling is not overridden. (using default)
    this.raise_errors = true;

    // if a notfound error is raised then the location.hash with be set to the root page.
    this.notFound = function() {
      location.hash = "/all";
    }

    //this is used to call any functions before any route is executed.
    this.before(/.*/, function() {
      try {  } catch(e) {  }
    });

    // this is used to call any functions after the route has been executed.
    this.after(function() {
      try {  } catch(e) { console.log(e); }
    });

    this.get('#/all', function() {
      _.selectedMatch(null);
      _.selectedMatchId(null);
    });

    this.get('#/match/:matchId', function() {
      var matchId = this.params.matchId;
      _.selectedMatchId(matchId);
    });

  });

  return _;
}
