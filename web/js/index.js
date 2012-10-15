$(function(){
	window.setInterval(function(){
		$.ajax({
			url: 'Status',
			dataType: 'json',
			type: 'POST',
			success: function(data){
				$('#status').html('Card Jobs: '+data.cardJobs+'<br />Language Jobs: '+data.langJobs);
			}
		});
	},10000);
});
