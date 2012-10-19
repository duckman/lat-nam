$(function(){
	$('#search').submit(function(){
		$.ajax({
			url: 'Find',
			dataType: 'json',
			data: {
				query: '{name:"'+$('#text').val()+'"}'
			},
			type: 'POST',
			success: function(data){
				$('#cards').empty();
				for(var x=0;x<data.length;++x)
				{
					$('#cards').append('<div class="card"><img src="Image?type=card&multiverseid='+data[x].multiverseid+'" /><br />Name: '+data[x].name+'<br />Text: '+data[x].text+'</div>');
				}
			}
		});
		return false;
	});

	$('#text').typeahead({
		minLength: 3,
		source: function(query,callback){
			$.ajax({
				url: 'Find',
				dataType: 'json',
				data: {
					query: '{$or:[{lang:"English"},{lang:null}],name:{"$regex":"^'+query+'", "$options":"i"}}',
					fields: '{name:1}',
					sort: 'name',
					limit: 50
				},
				type: 'POST',
				success: function(data){
					var rtn = new Array();
					for(var x=0;x<data.length;++x)
					{
						if(rtn.indexOf(data[x].name)==-1)
						{
							rtn.push(data[x].name);
						}
					}
					callback(rtn);
				}
			})
		}
	});
});
