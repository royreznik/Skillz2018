$(document).ready(() => {
    // extra behaviour on index.js - show and hide classes by clicking on packages.
    $('.header.package').click(function(package){
        fields = $('.class.header');
        // Hide only classes from this package.
        fields.each((index, element) => $(this).attr('name') == $(element).attr('name') ? $(element).toggle() : undefined);
        // Hide inner fields
        if (fields.css('display') === 'none') {
            $('.class-fields').each((i, e) => $(e).hide() );
        }
    });
    
    // extra behaviour on index.js - show and hide class fields when clicking on classes.
    $('.header').not('.package').click(function(){
        const ARROW_DOWN = 'fa-chevron-circle-down';
        const ARROW_RIGHT = 'fa-chevron-circle-right';
		
        let fields = $(this).parent().next('.class-fields');
        fields.toggle();
        let icon = $(this).find('i').first();
        // if the fields are hidden, show an arrow pointing to the right. Else, show an arrow pointing down.
        if (fields.css('display') === 'none') {
            icon.removeClass(ARROW_DOWN).addClass(ARROW_RIGHT);
        } else {
            icon.removeClass(ARROW_RIGHT).addClass(ARROW_DOWN);
        }
    });
	
    // Go to the link on the link specified in the url, even if it is hidden.
    $(function() {
        let linkName = window.location.href.split('#')[1];
        if (linkName === undefined) {
            return;
        }
        let link = $("a[name='" + linkName + "']").first();
        if (link === undefined) {
            return;
        }
        let field = link.closest('.field');
        if (field !== undefined && field.parent().css('display') === 'none') {
            field.parent().show();
        }
        $(window).scrollTop(link.offset().top);			
    });

    // Change the width of the sidebar and the content area.
    $(window).resize();

    /**
     * Generate the class hierarchy uml diagram.
     */ 
    $(function() {
        // Checks if browser is supported
        if (!mxClient.isBrowserSupported()) {
            // Displays an error message if the browser is not supported.
            mxUtils.error('Browser is not supported!', 200, false);
            return;
        }
        
        let container = document.getElementById('UMLContainer');
        if (container === null) {
            return;
        }							
    
        // Creates the graph inside the pre-defined container
        let graph = new mxGraph(container);

        // graph isn't interactive
        graph.setEnabled(false);

        // make the container in the exact size that is required to contain the graph.
        graph.resizeContainer = true;

        // Make the graph's hyperlinks work - instead of a string, a link element is returned
        graph.convertValueToString = (cell) => cell.link !== undefined ? cell.link : '';

        // Set vertex styles
        let style = graph.getStylesheet().getDefaultVertexStyle();
        style[mxConstants.STYLE_SHAPE] = mxConstants.SHAPE_RECTANGLE;
        style[mxConstants.STYLE_GRADIENTCOLOR] = 'gainsboro';
        style[mxConstants.STYLE_FILLCOLOR] = 'whitesmoke';
        style[mxConstants.STYLE_SHADOW] = true;
        style[mxConstants.STYLE_ROUNDED] = true;
        style[mxConstants.STYLE_ARCSIZE] = 50;
        
        // Set edges style
        style = graph.getStylesheet().getDefaultEdgeStyle();
        style[mxConstants.STYLE_EDGE] = mxEdgeStyle.TopToBottom;
        style[mxConstants.STYLE_ROUNDED] = true;
        
        // Enables automatic layout on the graph and installs
        // a tree layout for all groups who's children are
        // being changed, added or removed.
        let layout = new mxCompactTreeLayout(graph, false);
        layout.useBoundingBox = false;
        layout.edgeRouting = false;
        layout.moveTree = true; // Move tree to top-left corner.
        layout.levelDistance = 30;
        layout.nodeDistance = 20;

        let layoutMgr = new mxLayoutManager(graph); // apply the layout to the graph
        
        layoutMgr.getLayout = () => layout; // apply the layout to all graph cells with 
        
        // Gets the default parent for inserting new cells. This
        // is normally the first child of the root (ie. layer 0).
        let parent = graph.getDefaultParent();
                        
        // Adds the root vertex of the tree
        graph.getModel().beginUpdate();
        try {
            // Go over the root classes and generate them and their subtrees.
            for (let rootClass of hierarchyData) {
                let root = graph.insertVertex(parent, rootClass.name, rootClass.name, 0, 0, 80, 40);
                root.link = makeLink(rootClass);
                addClasses(graph, rootClass, root);
            }

        } finally {
            // Updates the display
            graph.getModel().endUpdate();
        }
    });

    // Find Hebrew
    let reg = /[\u0590-\u05FF]/;

    // Align the Hebrew info pages to the right
    $('.treeLink').each(function(){
        if(reg.exec($(this).html())) {
            $(this).toggleClass('rtl-tree-component');
            
        }
    });

    let headerContent = $('.page-header').html();
    if (reg.exec(headerContent)) {
        $('.page-header').attr('dir', 'rtl');
        $('.page-header').children().children().html('קובץ מידע');        
    }

    markNewFiles();

    // Make inline code be ltr
    const UNICODE_LTR_MARK = '&#x200E;';
    $('code, .new-marker').each(function(){
        let content = $(this).html();
        content = UNICODE_LTR_MARK + content + UNICODE_LTR_MARK; // Inserts a unicode LTR mark
        $(this).html(content);
    });
});

$(window).resize(() => {
    // On resize of the window, change the width of the sidebar and the content area.
    $('#sidebar').width($('.row-fluid').width() * 0.232);
    $('.span9').width($('.row-fluid').width() * 0.69);
});

/**
 * This is a helper function for the UML diagram creation, that returns a link to the class page relevant to the given class object.
 * 
 * @param {Object} classObject - The class object.
 * @return {HTMLAnchorElement} A link to the class page relevant to the given class object.
 */
function makeLink(classObject) {
    let link = document.createElement('a');
    link.innerHTML = classObject.name.split('/').pop(); // The text to be displayed shouldn't be the full path
    link.href = dox.rootPath + classObject.name + '.html';
    link.style.textDecoration = 'none'; // remove the underline from the link
    return link;
}

/**
 * This is a helper function used to generate the class nodes in the index UML diagram.
 * 
 * @param {mxGraph} graph - The graph on which the classes are drawn.
 * @param {Object} currentClass - The hierarchy data regarding the current class.
 * @param {mxCell} currentClassVertex - The graph vertex of the current class, if exists.
 */
function addClasses(graph, currentClass, currentClassVertex) {
    let parent = graph.getDefaultParent();
    
    // Go over subclasses, create them and continue recursively.
    for (let classObject of currentClass.subclasses) {
        // make vertex
        let newClassVertex = graph.insertVertex(parent, classObject.name, classObject.name, 0, 0, 80, 40);
        newClassVertex.link = makeLink(classObject);

        graph.insertEdge(parent, null, '', currentClassVertex, newClassVertex); // make arrow

        addClasses(graph, classObject, newClassVertex);
    }
}


/**
 * Mark the files that their names are in the newFiles list in the JS file new, as New.
 */
function markNewFiles() {
    // For each element in the sidebar
    $('.nav-list li').each(function () {
        let name = $(this).children().eq(0).attr('title');
        // If the name is new
        if (newFiles.indexOf(name) !== -1) {
            $(this).children().append('<div class="new-marker marker"> NEW! </div>');
        }
    });

    // For each element in the index
    $('td').each(function () {
        let content = $(this).html();
        // REGEX that searches for a pattern that exists in the node
        // of <i> stuff </i> tag and then name of the class.
        let match = /<i[\s\S]*?>[\s\S]*?<\/i>\s*(\w+)/.exec(content);
        if (match !== null) {
            let name = match[1];
            let classNames = [];
            //Get the names of the classes, remove the part of the package if it exists
            newFiles.forEach(f => classNames.push(f.split('.')[1] || f.split('.')[0]));
            if (classNames.indexOf(name) !== -1) {
                $(this).append('<div class="new-marker marker"> NEW! </div>');
            }
        }
    });
}