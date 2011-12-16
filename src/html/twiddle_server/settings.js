var id = 0;
var activeSongs = {};
var loadedImages = {};
var init = true;

// Position and resize stuff based on the window height/width
function resizeElements() {
	// Compute the max height so that the settings fit on the screen
	$('#song-list').height($(window).height() - $('#settings-container-inner').height() - 200);
};

// Populate the song list given an array of songs
function populateSongList(json) {
	var elmt = $('#song-list');

	for (var i in json) {
		var name = json[i]['name'];
		var path = json[i]['path'];
		var html = '<div class="draggable-song" data-name="' + name + '" data-path="' + path + '">';

		html += '<div class="text">' + name + '</div>';
		html += '</div>';

		elmt.append(html);
	}

	$('.draggable-song').draggable({ 
		appendTo: 'body',
		containment: 'window',
		scroll: false,
		helper: 'clone'
	});
};

function handleSongDrop(elmt) {
	var name = elmt.attr('data-name');
	var path = elmt.attr('data-path'); 

	if (!(activeSongs[path] >= 0)) {
		var elmtId = id++;
		addSong(name, path);
		activeSongs[path] = elmtId;
	}
	else {
		alert('You already have that song in the test list!');
	}
};

function addSong(name, path) {
	if ($('#testing-songs-container').attr('data-init') == 1) {
		$('#testing-songs-container').html('').attr('data-init', 0);
	}

	var html = '<div class="test-song" data-path="' + path + '"><div class="header4">' + name + '</div>';
	html += '<div class="test-song-inner" data-loaded="0" id="test-song-' + id + '">Loading...</div>'
	html += '</div>';

	$('#testing-songs-container').append(html);

	$.ajax('/commands/add-song?path=' + path + '&name=' + name, {
		dataType : 'json',
		type     : 'get',
		cache    : false
	});
};

function getId(path) {
	if (! activeSongs[path]) {
		activeSongs[path] = id++;
	}
	return activeSongs[path];
};

function renderImageControls(elmt) {
	var imgs = $('.spec-img-container', elmt);

	if (elmt.attr('data-rendered') != 1) {
		elmt.attr('data-rendered', 1);
		var imgs = imgs.hide();

		var ctrl = $('<div class="img-slider"></div>');
		ctrl.slider({
			min    : 1,
			max    : imgs.length,
			slide  : function(event,ui) { updateImgArea($(this),ui.value); }
		});
		ctrl.insertBefore(elmt);
		$('<span class="slider-value-text">(1 / ' + imgs.length + ')</span>').insertAfter(ctrl);

		imgs.first().show();
	}
	else {
		var slider = elmt.prev().prev();
		var activeId = getActiveValue(slider);

		slider.slider({
			min   : 1,
			max   : imgs.length,
			value : activeId,
			slide : function(event,ui) { updateImgArea($(this),ui.value); }
		});

		updateImgArea(slider, -1);
		imgs.last().hide();
	}
};

function getActiveValue(slider) {
	var val = $('.spec-img-container:visible', slider.next().next()).attr('data-id');

	if (val) {
		return val;
	}
	else {
		return 1;
	}
};

function updateImgArea(slider, val) {
	if (val != -1) {
		var elmt = $('.spec-img-container', slider.next().next())
			.hide()
			.eq(val-1);

		elmt.show().scrollTop($('img', elmt).height());
	}
	else {
		val = getActiveValue(slider);
	}
	slider.next().html('(' + val + ' / ' + slider.slider('option', 'max') + ')');
};

function refreshImages() {
	$.ajax('/images', {
		dataType : 'json',
		type     : 'get',
		cache    : false,
		success  : function(allImages) {
			for (var ix in allImages) {
				var song = allImages[ix];
				var name = song['name'];
				var path = song['path'];

				var elmt = $('.test-song').filter(function() { return $(this).attr('data-path') == song['path'] });
		
				if (elmt.length == 0) {
					addSong(name, path);
					elmt =  $('.test-song').last();
				}

				var inner = $('.test-song-inner', elmt);
				for (var i in song['images']) {
					var img = song['images'][i];

					if (! loadedImages[img]) {
						loadedImages[img] = 1;
						if (inner.attr('data-loaded') == 0) {
							inner.html('');
							inner.attr('data-loaded', 1);
						}

						inner.append('<div class="spec-img-container" data-id="' + 
							($('.spec-img-container',inner).length+1) + '">'+
							'<img src="/spectrograms/' + img + '" /></div>');
						$('img', inner).addClass('spec-img').load(function() {
							$(this).parent().scrollTop($(this).height());
							$(this).parent().width($(this).parent().parent().width());
						});
						if (!init) {
							inner.effect("highlight", {}, 3000);
						}

						renderImageControls(inner);
					}
				}
			}

			if (init) {
				init = false;
			}

			setTimeout(refreshImages, 5000);
		}
	});
};

function applySettings(settings) {
	var staticSettings = ['frame-size', 'frame-overlap', 'window-size', 'n-sds', 'peak-density'];
	var radio  = ['peak-algorithm', 'peak-retention'];

	for (var i in staticSettings) {
		var key = staticSettings[i];
		$('#' + key).val(settings[key]);
	}

	for (var i in radio) {
		var key = radio[i];
		$(':radio[name="' + key + '"]')
			.filter('[value="' + settings[key] + '"]')
			.attr('checked', true);

		// Show the appropriate params area if need be
		getShowRadioParamsFn(key)();
	}

	// Special: jQuery-UI elements, etc.
	var windowWidth = settings['window-size'];
	$('#window-size').slider("value", windowWidth);

	// Need to bind a change event handler for slider. If it's set before settings
	// are loaded it gets triggered when we set it.
	$('#window-size').bind("slidechange", function() { handleUpdateSetting($(this)); });

	// Handle smoothing function as a special case 'cause it's serialized in a
	// sort of funny way.
	var algorithm = settings["smoothing-fn"]["algorithm"];
	$(':radio[name="smoothing-fn"]')
		.filter('[value="' + algorithm + '"]')
		.attr('checked', true);

	// For now, the only smoothing fn is exponential, so only one param to worry
	// about.
	if (algorithm == "exponential") {
		$('#exp-smoothing-alpha').val(settings["smoothing-fn"]["params"]["smoothingFactor"]);
	}

	// Show appropriate params area
	getShowRadioParamsFn("smoothing-fn")();
};

function updatePeakAlgorithms() {
	var clickedVal = $(':radio[name="peak-algorithm"]').filter(':checked').val();

	$('.peak-algorithm-params').hide().each(function() {
		if ($(this).attr('data-for') == clickedVal) {
			$(this).show();
		}
	});
};

function handleUpdateSetting(obj) {
	var key;
	if (obj.attr('id')) {
		key = obj.attr('id');
	} 
	else {
		key = obj.attr('name');
	}

	var val;
	if (key == "window-size") {
		val = obj.slider("value");
	}
	else {
		val = obj.val();
	}

	$.ajax('/update-setting/' + key + '/' + val, {
		type : 'get',
		cache : false
	});
};

function getShowRadioParamsFn(radioName) {
	return function() {
		var clickedVal = $(':radio[name="' + radioName + '"]').filter(':checked').val();

		$('.' + radioName + '-params').hide().each(function() {
			if ($(this).attr('data-for') == clickedVal) {
				$(this).show();
			}
		});
	};
};

$(function() {
	// Load list of songs from the server
	$.ajax('/settings/song-list', {
		dataType : 'json',
		type     : 'get',
		cache    : false,
		success  : function(data) { populateSongList(data); }
	});

	// Handle dynamic hiding of peak detection algorithm parameter areas
	$('.peak-algorithm-params').hide();
	$('.smoothing-fn-params').hide();
	$(':radio[name="peak-algorithm"]').change(getShowRadioParamsFn("peak-algorithm"));
	$(':radio[name="smoothing-fn"]').change(getShowRadioParamsFn("smoothing-fn"));

	// Set up jQuery-UI elements
	$('#window-size').slider({
		min    : 20,
		max    : 2000
	});

	// Set up droppable zone
	$('#testing-songs-container').droppable({
		over : function() { $(this).addClass('songs-container-active'); },
		out  : function() { $(this).removeClass('songs-container-active'); },
		drop : function(event, ui) { $(this).removeClass('songs-container-active'); handleSongDrop(ui.draggable); }
	});

	// Handle the resizing stuff
	$(window).resize(function() { resizeElements(); });
	resizeElements();

	// Set up image refresher
	refreshImages();

	// Load settings from the server
	$.ajax('/settings', {
		dataType : 'json',
		type     : 'get',
		cache    : false,
		success  : function(data) { applySettings(data); }
	});

	// Set up handler to inform server of settings changes
	$('.setting').change(function() { handleUpdateSetting($(this)); });
});
