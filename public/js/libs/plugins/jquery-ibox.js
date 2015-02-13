// ibox image zoomer plugin // roXon
// adapted from http://jsfiddle.net/roXon/HmTrw/

(function($) {
  $.fn.ibox = function() {
    var resize = 10;
    var img = this;

    img.each(function() {

      var el = $(this);

      el.mouseenter(function() {

        var body = $('body');
        body.append('<div class="img-zoom" />');
        var ibox = $('.img-zoom');
        ibox.css('z-index', 9999);
        ibox.css('position', 'absolute');

        var elX = 0;
        var elY = 0;

        elX = 10;
        elY = 10;
        var h = el.height();
        var w = el.width();
        var wh;
        checkwh = (h < w) ? (wh = (w / h * resize) / 2) : (wh = (w * resize / h) / 2);

        $(this).clone().prependTo(ibox);
        ibox.css({
            top: elY + 'px',
            left: elX + 'px'
        });

        ibox.stop().fadeTo(200, 1, function() {
            $(this).animate({top: '-='+(resize/2), left:'-='+wh},400).children('img').animate({height:'+='+resize},400);
        });
      });

      el.mouseleave(function() {
          $('.img-zoom').remove();
      });
    });
  };
})(jQuery);