$(document).ready(function(){
  var app = new App();
  ko.applyBindings(app, $(".wrapper")[0]);
});

window.App = function() {
  var _ = {}

  /*
   * Object models
   */
  _.positionModel = function(data) {
    var o = this;
    o.lat = ko.observable(data.lat);
    o.lon = ko.observable(data.lon);
  }
  _.searchModel = function(data) {
    var o = this;
    o.age_filter_min = ko.observable(data.age_filter_min);
    o.age_filter_max = ko.observable(data.age_filter_max);
    o.distance_filter = ko.observable(data.distance_filter);
    o.gender = ko.observable(data.gender);
    o.interested_in = ko.observableArray(data.interested_in);
  }

  /*
   * ViewModels
   */
  _.position = ko.observable();
  _.search = ko.observable();
  _.ageRange = ko.observableArray([18,27]);
  _.ageRange.subscribe(function(newVal) {
    var s = ko.toJS(_.search())
    s.age_filter_min = Number(newVal[0]);
    s.age_filter_max = Number(newVal[1]);
    _.search(new _.searchModel(s));
  });
  _.profileGetter = ko.computed(function() {
    $('.loader-global').show();
    $.ajax({
      url: "/t/"+getAuthToken()+"/profile",
      type: "GET",
      dataType: "json",
      success: function(data) {
        $('.loader-global').hide();
        if(data.pos.length > 0) _.position(new _.positionModel(data.pos));
        _.search(new _.searchModel(data));
        _.ageRange([data.age_filter_min, data.age_filter_max]);
      },
      error: function() {
        $('.loader-global').hide();
      }
    });
  });

  _.saveSearch = function(data, event) {
    $('.loader-global').show();
    $.ajax({
      url: "/t/"+getAuthToken()+"/profile/discovery",
      type: "POST",
      data: ko.toJSON(_.search),
      dataType: "json",
      contentType: "application/json",
      success: function(data) {
        $('.loader-global').hide();
      },
      error: function() {
        $('.loader-global').hide();
      }
    });
  }

  _.savePosition = function() {
    $('.loader-global').show();
    var coords = {lat: _.position().lat(), lon: _.position().lon()};
    $.ajax({
      url: "/t/"+getAuthToken()+"/profile/position",
      type: "POST",
      data: JSON.stringify(coords),
      dataType: "json",
      contentType: "application/json",
      success: function(data) {
        $('.loader-global').hide();
      },
      error: function() {
        $('.loader-global').hide();
      }
    });
  }

  _.fixPosition = function() {
    if(navigator.geolocation) {
      $('.loader-global').show();
      navigator.geolocation.getCurrentPosition(function(position) {
        _.position(new _.positionModel({lat: position.coords.latitude, lon: position.coords.longitude}));
        _.savePosition(position.coords.latitude, position.coords.longitude);
        $('.loader-global').hide();
      }, function(data) {
        alert("Sorry, geolocation via your browser failed. Could be a permissions issue.");
        _.position(new _.positionModel({lat: 49.25, lon: -123.1}));
        $('.loader-global').hide();
      });
    } else {
      alert("Sorry, geolocation is not supported by your browser.");
    }
  }

  return _;
}