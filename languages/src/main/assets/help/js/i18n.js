function getTranslation(page, callback) {
  $.when(
      function() {
        var d = $.Deferred();
        var json = $.getJSON("raw/" + page + ".json");
        json.done(function(result) {
          d.resolve(result);
        })
        json.fail(function() {
          d.resolve({});
        })
        return d.promise();
      }(),
      $.getJSON("locale/en/" + page + ".json")
    )
    .done(function(translation, fallback) {
      callback(translation, fallback[0]);
    });
}

function getParam(name, url) {
  if (!url) url = window.location.href;
  name = name.replace(/[\[\]]/g, "\\$&");
  var regex = new RegExp("[?&]" + name + "(=([^&#]*)|&|#|$)"),
    results = regex.exec(url);
  if (!results) return null;
  if (!results[2]) return '';
  return decodeURIComponent(results[2].replace(/\+/g, " "));
}

function applyTranslation(page) {
  getTranslation(page, function(translation, fallback) {
    $('[data-i18n]').each(function(index, element) {
      var key = $(element).data('i18n');
      var item = translation[key];
      if (item !== null && !item) item = fallback[key];
      if (item && item.length != 0) element.innerHTML = item;
    });
  });
}

$(function(){
  var page = window.location.pathname.split('/');
  page = page[page.length-1].split('.')[0];
  applyTranslation(page);

  var b=window.location.hash;
  if(b){
    if(b.startsWith("#")){
      b=b.substr(1)
    }
    var a=document.getElementById(b);
    if(a){
      a.open=true
      a.scrollIntoView(true);
    }
   }
})
