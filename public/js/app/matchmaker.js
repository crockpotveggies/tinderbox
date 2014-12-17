$(document).ready(function(){
  var app = new App();
  ko.applyBindings(app, $(".wrapper")[0]);
});

window.App = function() {
  var _ = {}

  /*
   * Object models
   */
  _.personModel = function(data) {
    var o = this;
    o._id = ko.observable(data._id);
    o.name = ko.observable(data.name);
    o.photos = ko.observable($.map(data.photos, function(item){ return new _.photoModel(item) }));
    o.bio = ko.observable(data.bio);
    o.mainPhoto = ko.observable((data.photos.length!=0 ? data.photos[0].processedFiles[3].url : "/assets/img/user-generic.png"));
    o.ping_time = ko.observable(data.ping_time);
    o.distance_mi = ko.observable((typeof data.distance_mi=="undefined" ? null : data.distance_mi));
    o.birth_date = ko.observable(data.birth_date);
    o.age = ko.observable(moment(data.birth_date).fromNow(true).split(" ")[0]);
    o.common_friend_count = ko.observable(data.common_friend_count);
    o.common_like_count = ko.observable(data.common_like_count);
    o.lastSeen = ko.observable(moment(data.ping_time).fromNow());
    setInterval(function() {
      o.lastSeen(moment(o.ping_time()).fromNow());
    }, 60000);
  }
  _.photoModel = function(data) {
    var o = this;
    o.id = ko.observable(data.id);
    o.url = ko.observable(data.url);
    o.squareUrl = ko.observable(data.processedFiles[0].url)
    o.fileName = ko.observable(data.fileName);
    o.extension = ko.observable(data.extension);
  }

  /*
   * ViewModels
   */
  _.featuredProfile = ko.observable();
  _.featuredProfile.subscribe(function() {
    $("#featuredProfileCarousel").carousel({interval: 4000});
  });
  _.recs = ko.observableArray([]);
  _.recs.subscribe(function(newValue) {
    if(newValue.length > 0) _.featuredProfile(newValue[0]);
    if(newValue.length < 5) _.recsAppender();
  })
  _.recsGetter = ko.computed(function() {
    $('.loader-global').show()
    $.ajax({
      url: "/t/"+getAuthToken()+"/recommendations",
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
  });
  _.recsAppender = function() {
    $('.loader-global').show()
    $.ajax({
      url: "/t/"+getAuthToken()+"/recommendations",
      type: "GET",
      dataType: "json",
      success: function(data) {
        $('.loader-global').hide();
        if(typeof data.message=="undefined") {
          var d = $.map(data, function(item){ return new _.personModel(item) });
          $.map(d, function(item) { _.recs.push(item); });
        }
      },
      error: function() {
        $('.loader-global').hide();
      }
    });
  }

  _.like = function(data, event) {
    $.getJSON("/t/"+getAuthToken()+"/like/"+data._id(), function(result) {
      if(result!=null) alert("It's a match! Head to the inbox to chat.")
      _.recs.remove(data);
    });
  }
  _.dislike = function(data, event) {
    $.getJSON("/t/"+getAuthToken()+"/dislike/"+data._id(), function(result) {
      _.recs.remove(data);
    });
  }

  return _;
}