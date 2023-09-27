/*global jQuery, _, document, window*/
jQuery.fn.contextMenuWidget = function (mapModel, idea) {
  'use strict';
  var content = this.find('[data-mm-context-menu]').clone(),
    element = jQuery('<ul class="dropdown-menu">')
      .css('position', 'absolute')
      .css('z-index', '999')
      .hide()
      .appendTo('body'),
    hide = function () {
      if (element.is(':visible')) {
        element.hide();
      }
      jQuery(document).off('click touch keydown', hide);
    },
    topMenus = {},
    getTopMenu = function (label) {
      if (!topMenus[label]) {
        var dropDownMenu = jQuery(
          '<li class="dropdown-submenu"><a tabindex="-1" href="#"></a><ul class="dropdown-menu"></ul></li>',
        ).appendTo(element);
        dropDownMenu.find('a').text(label);
        topMenus[label] = dropDownMenu.find('ul');
      }
      return topMenus[label];
    };
  content.find('a').attr('data-category', 'Context Menu');
  _.each(content, function (menuItem) {
    var submenu = jQuery(menuItem).attr('data-mm-context-menu');

    if (submenu) {
      getTopMenu(submenu).append(menuItem);
    } else {
      element.append(menuItem);
    }
  });
  mapModel.addEventListener(
    'mapMoveRequested mapScaleChanged nodeSelectionChanged nodeEditRequested',
    hide,
  );
  mapModel.addEventListener('contextMenuRequested', function (nodeId, x, y) {
    var currentLayout = mapModel.getCurrentLayout(),
      node_disabled = currentLayout.nodes[nodeId].attr.disabled,
      node_type = currentLayout.nodes[nodeId].attr.nodeType;

    element.find('.enableCategory').hide();
    element.find('.disableCategory').hide();
    element.find('.createDeck').hide();
    element.find('.openDeck').hide();
    element.find('.editCategory').hide();
    element.find('.editFlashcard').hide();
    element.find('.editContent').hide();
    element.find('.editHome').hide();

    // console.log("ContextMenu, edit: " + node_type)
    switch (node_type) {
      case 'category':
        if (nodeId == 1) {
          // legacy, just in case the root node is identified as a category
          element.find('.editHome').show();
          element.find('.removeSubIdea').hide();
        } else {
          element.find('.editCategory').show();
          element.find('.createDeck').show();
          element.find('.removeSubIdea').show();
          node_disabled == 1
            ? element.find('.enableCategory').show()
            : element.find('.disableCategory').show();
        }
        break;
      case 'flashcard':
        element.find('.editFlashcard').show();
        break;
      case 'root':
        element.find('.editHome').show();
        element.find('.removeSubIdea').hide();
        break;
      case 'deck':
        element.find('.openDeck').show();
        break;
    }

    var mapOffset = jQuery('#mapContainer').offset();
    element
      .css('left', x + mapOffset.left)
      .css('top', y + (mapOffset.top - 20))
      .css('display', 'block')
      .show();
    jQuery(document).off('click', hide);
    element.on('mouseenter', function () {
      jQuery(document).off('click', hide);
    });
    element.mouseleave(hide);
    element.on('click', hide);
    jQuery(document).on('touch keydown', hide);
  });
  element.on('contextmenu', function (e) {
    e.preventDefault();
    e.stopPropagation();
    return false;
  });
  return element;
};
