
// gmap3
// jquery placeholder plugin
// placeholder-text for old browsers

$('input, textarea').placeholder();

//setup autocomplete interface on textarea
var input = document.getElementById('address');
var autocomplete = new google.maps.places.Autocomplete(input, {});

google.maps.event.addListener(autocomplete, 'place_changed', function() {
	setMaps( autocomplete.getPlace().geometry.location );
});

//load location from browser (if supported)
$(window).load(function () {
	getLocation();
});

function getLocation(){
	if (navigator.geolocation){
		navigator.geolocation.getCurrentPosition( initMaps, failedMaps );
	}else{
		alert("Browser does not support Geolocation. Creating pseudo-location (Aachen)");
	}
}

//on failed browser-location create a dummy and log error
function failedMaps( error ){
	console.log(error);
	initMaps( new google.maps.LatLng(50.776585,6.083612) );
}

//init maps on success of first location (or dummylocation)
function initMaps( position ){
	setMaps(position, true);
}

function setMaps( position ){
	setMaps( position, false);
}

function setMaps( position, initMap ){
	var currentPosition;
	if(position instanceof google.maps.LatLng){
		//currently used format of googles LatLng
		currentPosition = position;
	}else{
		//browser based latitude / longitude to currently used one
		currentPosition = new google.maps.LatLng(position.coords.latitude, position.coords.longitude);
	}

	var init;
	if(initMap){
		init = { 
			action:'init',
			options:{
				center: [ currentPosition.lat() , currentPosition.lng()],
				zoom: 15
			}
		}
	}

	$("#gmaptest").gmap3(
		init,
		{
			action:'clear', 
			name:'marker', 
			tag:'caseTarget'
		},
		{
			action:'addMarkers',
			markers:[
				{ 
		    		lat: currentPosition.lat(), 
		    		lng: currentPosition.lng(), 
		    		name:'marker',
		    		tag:'caseTarget'
		    	}
		    ],
		    marker:{
	      		options:{
	        		draggable: true
	      		},

				events:{
					mouseup: function(marker, event){
						setPlace(marker.getPosition());
					}
				}
			}
		},
		{
			action:'panTo',
			args: [ currentPosition ]
		}
	); 

	//set before drag
	$('#latitude').attr('value', currentPosition.lat());
	$('#longitude').attr('value', currentPosition.lng());
}

//sets address determined by marker
function setPlace(location){
	geocoder = new google.maps.Geocoder();
	geocoder.geocode({'latLng': location}, function(results, status) {
		if (status == google.maps.GeocoderStatus.OK) {
			if (results[1]) {
			    $('#address').val(results[0].formatted_address);
			}
		} else {
			alert("Geocoder failed due to: " + status);
		}
    });
	$('#latitude').attr('value', location.lat());
	$('#longitude').attr('value', location.lng());
}

$('#startcasebutton').click(function(){
	$.post('http://137.226.188.51/emurgency/gcm/sendMission', 
		{ 
			caseLatitude: $('#latitude').attr('value'), 
			caseLongitude: $('#longitude').attr('value'),
			caseAddress: $('#address').val(),
			caseNotes: $('#notes').val()
		}, 
		function(data) {
			alert('Case notifications send...');
		}
	);
});
