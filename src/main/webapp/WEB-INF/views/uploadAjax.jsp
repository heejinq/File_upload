<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Insert title here</title>
</head>



<body>

<div class='uploadDiv' >
 <input type="file" name="uploadFile" multiple style="background-color: lavender;  color: purple;"> 
<button id='uploadBtn' style="background-color: lavender;  color: purple;">Upload</button>
</div>
<!-- 섬네일용 여기에 보여주기 스타일 -->
<style>
.uploadResult{
width:100%;
background-color:lavender;}

.uploadResult ul{
display: flex;
flex-flow: row;
justify-content: center;
align-items: center;}

.uploadResult ul li{
list-style: none;
padding: 10px;}

.uploadResult ul li img{
width: 20px;}
</style>
<style>
.uploadResult{
width:100%;
background-color: lavender;}
.uploadResult ul{
display: flex;
flex-flow: row;
justify-content: center;
align-items: center;
}
.uploadResult ul li{
list-style: none;
padding: 10px;
align-content: center;
text-align: center;}
.uploadResult ul li img{
width: 100%;}
.uploadResult ul li span{
color: white;}
.bigPictureWrapper{
position: absolute;
display: none;
justify-content: center;
align-content: center;
top: 0%;
width: 100%;
height: 100%;
background-color: lavender;
z-index: 100;
background: rgba(255,255,255,0.5);
}
.bigPicture{
position: relative;
display: flex;
justify-content: center;
align-items: center;
}
.bigPicture img{
width: 600px;}
</style>
<div class="uploadResult">
<ul>
</ul>
</div>
<div class="bigPictureWrapper">
<div class="bigPicture">
</div>
</div>

<script
  src="https://code.jquery.com/jquery-3.3.1.js"
  integrity="sha256-2Kok7MbOyxpgUVvAk/HJ2jigOSYS2auK4Pfzbm7uH60="
  crossorigin="anonymous"></script>
		
<script>
function showImage(fileCallPath){
// 	alert(fileCallPath);
$(".bigPictureWrapper").css("display","flex").show();
$(".bigPicture").html("<img src='/display?fileName=" + encodeURI(fileCallPath) + "'>").animate({width:'100%',height:'100%'},1000);
}
$(document).ready(function(){
// 	이름 목록 보여주는 부분
	var uploadResult = $(".uploadResult ul");
	function showUploadedFile(uploadResultArr){
		var str = "";
		$(uploadResultArr).each(function(i,obj){
			if (!obj.image){
			var fileCallPath = encodeURIComponent(obj.uploadPath+ "/"+obj.uuid+"-"+obj.fileName);	
			var fileLink = fileCallPath.replace(new RegExp(/\\/g),"/");
			str += "<li><div><a href='/download?fileName="+fileCallPath+"'>"
				+"<img src ='/resources/img/heart.jpeg'>" 
				+ obj.fileName + "</a>"+"<span data-file=\'"
				+fileCallPath+"\' data-type='file'>[X]</span>"+"</div></li>";
			}else{
			var fileCallPath = encodeURIComponent(obj.uploadPath+ "/sn_"+obj.uuid+"-"+obj.fileName);
			var originPath = obj.uploadPath+"\\"+obj.uuid +"-"+obj.fileName;
			originPath = originPath.replace(new RegExp(/\\/g),"/");
			
			str += "<li><a href=\"javascript:showImage(\'"+originPath+"\')\">"
				+"<img src='/display?fileName="+fileCallPath+"'/></a>"
				+"<span data-file=\'"+fileCallPath+"\' data-type='image'>[X]</span>"+"</div></li>";
		}
		});
		uploadResult.append(str);
	}
	
	$(".bigPictureWrapper").on("click", function(e){
		$(".bigPicture").animate({width:'0%', height: '0%'}, 1000);
		$('.bigPictureWrapper').hide();
	}); // bigPictureWrapper닫는부분
	

	
	var regex = new RegExp("(.*?)\.(exe|sh|zip|alz)$");
	var maxsize = 5242880;
	
	function checkExtension(fileName, fileSize) {
		if(regex.test(fileName)) {
			alert("해당 종류의 파일은 업로드가 불가능합니다.");
			return false;
		}
		if(fileSize >= maxsize) {
			alert("파일 사이즈 초과입니다. ");
			return false;
		}
		return true;
	}
	
	var cloneObj = $(".uploadDiv").clone();
	$("#uploadBtn").on("click", function(e){
		var formData = new FormData();
		
		var inputFile = $("input[name='uploadFile']");
		
		var files = inputFile[0].files;
		
		console.log(files);
		
		for(var i=0; i<files.length; i++) {
			if(!checkExtension(files[i].name,files[i].size)) {
				return false;
			}
			formData.append("uploadFile",files[i]);
		}
		
		$.ajax({
			url:'/uploadAjaxAction',
			processData: false,
			contentType: false,
			data: formData,
			type: 'POST',
			dataType:'json',
			success: function(result) {
				console.log(result);
				showUploadedFile(result);
				$(".uploadDiv").html(cloneObj.html());
			}
		});
				
	});
});
</script>
<script>
//	삭제버튼관련 여기부터
	$(".uploadResult").on("click","span",function(e){
		var targetFile =$(this).data("file");
		var type = $(this).data("type");
		console.log(targetFile);


	$.ajax({
		url : '/deleteFile',
		data : {fileName : targetFile, type : type},
		dataType : 'text',
		type : 'POST',
			success : function(result){
				alert(result);
			}
	});
	});
// 	삭제버튼 여기까지
</script>
</body>
</html>

