(function () {
    var enable = %s;
    var id = 'yuzubrowser_invert_mode';
    var style = document.getElementById(id);

    if ((enable && style != null) || (!enable && style == null)) {
        return
    }

    if (enable) {
        style = document.createElement('style');
        style.type = 'text/css';
        style.id = id;
        style.innerHTML = 'img,video,canvas{-webkit-filter:invert(100%)}';

        document.getElementsByTagName('head')[0].appendChild(style);
    } else {
        style.parentNode.removeChild(style);
    }
}());