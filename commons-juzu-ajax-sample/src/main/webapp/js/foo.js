$(".update").on("click", function() {
		$(this).jzAjax("Controller.foo()", {
			dataType:"text",
			success: function(data) {
				$('.content').text(data);
			}
		});
});