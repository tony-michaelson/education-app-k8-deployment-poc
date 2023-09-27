/*jslint forin: true, nomen: true*/
/*global _, MAPJS, observable*/
MAPJS.MapModel = function (
  layoutCalculator,
  selectAllTitles,
  clipboardProvider,
) {
  'use strict';
  var self = this,
    clipboard = clipboardProvider || new MAPJS.MemoryClipboard(),
    analytic,
    currentLayout = {
      nodes: {},
      connectors: {},
    },
    idea,
    isInputEnabled = true,
    isEditingEnabled = true,
    currentlySelectedIdeaId,
    activatedNodes = [],
    setActiveNodes = function (activated) {
      var wasActivated = _.clone(activatedNodes);
      if (activated.length === 0) {
        activatedNodes = [currentlySelectedIdeaId];
      } else {
        activatedNodes = activated;
      }
      self.dispatchEvent(
        'activatedNodesChanged',
        _.difference(activatedNodes, wasActivated),
        _.difference(wasActivated, activatedNodes),
      );
    },
    horizontalSelectionThreshold = 300,
    moveNodes = function (nodes, deltaX, deltaY) {
      if (deltaX || deltaY) {
        _.each(nodes, function (node) {
          node.x += deltaX;
          node.y += deltaY;
          self.dispatchEvent('nodeMoved', node);
        });
      }
    },
    isAddLinkMode,
    updateCurrentLayout = function (newLayout) {
      var nodeId,
        newNode,
        oldNode,
        newConnector,
        oldConnector,
        linkId,
        newLink,
        oldLink,
        newActive;
      for (nodeId in currentLayout.connectors) {
        newConnector = newLayout.connectors[nodeId];
        oldConnector = currentLayout.connectors[nodeId];
        if (
          !newConnector ||
          newConnector.from !== oldConnector.from ||
          newConnector.to !== oldConnector.to
        ) {
          self.dispatchEvent('connectorRemoved', oldConnector);
        }
      }
      for (nodeId in currentLayout.nodes) {
        oldNode = currentLayout.nodes[nodeId];
        newNode = newLayout.nodes[nodeId];
        if (!newNode) {
          /*jslint eqeq: true, loopfunc: true*/
          if (nodeId == currentlySelectedIdeaId) {
            self.selectNode(idea.id);
          }
          newActive = _.reject(activatedNodes, function (e) {
            return e == nodeId;
          });
          if (newActive.length !== activatedNodes.length) {
            setActiveNodes(newActive);
          }
          self.dispatchEvent('nodeRemoved', oldNode, nodeId);
        }
      }
      for (nodeId in newLayout.nodes) {
        oldNode = currentLayout.nodes[nodeId];
        newNode = newLayout.nodes[nodeId];
        if (!oldNode) {
          self.dispatchEvent('nodeCreated', newNode);
        } else {
          if (newNode.x !== oldNode.x || newNode.y !== oldNode.y) {
            self.dispatchEvent('nodeMoved', newNode);
          }
          if (newNode.title !== oldNode.title) {
            self.dispatchEvent('nodeTitleChanged', newNode);
          }
          if (!_.isEqual(newNode.attr || {}, oldNode.attr || {})) {
            self.dispatchEvent('nodeAttrChanged', newNode);
          }
        }
      }
      for (nodeId in newLayout.connectors) {
        newConnector = newLayout.connectors[nodeId];
        oldConnector = currentLayout.connectors[nodeId];
        if (
          !oldConnector ||
          newConnector.from !== oldConnector.from ||
          newConnector.to !== oldConnector.to
        ) {
          self.dispatchEvent('connectorCreated', newConnector);
        }
      }
      for (linkId in newLayout.links) {
        newLink = newLayout.links[linkId];
        oldLink = currentLayout.links && currentLayout.links[linkId];
        if (oldLink) {
          if (!_.isEqual(newLink.attr || {}, (oldLink && oldLink.attr) || {})) {
            self.dispatchEvent('linkAttrChanged', newLink);
          }
        } else {
          self.dispatchEvent('linkCreated', newLink);
        }
      }
      for (linkId in currentLayout.links) {
        oldLink = currentLayout.links[linkId];
        newLink = newLayout.links && newLayout.links[linkId];
        if (!newLink) {
          self.dispatchEvent('linkRemoved', oldLink);
        }
      }
      currentLayout = newLayout;
      self.dispatchEvent('layoutChangeComplete');
    },
    revertSelectionForUndo,
    revertActivatedForUndo,
    editNewIdea = function (newIdeaId) {
      revertSelectionForUndo = currentlySelectedIdeaId;
      revertActivatedForUndo = activatedNodes.slice(0);
      self.selectNode(newIdeaId);
      self.editNode(false, true, true);
    },
    getCurrentlySelectedIdeaId = function () {
      return currentlySelectedIdeaId || idea.id;
    },
    onIdeaChanged = function () {
      revertSelectionForUndo = false;
      revertActivatedForUndo = false;
      updateCurrentLayout(self.reactivate(layoutCalculator(idea)));
    },
    currentlySelectedIdea = function () {
      return idea.findSubIdeaById(currentlySelectedIdeaId) || idea;
    },
    ensureNodeIsExpanded = function (source, nodeId) {
      var node = idea.findSubIdeaById(nodeId) || idea;
      if (node.getAttr('collapsed')) {
        idea.updateAttr(nodeId, 'collapsed', false);
      }
    };
  observable(this);
  analytic = self.dispatchEvent.bind(self, 'analytic', 'mapModel');
  self.getIdea = function () {
    return idea;
  };
  self.isEditingEnabled = function () {
    return isEditingEnabled;
  };
  self.getCurrentLayout = function () {
    return currentLayout;
  };
  self.analytic = analytic;
  self.getCurrentlySelectedIdeaId = getCurrentlySelectedIdeaId;
  this.setIdea = function (anIdea) {
    if (idea) {
      idea.removeEventListener('changed', onIdeaChanged);
      setActiveNodes([]);
      self.dispatchEvent(
        'nodeSelectionChanged',
        currentlySelectedIdeaId,
        false,
      );
      currentlySelectedIdeaId = undefined;
    }
    idea = anIdea;
    idea.addEventListener('changed', onIdeaChanged);
    onIdeaChanged();
    self.selectNode(idea.id, true);
    //self.dispatchEvent('mapViewResetRequested');
  };
  this.setEditingEnabled = function (value) {
    isEditingEnabled = value;
  };
  this.getEditingEnabled = function () {
    return isEditingEnabled;
  };
  this.setInputEnabled = function (value) {
    if (isInputEnabled !== value) {
      isInputEnabled = value;
      self.dispatchEvent('inputEnabledChanged', value);
    }
  };
  this.getInputEnabled = function () {
    return isInputEnabled;
  };
  this.selectNode = function (id, force, appendToActive) {
    if (
      force ||
      (isInputEnabled &&
        (id !== currentlySelectedIdeaId || !self.isActivated(id)))
    ) {
      if (currentlySelectedIdeaId) {
        self.dispatchEvent(
          'nodeSelectionChanged',
          currentlySelectedIdeaId,
          false,
        );
      }
      currentlySelectedIdeaId = id;
      if (appendToActive) {
        self.activateNode('internal', id);
      } else {
        setActiveNodes([id]);
      }

      self.dispatchEvent('nodeSelectionChanged', id, true);
    }
  };
  this.clickNode = function (id, event) {
    var button = event && event.button;
    if (event && (event.altKey || event.ctrlKey || event.metaKey)) {
      self.addLink('mouse', id);
    } else if (event && event.shiftKey) {
      /*don't stop propagation, this is needed for drop targets*/
      self.toggleActivationOnNode('mouse', id);
    } else if (isAddLinkMode && !button) {
      this.addLink('mouse', id);
      this.toggleAddLinkMode();
    } else {
      this.selectNode(id);
      if (button && isInputEnabled) {
        self.dispatchEvent(
          'contextMenuRequested',
          id,
          event.layerX,
          event.layerY,
        );
      }
    }
  };
  this.findIdeaById = function (id) {
    /*jslint eqeq:true */
    if (idea.id == id) {
      return idea;
    }
    return idea.findSubIdeaById(id);
  };
  this.getSelectedStyle = function (prop) {
    return this.getStyleForId(currentlySelectedIdeaId, prop);
  };
  this.getStyleForId = function (id, prop) {
    var node = currentLayout.nodes && currentLayout.nodes[id];
    return node && node.attr && node.attr.style && node.attr.style[prop];
  };
  this.toggleCollapse = function (source) {
    var selectedIdea = currentlySelectedIdea(),
      isCollapsed;
    if (self.isActivated(selectedIdea.id) && _.size(selectedIdea.ideas) > 0) {
      isCollapsed = selectedIdea.getAttr('collapsed');
    } else {
      isCollapsed = self.everyActivatedIs(function (id) {
        var node = self.findIdeaById(id);
        if (node && _.size(node.ideas) > 0) {
          return node.getAttr('collapsed');
        }
        return true;
      });
    }

    var that = this;

    mpio.patchMapNodeAttributes(
      {
        collapsed: !isCollapsed,
      },
      top.deck_id,
      top.segmentID,
      selectedIdea.attr.id,
      () => {
        that.collapse(source, !isCollapsed);
      },
      (error) => {
        top.bootbox.alert('Something went wrong! Try again.');
      },
    );
  };
  this.collapse = function (source, doCollapse) {
    analytic('collapse:' + doCollapse, source);
    var contextNodeId = getCurrentlySelectedIdeaId(),
      contextNode = function () {
        return (
          contextNodeId &&
          currentLayout &&
          currentLayout.nodes &&
          currentLayout.nodes[contextNodeId]
        );
      },
      oldContext,
      newContext;
    oldContext = contextNode();
    if (isInputEnabled) {
      self.applyToActivated(function (id) {
        var node = self.findIdeaById(id);
        if (node && (!doCollapse || (node.ideas && _.size(node.ideas) > 0))) {
          idea.updateAttr(id, 'collapsed', doCollapse);
        }
      });
    }
    newContext = contextNode();
    if (oldContext && newContext) {
      moveNodes(
        currentLayout.nodes,
        oldContext.x - newContext.x,
        oldContext.y - newContext.y,
      );
    }
    self.dispatchEvent('layoutChangeComplete');
  };
  this.updateStyle = function (source, prop, value) {
    /*jslint eqeq:true */
    if (!isEditingEnabled) {
      return false;
    }
    if (isInputEnabled) {
      analytic('updateStyle:' + prop, source);
      self.applyToActivated(function (id) {
        if (self.getStyleForId(id, prop) != value) {
          idea.mergeAttrProperty(id, 'style', prop, value);
        }
      });
    }
  };
  this.updateLinkStyle = function (source, ideaIdFrom, ideaIdTo, prop, value) {
    if (!isEditingEnabled) {
      return false;
    }
    if (isInputEnabled) {
      analytic('updateLinkStyle:' + prop, source);
      var merged = _.extend(
        {},
        idea.getLinkAttr(ideaIdFrom, ideaIdTo, 'style'),
      );
      merged[prop] = value;
      idea.updateLinkAttr(ideaIdFrom, ideaIdTo, 'style', merged);
    }
  };
  this.addSubIdea = function (source, parentId) {
    if (!isEditingEnabled) {
      return false;
    }
    var target = parentId || currentlySelectedIdeaId,
      newId;
    analytic('addSubIdea', source);
    if (isInputEnabled) {
      idea.batch(function () {
        ensureNodeIsExpanded(source, target);
        newId = idea.addSubIdea(target);
      });
      if (newId) {
        editNewIdea(newId);
      }
    }
  };
  this.insertIntermediate = function (source) {
    if (!isEditingEnabled) {
      return false;
    }
    if (!isInputEnabled || currentlySelectedIdeaId === idea.id) {
      return false;
    }
    var activeNodes = [],
      newId;
    analytic('insertIntermediate', source);
    self.applyToActivated(function (i) {
      activeNodes.push(i);
    });
    newId = idea.insertIntermediateMultiple(activeNodes);
    if (newId) {
      editNewIdea(newId);
    }
  };
  this.addSiblingIdeaBefore = function (source) {
    var newId, parent, contextRank, newRank;
    if (!isEditingEnabled) {
      return false;
    }
    analytic('addSiblingIdeaBefore', source);
    if (!isInputEnabled) {
      return false;
    }
    parent = idea.findParent(currentlySelectedIdeaId) || idea;
    idea.batch(function () {
      ensureNodeIsExpanded(source, parent.id);
      newId = idea.addSubIdea(parent.id);
      if (newId && currentlySelectedIdeaId !== idea.id) {
        contextRank = parent.findChildRankById(currentlySelectedIdeaId);
        newRank = parent.findChildRankById(newId);
        if (contextRank * newRank < 0) {
          idea.flip(newId);
        }
        idea.positionBefore(newId, currentlySelectedIdeaId);
      }
    });
    if (newId) {
      editNewIdea(newId);
    }
  };
  this.addSiblingIdea = function (source) {
    var newId, nextId, parent, contextRank, newRank;
    if (!isEditingEnabled) {
      return false;
    }
    analytic('addSiblingIdea', source);
    if (isInputEnabled) {
      parent = idea.findParent(currentlySelectedIdeaId) || idea;
      idea.batch(function () {
        ensureNodeIsExpanded(source, parent.id);
        newId = idea.addSubIdea(parent.id);
        if (newId && currentlySelectedIdeaId !== idea.id) {
          nextId = idea.nextSiblingId(currentlySelectedIdeaId);
          contextRank = parent.findChildRankById(currentlySelectedIdeaId);
          newRank = parent.findChildRankById(newId);
          if (contextRank * newRank < 0) {
            idea.flip(newId);
          }
          if (nextId) {
            idea.positionBefore(newId, nextId);
          }
        }
      });
      if (newId) {
        editNewIdea(newId);
      }
    }
  };
  this.removeSubIdea = function (source) {
    if (!isEditingEnabled) {
      return false;
    }
    analytic('removeSubIdea', source);
    if (isInputEnabled) {
      var shouldSelectParent,
        previousSelectionId = getCurrentlySelectedIdeaId(),
        parent = idea.findParent(previousSelectionId);
      self.applyToActivated(function (id) {
        var removed = idea.removeSubIdea(id);
        /*jslint eqeq: true*/
        if (previousSelectionId == id) {
          shouldSelectParent = removed;
        }
      });
      if (shouldSelectParent) {
        self.selectNode(parent.id);
      }
    }
  };
  this.updateTitle = function (ideaId, title, isNew) {
    if (isNew) {
      // ** JSON CREATE CATEGORY ** //
      var parent = idea.findParent(currentlySelectedIdeaId) || idea;
      var _currentRank = parent.findChildRankById(currentlySelectedIdeaId);

      if (!title) {
        return false;
      }

      mpio.createMapNode(
        {
          nodeNumber: ideaId,
          parentID: parent.attr.id,
          order: _currentRank,
          name: title,
          nodeType: global_node_type,
        },
        top.deck_id,
        top.segmentID,
        (newNodeID) => {
          idea.initialiseTitle(
            ideaId,
            title,
            newNodeID,
            parent.attr.top_map_id,
            parent.attr.path,
            global_node_type,
          );
          if (global_node_type === 'flashcard') {
            this.redirectEditFlashcard(newNodeID);
          }
        },
        (error) => {
          top.bootbox.alert('Something went wrong! Try again.');
        },
      );
    } else {
      idea.updateTitle(ideaId, title);

      var node = idea.findSubIdeaById(ideaId) || idea;
      if (typeof node.attr.id != 'undefined') {
        mpio.patchMapNode(
          {
            name: title,
          },
          top.deck_id,
          top.segmentID,
          node.attr.id,
          () => {},
          (error) => {
            idea.undo();
            top.bootbox.alert('Something went wrong! Try again.');
          },
        );
      }
    }
  };
  this.editNode = function (source, shouldSelectAll, editingNew) {
    if (!isEditingEnabled) {
      return false;
    }
    if (source) {
      analytic('editNode', source);
    }
    if (!isInputEnabled) {
      return false;
    }
    var title = currentlySelectedIdea().title;
    if (_.include(selectAllTitles, title)) {
      // === 'Press Space or double-click to edit') {
      shouldSelectAll = true;
    }
    self.dispatchEvent(
      'nodeEditRequested',
      currentlySelectedIdeaId,
      shouldSelectAll,
      !!editingNew,
    );
  };
  this.editIcon = function (source) {
    if (!isEditingEnabled) {
      return false;
    }
    if (source) {
      analytic('editIcon', source);
    }
    if (!isInputEnabled) {
      return false;
    }
    self.dispatchEvent('nodeIconEditRequested', currentlySelectedIdeaId);
  };
  this.scaleUp = function (source) {
    self.scale(source, 1.25);
  };
  this.scaleDown = function (source) {
    self.scale(source, 0.8);
  };
  this.scale = function (source, scaleMultiplier, zoomPoint) {
    if (isInputEnabled) {
      self.dispatchEvent('mapScaleChanged', scaleMultiplier, zoomPoint);
      analytic(scaleMultiplier < 1 ? 'scaleDown' : 'scaleUp', source);
    }
  };
  this.move = function (source, deltaX, deltaY) {
    if (isInputEnabled) {
      self.dispatchEvent('mapMoveRequested', deltaX, deltaY);
      analytic('move', source);
    }
  };
  this.resetView = function (source) {
    if (isInputEnabled) {
      self.selectNode(idea.id);
      self.dispatchEvent('mapViewResetRequested');
      analytic('resetView', source);
    }
  };
  this.openAttachment = function (source, nodeId) {
    analytic('openAttachment', source);
    nodeId = nodeId || currentlySelectedIdeaId;
    var node = currentLayout.nodes[nodeId],
      attachment = node && node.attr && node.attr.attachment;
    if (node) {
      self.dispatchEvent('attachmentOpened', nodeId, attachment);
    }
  };
  this.setAttachment = function (source, nodeId, attachment) {
    if (!isEditingEnabled) {
      return false;
    }
    analytic('setAttachment', source);
    var hasAttachment = !!(attachment && attachment.content);
    idea.updateAttr(nodeId, 'attachment', hasAttachment && attachment);
  };
  this.editHome = function (source, nodeId) {
    analytic('editHome', source);
    var node = idea.findSubIdeaById(nodeId) || idea;
    loadModalPrimary(
      '/modals/home_properties?id=' + node.attr.id,
      $('#modal_primary'),
    );
  };
  this.editCategory = function (source, nodeId) {
    analytic('editCategory', source);
    var node = idea.findSubIdeaById(nodeId) || idea;
    // loadModalPrimary('/posts/edit/'+node.attr.id, $('#modal_primary') );
    location.href =
      '/maps/' +
      top.deck_id +
      '/' +
      top.segmentID +
      '/#/content/' +
      node.attr.id +
      '/edit';
  };
  this.nodeInfo = function (source, nodeId) {
    var node = idea.findSubIdeaById(nodeId) || idea;
    var currentLayout = mapModel.getCurrentLayout().nodes[nodeId];
    var parent = idea.findParent(currentlySelectedIdeaId) || idea;
    var _currentRank = parent.findChildRankById(currentlySelectedIdeaId);
  };

  this.createDeck = function (source, nodeId) {
    var node = idea.findSubIdeaById(nodeId) || idea;
    mpio.makeSubMap(
      top.deck_id,
      top.segmentID,
      node.attr.id,
      (newMapID) => {
        analytic('createDeck', source);
        refreshMap(function () {
          location.href = '/maps/' + top.deck_id + '/' + newMapID + '/#/editor';
        });
      },
      (error) => {
        top.bootbox.alert('Something went wrong! Try again.');
      },
    );
  };
  this.dblClickEvent = function (source, nodeId) {
    var node = idea.findSubIdeaById(nodeId) || idea;
    if (node.attr.nodeType === 'mindmap') {
      location.href =
        '/maps/' + node.attr.mapID + '/' + node.attr.id + '/#/editor';
    } else if (node.attr.nodeType === 'flashcard') {
      this.editFlashcard(source, nodeId);
    } else if (node.attr.nodeType === 'category') {
      this.editCategory(source, nodeId);
    }
  };
  this.editFlashcard = function (source, nodeId) {
    analytic('editFlashcard', source);
    var node = idea.findSubIdeaById(nodeId) || idea;
    // loadModalPrimary('/cards/edit/'+node.attr.id, $('#modal_primary') );
    this.redirectEditFlashcard(node.attr.id);

    // parent.launchEditor("/admin/cms/content_editor", "/admin/cms/content_editor_save",
    // { type:         "node_content",
    // item_id:      node.attr.id,
    // format:       "html",
    // modal_name:   "content_properties"
    // } );
  };
  this.redirectEditFlashcard = function (nodeID) {
    location.href =
      '/maps/' +
      top.deck_id +
      '/' +
      top.segmentID +
      '/#/cards/' +
      nodeID +
      '/edit';
  };

  this.enableCategory = function (source, nodeID) {
    var self = this;

    var node = idea.findSubIdeaById(nodeID) || idea;
    if (typeof node.attr.id != 'undefined') {
      mpio.patchMapNode(
        {
          disabled: false,
        },
        top.deck_id,
        top.segmentID,
        node.attr.id,
        () => {
          analytic('enableCategory', source);
          idea.updateAttr(currentlySelectedIdeaId, 'disabled', false);
          self.updateStyle('cmdline', 'background', '#e0e0e0');
        },
        (error) => {
          top.bootbox.alert('Something went wrong! Try again.');
        },
      );
    }
  };
  this.disableCategory = function (source, nodeID) {
    var self = this;

    var node = idea.findSubIdeaById(nodeID) || idea;
    if (typeof node.attr.id != 'undefined') {
      mpio.patchMapNode(
        {
          disabled: true,
        },
        top.deck_id,
        top.segmentID,
        node.attr.id,
        () => {
          analytic('disableCategory', source);
          idea.updateAttr(currentlySelectedIdeaId, 'disabled', true);
          self.updateStyle('cmdline', 'background', '#5e5e5e');
        },
        (error) => {
          top.bootbox.alert('Something went wrong! Try again.');
        },
      );
    }
  };
  this.addLink = function (source, nodeIdTo) {
    if (!isEditingEnabled) {
      return false;
    }
    analytic('addLink', source);
    idea.addLink(currentlySelectedIdeaId, nodeIdTo);
  };
  this.selectLink = function (source, link, selectionPoint) {
    if (!isEditingEnabled) {
      return false;
    }
    analytic('selectLink', source);
    if (!link) {
      return false;
    }
    self.dispatchEvent(
      'linkSelected',
      link,
      selectionPoint,
      idea.getLinkAttr(link.ideaIdFrom, link.ideaIdTo, 'style'),
    );
  };
  this.removeLink = function (source, nodeIdFrom, nodeIdTo) {
    if (!isEditingEnabled) {
      return false;
    }
    analytic('removeLink', source);
    idea.removeLink(nodeIdFrom, nodeIdTo);
  };

  this.toggleAddLinkMode = function (source) {
    if (!isEditingEnabled) {
      return false;
    }
    if (!isInputEnabled) {
      return false;
    }
    analytic('toggleAddLinkMode', source);
    isAddLinkMode = !isAddLinkMode;
    self.dispatchEvent('addLinkModeToggled', isAddLinkMode);
  };
  this.cancelCurrentAction = function (source) {
    if (!isInputEnabled) {
      return false;
    }
    if (!isEditingEnabled) {
      return false;
    }
    if (isAddLinkMode) {
      this.toggleAddLinkMode(source);
    }
  };
  self.undo = function (source) {
    if (!isEditingEnabled) {
      return false;
    }

    analytic('undo', source);
    var undoSelectionClone = revertSelectionForUndo,
      undoActivationClone = revertActivatedForUndo;
    if (isInputEnabled) {
      idea.undo();
      if (undoSelectionClone) {
        self.selectNode(undoSelectionClone);
      }
      if (undoActivationClone) {
        setActiveNodes(undoActivationClone);
      }
    }
  };
  self.redo = function (source) {
    if (!isEditingEnabled) {
      return false;
    }

    analytic('redo', source);
    if (isInputEnabled) {
      idea.redo();
    }
  };
  self.moveRelative = function (source, relativeMovement) {
    if (!isEditingEnabled) {
      return false;
    }
    analytic('moveRelative', source);
    if (isInputEnabled) {
      idea.moveRelative(currentlySelectedIdeaId, relativeMovement);
    }
  };
  self.cut = function (source) {
    if (!isEditingEnabled) {
      return false;
    }
    analytic('cut', source);
    if (isInputEnabled) {
      var activeNodeIds = [],
        parents = [],
        firstLiveParent;
      self.applyToActivated(function (nodeId) {
        activeNodeIds.push(nodeId);
        parents.push(idea.findParent(nodeId).id);
      });
      clipboard.put(idea.cloneMultiple(activeNodeIds));
      idea.removeMultiple(activeNodeIds);
      firstLiveParent = _.find(parents, idea.findSubIdeaById);
      self.selectNode(firstLiveParent || idea.id);
    }
  };
  self.copy = function (source) {
    var activeNodeIds = [];
    if (!isEditingEnabled) {
      return false;
    }
    analytic('copy', source);
    if (isInputEnabled) {
      self.applyToActivated(function (node) {
        activeNodeIds.push(node);
      });
      clipboard.put(idea.cloneMultiple(activeNodeIds));
    }
  };
  self.paste = function (source) {
    if (!isEditingEnabled) {
      return false;
    }
    analytic('paste', source);
    if (isInputEnabled) {
      var result = idea.pasteMultiple(currentlySelectedIdeaId, clipboard.get());
      if (result && result[0]) {
        self.selectNode(result[0]);
      }
    }
  };
  self.pasteStyle = function (source) {
    var clipContents = clipboard.get();
    if (!isEditingEnabled) {
      return false;
    }
    analytic('pasteStyle', source);
    if (isInputEnabled && clipContents && clipContents[0]) {
      var pastingStyle = clipContents[0].attr && clipContents[0].attr.style;
      self.applyToActivated(function (id) {
        idea.updateAttr(id, 'style', pastingStyle);
      });
    }
  };
  self.getIcon = function (nodeId) {
    var node = currentLayout.nodes[nodeId || currentlySelectedIdeaId];
    if (!node) {
      return false;
    }
    return node.attr && node.attr.icon;
  };
  self.setIcon = function (source, url, imgWidth, imgHeight, position, nodeId) {
    if (!isEditingEnabled) {
      return false;
    }
    analytic('setIcon', source);
    nodeId = nodeId || currentlySelectedIdeaId;
    var nodeIdea = self.findIdeaById(nodeId);
    if (!nodeIdea) {
      return false;
    }
    if (url) {
      idea.updateAttr(nodeId, 'icon', {
        url: url,
        width: imgWidth,
        height: imgHeight,
        position: position,
      });
    } else if (nodeIdea.title || nodeId === idea.id) {
      idea.updateAttr(nodeId, 'icon', false);
    } else {
      idea.removeSubIdea(nodeId);
    }
  };
  self.moveUp = function (source) {
    self.moveRelative(source, -1);
  };
  self.moveDown = function (source) {
    self.moveRelative(source, 1);
  };
  self.getSelectedNodeId = function () {
    return getCurrentlySelectedIdeaId();
  };
  self.centerOnNode = function (nodeId) {
    if (!currentLayout.nodes[nodeId]) {
      idea.startBatch();
      _.each(idea.calculatePath(nodeId), function (parent) {
        idea.updateAttr(parent.id, 'collapsed', false);
      });
      idea.endBatch();
    }
    self.dispatchEvent('nodeFocusRequested', nodeId);
    self.selectNode(nodeId);
  };
  self.search = function (query) {
    var result = [];
    query = query.toLocaleLowerCase();
    idea.traverse(function (contentIdea) {
      if (
        contentIdea.title &&
        contentIdea.title.toLocaleLowerCase().indexOf(query) >= 0
      ) {
        result.push({ id: contentIdea.id, title: contentIdea.title });
      }
    });
    return result;
  };
  //node activation and selection
  (function () {
    var isRootOrRightHalf = function (id) {
        return currentLayout.nodes[id].x >= currentLayout.nodes[idea.id].x;
      },
      isRootOrLeftHalf = function (id) {
        return currentLayout.nodes[id].x <= currentLayout.nodes[idea.id].x;
      },
      nodesWithIDs = function () {
        return _.map(currentLayout.nodes, function (n, nodeId) {
          return _.extend({ id: parseInt(nodeId, 10) }, n);
        });
      },
      applyToNodeLeft = function (source, analyticTag, method) {
        var node,
          rank,
          isRoot = currentlySelectedIdeaId === idea.id,
          targetRank = isRoot ? -Infinity : Infinity;
        if (!isInputEnabled) {
          return;
        }
        analytic(analyticTag, source);
        if (isRootOrLeftHalf(currentlySelectedIdeaId)) {
          node =
            idea.id === currentlySelectedIdeaId
              ? idea
              : idea.findSubIdeaById(currentlySelectedIdeaId);
          ensureNodeIsExpanded(source, node.id);
          for (rank in node.ideas) {
            rank = parseFloat(rank);
            if (
              (isRoot && rank < 0 && rank > targetRank) ||
              (!isRoot && rank > 0 && rank < targetRank)
            ) {
              targetRank = rank;
            }
          }
          if (targetRank !== Infinity && targetRank !== -Infinity) {
            method.apply(self, [node.ideas[targetRank].id]);
          }
        } else {
          method.apply(self, [idea.findParent(currentlySelectedIdeaId).id]);
        }
      },
      applyToNodeRight = function (source, analyticTag, method) {
        var node,
          rank,
          minimumPositiveRank = Infinity;
        if (!isInputEnabled) {
          return;
        }
        analytic(analyticTag, source);
        if (isRootOrRightHalf(currentlySelectedIdeaId)) {
          node =
            idea.id === currentlySelectedIdeaId
              ? idea
              : idea.findSubIdeaById(currentlySelectedIdeaId);
          ensureNodeIsExpanded(source, node.id);
          for (rank in node.ideas) {
            rank = parseFloat(rank);
            if (rank > 0 && rank < minimumPositiveRank) {
              minimumPositiveRank = rank;
            }
          }
          if (minimumPositiveRank !== Infinity) {
            method.apply(self, [node.ideas[minimumPositiveRank].id]);
          }
        } else {
          method.apply(self, [idea.findParent(currentlySelectedIdeaId).id]);
        }
      },
      applyToNodeUp = function (source, analyticTag, method) {
        var previousSibling = idea.previousSiblingId(currentlySelectedIdeaId),
          nodesAbove,
          closestNode,
          currentNode = currentLayout.nodes[currentlySelectedIdeaId];
        if (!isInputEnabled) {
          return;
        }
        analytic(analyticTag, source);
        if (previousSibling) {
          method.apply(self, [previousSibling]);
        } else {
          if (!currentNode) {
            return;
          }
          nodesAbove = _.reject(nodesWithIDs(), function (node) {
            return (
              node.y >= currentNode.y ||
              Math.abs(node.x - currentNode.x) > horizontalSelectionThreshold
            );
          });
          if (_.size(nodesAbove) === 0) {
            return;
          }
          closestNode = _.min(nodesAbove, function (node) {
            return (
              Math.pow(node.x - currentNode.x, 2) +
              Math.pow(node.y - currentNode.y, 2)
            );
          });
          method.apply(self, [closestNode.id]);
        }
      },
      applyToNodeDown = function (source, analyticTag, method) {
        var nextSibling = idea.nextSiblingId(currentlySelectedIdeaId),
          nodesBelow,
          closestNode,
          currentNode = currentLayout.nodes[currentlySelectedIdeaId];
        if (!isInputEnabled) {
          return;
        }
        analytic(analyticTag, source);
        if (nextSibling) {
          method.apply(self, [nextSibling]);
        } else {
          if (!currentNode) {
            return;
          }
          nodesBelow = _.reject(nodesWithIDs(), function (node) {
            return (
              node.y <= currentNode.y ||
              Math.abs(node.x - currentNode.x) > horizontalSelectionThreshold
            );
          });
          if (_.size(nodesBelow) === 0) {
            return;
          }
          closestNode = _.min(nodesBelow, function (node) {
            return (
              Math.pow(node.x - currentNode.x, 2) +
              Math.pow(node.y - currentNode.y, 2)
            );
          });
          method.apply(self, [closestNode.id]);
        }
      },
      applyFuncs = {
        Left: applyToNodeLeft,
        Up: applyToNodeUp,
        Down: applyToNodeDown,
        Right: applyToNodeRight,
      };
    self.getActivatedNodeIds = function () {
      return activatedNodes.slice(0);
    };
    self.activateSiblingNodes = function (source) {
      var parent = idea.findParent(currentlySelectedIdeaId),
        siblingIds;
      analytic('activateSiblingNodes', source);
      if (!parent || !parent.ideas) {
        return;
      }
      siblingIds = _.map(parent.ideas, function (child) {
        return child.id;
      });
      setActiveNodes(siblingIds);
    };
    self.activateNodeAndChildren = function (source) {
      analytic('activateNodeAndChildren', source);
      var contextId = getCurrentlySelectedIdeaId(),
        subtree = idea.getSubTreeIds(contextId);
      subtree.push(contextId);
      setActiveNodes(subtree);
    };
    _.each(['Left', 'Right', 'Up', 'Down'], function (position) {
      self['activateNode' + position] = function (source) {
        applyFuncs[position](source, 'activateNode' + position, function (
          nodeId,
        ) {
          self.selectNode(nodeId, false, true);
        });
      };
      self['selectNode' + position] = function (source) {
        applyFuncs[position](source, 'selectNode' + position, self.selectNode);
      };
    });
    self.toggleActivationOnNode = function (source, nodeId) {
      analytic('toggleActivated', source);
      if (!self.isActivated(nodeId)) {
        setActiveNodes([nodeId].concat(activatedNodes));
      } else {
        setActiveNodes(_.without(activatedNodes, nodeId));
      }
    };
    self.activateNode = function (source, nodeId) {
      analytic('activateNode', source);
      if (!self.isActivated(nodeId)) {
        activatedNodes.push(nodeId);
        self.dispatchEvent('activatedNodesChanged', [nodeId], []);
      }
    };
    self.activateChildren = function (source) {
      analytic('activateChildren', source);
      var context = currentlySelectedIdea();
      if (
        !context ||
        _.isEmpty(context.ideas) ||
        context.getAttr('collapsed')
      ) {
        return;
      }
      setActiveNodes(idea.getSubTreeIds(context.id));
    };
    self.activateSelectedNode = function (source) {
      analytic('activateSelectedNode', source);
      setActiveNodes([getCurrentlySelectedIdeaId()]);
    };
    self.isActivated = function (id) {
      /*jslint eqeq:true*/
      return _.find(activatedNodes, function (activeId) {
        return id == activeId;
      });
    };
    self.applyToActivated = function (toApply) {
      idea.batch(function () {
        _.each(activatedNodes, toApply);
      });
    };
    self.everyActivatedIs = function (predicate) {
      return _.every(activatedNodes, predicate);
    };
    self.activateLevel = function (source, level) {
      analytic('activateLevel', source);
      var toActivate = _.map(
        _.filter(currentLayout.nodes, function (node) {
          /*jslint eqeq:true*/
          return node.level == level;
        }),
        function (node) {
          return node.id;
        },
      );
      if (!_.isEmpty(toActivate)) {
        setActiveNodes(toActivate);
      }
    };
    self.reactivate = function (layout) {
      _.each(layout.nodes, function (node) {
        if (_.contains(activatedNodes, node.id)) {
          node.activated = true;
        }
      });
      return layout;
    };
  })();
};
