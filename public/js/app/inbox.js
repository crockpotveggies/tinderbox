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
  _.matchModel = function(data) {
    var o = this;
    o._id = ko.observable(data._id);
    o.messages = ko.observableArray($.map(data.messages, function(item){ return new _.messageModel(item) }));
    o.messages.sort(function(left, right) { return new Date(left.created_date()) == new Date(right.created_date()) ? 0 : (new Date(right.created_date()) < new Date(left.created_date()) ? -1 : 1) });
    o.person = ko.observable(new _.personModel(data.person))
    o.last_activity_date = ko.observable(data.last_activity_date);
    o.participants = ko.observableArray(data.participants);
    o.messagePreview = ko.observable((data.messages.length!=0 ? data.messages[data.messages.length-1].message.slice(0, 15)+"..." : ""));
  }
  _.personModel = function(data) {
    var o = this;
    o._id = ko.observable(data._id);
    o.name = ko.observable(data.name);
    o.photos = ko.observable($.map(data.photos, function(item){ return new _.photoModel(item) }));
    o.bio = ko.observable(data.bio);
    o.mainPhoto = ko.observable((data.photos.length!=0 ? data.photos[0].processedFiles[3].url : "/assets/img/user-generic.png"));
    o.lastSeen = ko.observable(moment(data.ping_time).fromNow());
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
  }

  /*
   * ViewModels
   */
  _.matches = ko.observableArray([]);
  _.matchesGetter = ko.computed(function() {
    $.getJSON("/t/"+getAuthToken()+"/messages", function(data) {
      var d = $.map(data, function(item){ return new _.matchModel(item) });
      _.matches(d);
      _.matches.sort(function(left, right) { return new Date(left.last_activity_date()) == new Date(right.last_activity_date()) ? 0 : (new Date(right.last_activity_date()) < new Date(left.last_activity_date()) ? -1 : 1) });
    });
  });

  _.selectedMatch = ko.observable();
  _.selectedMatch.subscribe(function() {
    $(".tooltip-ui").tooltip({
      selector: "[data-toggle=tooltip]",
      container: "body"
    });
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

  _.messageDraft = ko.observable("");
  _.sendMessage = function(data, event) {
    $.post("/t/"+getAuthToken()+"/messages/send/"+_.selectedMatchId(), {message: _.messageDraft()}, function(data) {
      _.messageDraft("");
      _.selectedMatch().messages.unshift(new _.messageModel(data));
    });
  }

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