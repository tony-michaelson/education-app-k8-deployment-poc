var idea, mapModel;
var deck_id = '';
var segmentID = '';
var parent_deck_id = '';

var card_sequence = {};
var current_card_in_seq = 0;
var current_card = {};
var current_deck_id;
var posts = {};
var card_forgot = '';

var bootbox = {
  alert: function (message) {
    location.href =
      '/maps/' + deck_id + '/' + segmentID + '/#/bb/' + encodeURI(message);
  },
};

function addNode(nodeType) {
  global_node_type = nodeType;
  mapModel.addSubIdea('toolbar', mapModel.getSelectedNodeId());
}

function getMapAsJSON(mapID, segmentID, callback) {
  if (!segmentID) {
    segmentID = mapID;
  }
  mpio.getMap(mapID, segmentID, callback);
  // mapC.getMapByID(id, callback)
}

function refreshMap(callback) {
  var last_selected = mapModel.getSelectedNodeId();
  getMapAsJSON(deck_id, segmentID, function (response) {
    var site_map = eval(response);
    idea = MAPJS.content(site_map);
    mapModel.setIdea(idea);
    parent_deck_id = mapModel.getIdea().attr.parent_map_id;
    if (parent_deck_id) {
      $('#parentDeckButton').show();
    } else {
      $('#parentDeckButton').hide();
    }
    $('#mapContainer').css('cursor', 'auto');
    if (typeof callback == 'function') {
      callback();
    }
  });
}

// console.log( top.$('#mapContainer').offset().top );
// console.log( $(top.window).height() );
// console.log( $(top.document).height() );

function resizeMapHeight(buffer = 0) {
  var containerHeight =
    (window.innerHeight || $(window).height()) -
    56 -
    buffer -
    $('#mapContainer').offset().top;
  $('#mapContainer').height(containerHeight);
  if (mapModel) {
    mapModel.centerOnNode(1);
  } // center on root
}

$(window).resize(function () {
  if ($('#mapContainer').length) {
    resizeMapHeight();
  }
});

function setupMapStuff(mapID, segmentID, callback) {
  top.deck_id = mapID;
  top.segmentID = segmentID || mapID;
  resizeMapHeight(56);
  getMapAsJSON(mapID, segmentID, function (response) {
    var site_map = eval(response);
    callback(site_map.permissions);
    window.onerror = alert;
    var container = jQuery('#mapContainer'),
      idea = MAPJS.content(site_map),
      isTouch = false,
      mapModel = new MAPJS.MapModel(MAPJS.KineticMediator.layoutCalculator, []);
    container.mapWidget(console, mapModel, isTouch);
    jQuery('body').mapToolbarWidget(mapModel);

    mapModel.setIdea(idea);
    window.mapModel = mapModel;
    jQuery('.arrow').click(function () {
      jQuery(this).toggleClass('active');
    });

    jQuery('#nodeContextMenu')
      .contextMenuWidget(mapModel, idea)
      .mapToolbarWidget(mapModel);
  });
}

function getPostInfo(item_id, posts) {
  for (var i = 0; i < posts.length; i++) {
    if (posts[i].item_id === item_id) {
      return posts[i];
    }
  }
}
