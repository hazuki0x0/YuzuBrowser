(function() {
    var links = document.getElementsByTagName("link")

    var iconLinks = []

    for (var i = 0; i < links.length; i++) {
        var rel = links[i].rel
        if (rel == "apple-touch-icon-precomposed" || rel == "apple-touch-icon") {
            iconLinks.push(links[i])
        }
    }

    if (iconLinks.length == 0) return ""

    var max = -1
    var maxSize = -1
    var optional = null

    for (var i = 0; i < iconLinks.length; i++) {
        var sizes = iconLinks[i].sizes[0]
        if (sizes == null) {
            optional = iconLinks[i]
        } else {
            var size = Number(sizes[0].match(/\d+/))
            if (size > maxSize) {
                max = i
                maxSize = size
            }
        }
    }

    if (max == -1) {
        if (optional == null) {
            return ""
        } else {
            return optional.href
        }
    }

    return iconLinks[max].href
}());